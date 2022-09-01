package com.vulpesnovis;

import com.vulpesnovis.StftFilter.Processor;
import com.vulpesnovis.WavFile.Wav_reader;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * This class exists to encapsulate the code we need to perform runtime changes.
 * I could just put it into Drawer class, but it does not look professional to me.
 */
public class ChangesHandler implements ChangesListener {
    private final Stage primaryStage;
    private final Wav_reader reader;
    private final JFXOperator operator;

    public ChangesHandler(Stage primaryStage, Wav_reader reader, JFXOperator operator) {
        this.primaryStage = primaryStage;
        this.reader = reader;
        this.operator = operator;
    }

    @Override
    public void changeOne(Args[] args) {
        reader.readDefault();
        int sampleRate = reader.getSampleRate();
        int winSize = Args.getValInt(args, "WINDOW_LENGTH");
        int fftSize = (int)Math.pow(2, Args.getValInt(args, "FFT_SIZE"));
        Platform.runLater(() -> {
            operator.clearGroup();
            operator.orderAxisDrawing(sampleRate, fftSize, winSize, reader.getFileDuration());
        });
        Processor processor = new Processor(sampleRate, fftSize, winSize, Args.getValString(args, "WINDOW_FUNC"),
                (timeValues, freqValues, fftDataset, magMin, magMax) ->
                        Platform.runLater(() ->
                                operator.orderWaterfallDrawing(magMin,magMax,timeValues,freqValues,fftDataset)),
                true, false);
        processor.process(reader.getDecodedInput(winSize,-1));
    }
}
