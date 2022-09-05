package com.vulpesnovis;

import com.vulpesnovis.StftFilter.Processor;
import com.vulpesnovis.WavFile.Wav_reader;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class exists to encapsulate the code we need to perform runtime changes.
 * I could just put it into Drawer class, but it does not look professional to me.
 */
public class ChangesHandler implements ChangesListener {
    private final Stage primaryStage;
    private final Wav_reader reader;
    private final JFXOperator operator;

    public final static int CH_ONE=0;
    public final static int CH_LIST=1;
    public final static int CH_NUM_UP=2;
    public final static int CH_NUM_DOWN=3;

    private final String ANSI_RESET = "\u001B[0m";
    private final String ANSI_YELLOW = "\u001B[33m";

    public ChangesHandler(Stage primaryStage, Wav_reader reader, JFXOperator operator) {
        this.primaryStage = primaryStage;
        this.reader = reader;
        this.operator = operator;
    }

    @Override
    public void changeOne(Args[] args) {
        reader.readPrev();
        int sampleRate = reader.getSampleRate();
        int winSize = Args.getValInt(args, "WINDOW_LENGTH");
        int fftSize = Args.getValInt(args, "FFT_SIZE");
        Platform.runLater(() -> {
            operator.clearGroup();
            operator.orderAxisDrawing(sampleRate, winSize, reader.getFileDuration());
        });
        Processor processor = new Processor(sampleRate, fftSize, winSize, Args.getValString(args, "WINDOW_FUNC"),
                (timeValues, freqValues, fftDataset, magMin, magMax) ->
                        Platform.runLater(() ->
                                operator.orderWaterfallDrawing(fftSize, magMin,magMax,timeValues,freqValues,fftDataset)),
                true, false);
        processor.process(reader.getDecodedInput(winSize,-1));
    }

    int filesNumber = 0;
    @Override
    public void changeTwo(Args[] args, int action) {

        AtomicInteger filesSize = new AtomicInteger(reader.getFilesSize());
        AtomicBoolean returned = new AtomicBoolean(false);
        switch (action){
            case (CH_ONE) ->
                Platform.runLater(()->{
                    reader.readOne(-1);
                    Args.replaceVal(args, "AUDIO_PATH", reader.getFilePath());
                    returned.set(true);
                });
            case (CH_LIST) ->
                Platform.runLater(()->{
                    reader.setList();
                    filesSize.set(reader.getFilesSize());
                    if (filesSize.get()==-1){
                        System.out.println(ANSI_YELLOW + "No files selected. Switching to default." + ANSI_RESET);
                        reader.readDefault();
                        Args.replaceVal(args, "AUDIO_PATH", reader.getFilePath());
                        Args.replaceVal(args, "FILE_MODE", "default");
                    }
                    else {
                        filesNumber = 0;
                        Args.replaceVal(args, "AUDIO_PATH", reader.getFilePath());
                        reader.readOne(0);
                    }
                    returned.set(true);
                });
            case (CH_NUM_UP) -> {
                if (filesNumber == filesSize.get() - 1) {
                    System.out.println(ANSI_YELLOW + "You already at the top of the list" + ANSI_RESET);
                    return;
                } else {
                    filesNumber++;
                    reader.readOne(filesNumber);
                    Args.replaceVal(args, "AUDIO_PATH", reader.getFilePath());
                }
                returned.set(true);
            }
            case (CH_NUM_DOWN) -> {
                if (filesNumber == 0) {
                    System.out.println(ANSI_YELLOW + "You already at the bottom of the list" + ANSI_RESET);
                    return;
                } else {
                    filesNumber--;
                    reader.readOne(filesNumber);
                    Args.replaceVal(args, "AUDIO_PATH", reader.getFilePath());
                }
                returned.set(true);
            }
        }

        while (!returned.get()){
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        int sampleRate = reader.getSampleRate();
        int winSize = Args.getValInt(args, "WINDOW_LENGTH");
        int fftSize = Args.getValInt(args, "FFT_SIZE");
        Platform.runLater(() -> {
            operator.clearGroup();
            operator.orderAxisDrawing(sampleRate, winSize, reader.getFileDuration());
        });
        Processor processor = new Processor(sampleRate, fftSize, winSize, Args.getValString(args, "WINDOW_FUNC"),
                (timeValues, freqValues, fftDataset, magMin, magMax) ->
                        Platform.runLater(() ->
                                operator.orderWaterfallDrawing(fftSize, magMin,magMax,timeValues,freqValues,fftDataset)),
                true, false);
        processor.process(reader.getDecodedInput(winSize,-1));
    }
}
