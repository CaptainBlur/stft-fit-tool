package com.vulpesnovis;

import com.android.sdklib.util.SparseArray;
import com.android.sdklib.util.SparseIntArray;
import com.vulpesnovis.StftFilter.FFTDataListener;
import com.vulpesnovis.StftFilter.Processor;
import com.vulpesnovis.WavFile.Wav_reader;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class Drawer extends Application implements FFTDataListener {

    private static int windowWidth;
    private static int windowHeight;
    private static String func;

    private Stage primaryStage;
    private JFXOperator operator;

    public static void main(String[] args){

        for (int i = 0; i < args.length; i++) {
            if (i % 2 == 1){
                switch (args[i - 1]) {
                    case ("--width"), ("-W") -> windowWidth = Integer.parseInt(args[i]);
                    case ("--height"), ("-H") -> windowHeight = Integer.parseInt(args[i]);
                    case ("--win"), ("-w") -> func = args[i];
                }
                }
            }

        Application.launch(args);
        }
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        //Here we need to get all Transform data and init JFX drawer,
        //Then handle all resize and button press events from here
        primaryStage.setTitle("Spectrum waterfall");

        operator = new JFXOperator(primaryStage);
        Wav_reader reader = new Wav_reader(){};
        reader.readDefault();
        int sampleRate = reader.getSampleRate();
        int winSize = 6;
        int fftSize = (int)Math.pow(2, 9);
        operator.orderAxisDrawing(windowWidth, windowHeight, sampleRate, fftSize, winSize, reader.getFileDuration());
        Processor processor = new Processor(sampleRate, fftSize, winSize, func, this, true, false);
//        ranges.setAxesRanges(sampleRate, 405, reader.getSamplesCount()/processor.getWinSizeInSamples(), 20);
        processor.process(reader.getDecodedInput(winSize,-1));

//        ranges.defineAxesResInPixels(windowWidth, windowHeight);
        primaryStage.show();

    }

    @Override
    public void onDataComputed(int[] timeValues, int[] freqValues, SparseIntArray fftDataset, int magMin, int magMax) {
        operator.orderWaterfallDrawing(magMin,magMax,timeValues,freqValues,fftDataset);
    }

}