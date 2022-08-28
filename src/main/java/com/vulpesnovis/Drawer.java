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
                    case ("--width"), ("-w") -> windowWidth = Integer.parseInt(args[i]);
                    case ("--height"), ("-h") -> windowHeight = Integer.parseInt(args[i]);
                    case ("--win") -> func = args[i];
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
        int winSize = 40;
        int fftSize = (int)Math.pow(2, 7);
        operator.orderAxisDrawing(windowWidth, windowHeight, sampleRate, fftSize, winSize, reader.getFileDuration());
        Processor processor = new Processor(sampleRate, fftSize, winSize, func, this, true, false);
//        ranges.setAxesRanges(sampleRate, 405, reader.getSamplesCount()/processor.getWinSizeInSamples(), 20);
        processor.process(reader.getDecodedInput(winSize,-1));

//        ranges.defineAxesResInPixels(windowWidth, windowHeight);
        primaryStage.show();

    }

    @Override
    public void onDataComputed(int[] timeValues, int[] freqValues, SparseIntArray fftDataset, int magMin, int magMax) {
//        System.out.println(magMax);
//        System.out.println(timeValues.length);
//        System.out.println(freqValues.length);
//        System.out.println(fftDataset.size());

        operator.orderWaterfallDrawing(magMin,magMax,timeValues,freqValues,fftDataset);
//        ranges.setGrad(magMin, magMax, Color.LIME, Color.RED);
//        ArrayList<Node> nodeList = new ArrayList<>(drawAxis(windowWidth, windowHeight, (short) 1));
//        nodeList.add(drawWaterfall(windowWidth, windowHeight));
//        root.getChildren().addAll(nodeList);

//        Canvas canvas = new Canvas(windowWidth,windowHeight);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        drawShapes(gc);
//        root.getChildren().add(canvas);
    }



    private void drawInitialChart(Canvas chartCanvas, int xOffset, int yOffset) {
        //Firstly, we need to detect x and y axes resolution

    }
    //In this method we also set some crucial parameters to draw the waterfall itself (with the gradient) right after the axes and designations
    //Global Ranges instance holds them all.


    private void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);//*
        gc.setLineWidth(5);//*
        gc.strokeLine(40, 10, 10, 40);//*
        gc.fillOval(10, 60, 30, 30);
        gc.strokeOval(60, 60, 30, 30);
        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
        gc.fillPolygon(new double[]{10, 40, 10, 40},
                new double[]{210, 210, 240, 240}, 4);
        gc.strokePolygon(new double[]{60, 90, 60, 90},
                new double[]{210, 210, 240, 240}, 4);
        gc.strokePolyline(new double[]{110, 140, 110, 140},
                new double[]{210, 210, 240, 240}, 4);
    }

}