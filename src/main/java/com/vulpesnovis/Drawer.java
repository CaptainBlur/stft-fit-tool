package com.vulpesnovis;

import com.android.sdklib.util.SparseIntArray;
import com.vulpesnovis.StftFilter.FFTDataListener;
import com.vulpesnovis.StftFilter.Processor;
import com.vulpesnovis.WavFile.Wav_reader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Drawer extends Application implements FFTDataListener, Runnable{

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
        reader.readDefault();
        reader.printInfo();
    }

    @Override
    public void start(Stage primaryStage){
        //Here we need to get all Transform data and init JFX drawer,
        //Then handle all resize and button press events from here
        primaryStage.setTitle("Spectrum waterfall");
        primaryStage.setAlwaysOnTop(true);

        operator = new JFXOperator(primaryStage);

        int sampleRate = reader.getSampleRate();
        int winSize = Args.getValInt(initialArgs, "WINDOW_LENGTH");
        int fftSize = (int)Math.pow(2, Args.getValInt(initialArgs, "FFT_SIZE"));
        operator.orderAxisDrawing(Args.getValInt(initialArgs, "W_WIDTH"), Args.getValInt(initialArgs, "W_HEIGHT"), sampleRate, fftSize, winSize, reader.getFileDuration());

        CH = new ChangesHandler(primaryStage, reader, operator);
        inputThread = new Thread(this);
        running.set(true);
        inputThread.start();

        Processor processor = new Processor(sampleRate, fftSize, winSize, Args.getValString(initialArgs, "WINDOW_FUNC"), this, true, false);
        processor.process(reader.getDecodedInput(winSize,-1));

        int asf = 0;
        primaryStage.show();
//        System.out.println(primaryStage.);

    }

    @Override
    public void onDataComputed(int[] timeValues, int[] freqValues, SparseIntArray fftDataset, int magMin, int magMax) {
        operator.orderWaterfallDrawing(magMin,magMax,timeValues,freqValues,fftDataset);
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
                case ("FFT_SIZE") -> Args.replaceVal(enumArgs, NAName, String.valueOf(8));
                case ("WINDOW_LENGTH") -> Args.replaceVal(enumArgs, NAName, String.valueOf(20));
                case ("WINDOW_FUNC") -> Args.replaceVal(enumArgs, NAName, "rect");
            }
        } while(!NAName.matches("none"));
        return enumArgs;
    }
}