package com.vulpesnovis;

import com.android.sdklib.util.SparseIntArray;
import com.vulpesnovis.StftFilter.*;
import com.vulpesnovis.WavFile.Wav_reader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Drawer extends Application implements CompleteDataListener, FunctionTestListener, Runnable{

    public final static String APP_NAME = "Spectrum waterfall";
    private static String[] args;
    private Wav_reader reader;
    private JFXOperator operator;
    private static Args[] initialArgs;
    private Thread inputThread;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private ChangesHandler CH;

    public static void main(String[] args){
        Drawer.args = args;
        Application.launch(args);
    }
    @Override
    public void stop() throws Exception {
        super.stop();
        running.set(false);
        inputThread.interrupt();
    }

    @Override
    public void init() throws Exception {
        super.init();
        initialArgs = getDefaults(args);
        reader = new Wav_reader();
        String fileMode = Args.getValString(initialArgs, Args.FILE_MODE.name());
        switch (Objects.requireNonNull(fileMode)){
            case ("default") -> {
                reader.readDefault();
                Args.replaceVal(initialArgs, "AUDIO_PATH", reader.getFilePath());
            }
            case ("one") ->
                Platform.runLater(()->{
                    reader.readOne(-1);
                    Args.replaceVal(initialArgs, "AUDIO_PATH", reader.getFilePath());
                });
            case ("multiple") ->
                Platform.runLater(()->{
                    reader.setList();
                    if (reader.getFilesSize()==-1){
                        System.out.println("\u001B[33mNo files selected. Switching to default.\u001B[0m");
                        reader.readDefault();
                        Args.replaceVal(initialArgs, "FILE_MODE", "default");
                        Args.replaceVal(initialArgs, "AUDIO_PATH", reader.getFilePath());
                    }
                    else {
                        reader.readOne(0);
                        Args.replaceVal(initialArgs, "AUDIO_PATH", reader.getFilePath());
                    }
                });
        }
    }

    @Override
    public void start(Stage primaryStage){
        //Here we need to get all Transform data and init JFX drawer,
        //Then handle all resize and button press events from here
        primaryStage.setTitle(APP_NAME);
        primaryStage.setAlwaysOnTop(true);

        operator = new JFXOperator(primaryStage);

        int sampleRate = reader.getSampleRate();
        int winSize = Args.getValInt(initialArgs, "WINDOW_LENGTH");
        operator.orderAxisDrawing(Args.getValInt(initialArgs, "W_WIDTH"), Args.getValInt(initialArgs, "W_HEIGHT"), sampleRate, winSize, reader.getFileDuration());

        CH = new ChangesHandler(primaryStage, reader, operator);
        inputThread = new Thread(this);
        running.set(true);
        inputThread.start();

        if (Args.getValInt(initialArgs, "FFT_SIZE")==0)
            Args.replaceVal(initialArgs, "FFT_SIZE", String.valueOf(operator.getOptimalFftSizeMultiplier()));


//        Processor pr = new Processor(sampleRate, Args.getValInt(initialArgs, "FFT_SIZE"), "hann", this, true);
//        double[] buffer = reader.getDecodedInput(winSize,-1);
//        for (int i = 0; i < buffer.length; i++) {
//            double[] window = new double[960];
//            System.arraycopy(buffer, i, window, 0, 960);
//            pr.processSingle(window);
//            i+=960;
//        }
//        System.exit(0);


        Processor processor = new Processor(sampleRate, Args.getValInt(initialArgs, "FFT_SIZE"), winSize, Args.getValString(initialArgs, "WINDOW_FUNC"),
                this, true);
        processor.process(reader.getDecodedInput(winSize,-1));

        primaryStage.show();

    }

    @Override
    public void onDataComputed(int[] timeValues, int[] freqValues, SparseIntArray fftDataset, int magMin, int magMax) {
        operator.orderWaterfallDrawing(Args.getValInt(initialArgs, "FFT_SIZE"),magMin,magMax,timeValues,freqValues,fftDataset);
    }

    @Override
    public void onRawFunctionComputed(double[] values) {
        operator.orderFunctionDrawing(values);
    }

    @Override
    public void run() {
        ConsoleHandler.initListener(CH, initialArgs.clone());
        while (running.get()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.exit(0);
            }
            try {
                ConsoleHandler.poll();
            } catch (IOException e) {
                System.out.println("Sorry, can't read input");
            }
        }
    }

    private Args[] getDefaults(String[] args){
        Args[] enumArgs = Args.enterArgs(args);
        String NAName;
        do {
            NAName = Args.getNotAssigned(enumArgs);
            switch (NAName) {
                case ("W_WIDTH") -> {
                    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                    int width = (int)screenBounds.getWidth()-50*2;
                    Args.replaceVal(enumArgs, NAName, String.valueOf(width));
                }
                case ("W_HEIGHT") -> {
                    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
                    int height = (int)(screenBounds.getHeight()*0.5);
                    Args.replaceVal(enumArgs, NAName, String.valueOf(height));
                }
                case ("FFT_SIZE") -> Args.replaceVal(enumArgs, NAName, String.valueOf(0));
                case ("WINDOW_LENGTH") -> Args.replaceVal(enumArgs, NAName, String.valueOf(20));
                case ("WINDOW_FUNC") -> Args.replaceVal(enumArgs, NAName, "rect");
                case ("FILE_MODE") -> Args.replaceVal(enumArgs, NAName, "default");
                case ("AUDIO_PATH") -> Args.replaceVal(enumArgs, NAName, "null");
            }
        } while(!NAName.matches("none"));
        return enumArgs;
    }
}