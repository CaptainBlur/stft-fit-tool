package com.vulpesnovis;

import com.android.sdklib.util.SparseArray;
import com.android.sdklib.util.SparseIntArray;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import javax.naming.CannotProceedException;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//This class encapsulates all JFX drawing operations
//and provides methods to control (all two) drawing stages
//and those methods are overloaded in order to cover all using conditions
public class JFXOperator {

    private final Stage primaryStage;
    private final Group root;
    private final Scene scene;
    private final Ranges ranges;
    private final Buffer buffer;

    public JFXOperator(Stage primaryStage) {
        this.primaryStage = primaryStage;
        ranges = new Ranges();
        buffer = new Buffer();

        root = new Group();
        scene = new Scene(root);
        primaryStage.setScene(scene);
        ranges.setGradColors(Color.LIME, Color.RED);
    }


    public void orderAxisDrawing(int windowWidth, int windowHeight, int sampleRate, int fftSize, int windowSize, int fileDuration){
        ranges.setWindowSize(windowWidth, windowHeight);
        buffer.setTransformParams(sampleRate, fftSize, fileDuration / windowSize, windowSize);
        root.getChildren().addAll(drawAxis(windowWidth, windowHeight, (short) 1));
    }
    public void orderAxisDrawing(int sampleRate, int fftSize, int windowSize, int fileDuration){
        buffer.setTransformParams(sampleRate, fftSize, fileDuration / windowSize, windowSize);
        root.getChildren().addAll(drawAxis(ranges.windowWidth, ranges.windowHeight, (short) 1));
    }

    public void orderWaterfallDrawing(int magMin, int magMax, int[] timeValues, int[] freqValues, SparseIntArray fftDataset) {
        buffer.setMagnitudes(magMin, magMax);
        buffer.setData(timeValues, freqValues, fftDataset);
        root.getChildren().addAll(drawWaterfall());
    }
    public void orderDummy(){
    }
    public void clearGroup(){root.getChildren().clear();}

    private @NotNull List<Node> drawAxis(int chartCanvasX, int chartCanvasY, short interfaceScaleFactor) {
        ArrayList<Node> nodeList = new ArrayList<>();
        Canvas axisCanvas = new Canvas(chartCanvasX, chartCanvasY);
        GraphicsContext axisGC = axisCanvas.getGraphicsContext2D();

        final int lineWidth = 2;
        final int textSize = 14;
        final int margin = 20;
        final Font font = new Font(textSize);
        final Font gradFont = Font.font("Lucida Sans Unicode", FontPosture.ITALIC, textSize + 2);
        final Text ordText = new Text("12345.6");
        ordText.setFont(font);
        final Text absText = new Text("12345.6");
        absText.setFont(font);
        final Text gradientText = new Text(" -123dB");
        gradientText.setFont(gradFont);
        final int ordTextWidth = (int) ordText.getLayoutBounds().getWidth();
        final int absTextWidth = (int) absText.getLayoutBounds().getWidth();
        final int gradientTextWidth = (int) gradientText.getLayoutBounds().getWidth();
        final int marginChartLeft = ordTextWidth + (int) (margin * 2.3);
        final int marginChartBottom = absTextWidth + (int) (margin * 2.3);
        final int marginChartRight = margin * 2 + (int) ((float) margin * 1.5) + gradientTextWidth;

        final int indentBottom = 25;
        final int notchLength = 8;
        final int minimalIndent = (int) (textSize * 1.0);
        final int xAxisLength = chartCanvasX - marginChartRight - marginChartLeft;
        final int yAxisLength = chartCanvasY - margin - marginChartBottom;

        final int textCountY = (yAxisLength - lineWidth) / (textSize + minimalIndent);
        final int maxTextCountX = (xAxisLength - lineWidth) / (textSize + minimalIndent);
        final int textCountX = Math.min(buffer.winCount + 1, maxTextCountX);//bc fft at 0 time wasn't calculated
        final int realIndentY = (yAxisLength - (textSize * textCountY)) / (textCountY - 1);//excluding the highest notch to draw the arrow
        final int realIndentX = (xAxisLength - (textSize * textCountX)) / (textCountX - 1);

        final float ordValStep = (float) buffer.sr / 2 / (textCountY - 1);//excluding zero value
        final float absValStep = (float) buffer.winSize * buffer.winCount / (textCountX - 1);

        axisGC.setStroke(Color.BLACK);
        axisGC.setLineWidth(lineWidth);
        axisGC.strokeLine(marginChartLeft, chartCanvasY - marginChartBottom, chartCanvasX - marginChartRight, chartCanvasY - marginChartBottom);
        axisGC.strokeLine(marginChartLeft, chartCanvasY - marginChartBottom, marginChartLeft, margin);
        nodeList.add(axisCanvas);


        int offsetY = chartCanvasY - marginChartBottom + (textSize / 2) - lineWidth;
        float ordVal = 0;
        for (int i = 0; i < textCountY; i++) {
            String text = i > 0 ? String.format("%.1f", ordVal) : "0";
            Text sampleText = new Text(margin, offsetY, text);
            sampleText.setFont(font);
            sampleText.setWrappingWidth(marginChartLeft - margin * 2);
            sampleText.setTextAlignment(TextAlignment.RIGHT);
            nodeList.add(sampleText);

            //Drawing notches on the axis
            axisGC.strokeLine(marginChartLeft, offsetY - (double) (textSize / 2) + lineWidth, marginChartLeft - notchLength, offsetY - (double) (textSize / 2) + lineWidth);

            //Really crucial values is setting here
            if (i == 0) {
                ranges.wfOrdinateEnd = offsetY - (float) textSize / 2 + (float) lineWidth;
                Text gradientStartText = new Text(chartCanvasX - margin - gradientTextWidth, offsetY - (float) lineWidth / 2, "");
                gradientStartText.setFont(gradFont);
                ranges.gradientStartText = gradientStartText;
            } else if (i == textCountY - 1) {
                ranges.wfOrdinateStart = offsetY - (float) textSize / 2 + (float) lineWidth;
                Text gradientEndText = new Text(chartCanvasX - margin - gradientTextWidth, offsetY - (float) lineWidth / 2, "");
                gradientEndText.setFont(gradFont);
                ranges.gradientEndText = gradientEndText;
            }

            offsetY -= realIndentY + textSize;
            ordVal += ordValStep;
        }
        int offsetX = marginChartLeft + textSize / 2 + lineWidth;
        float absVal = 0;
        for (int i = 0; i < textCountX; i++) {
            double textY = chartCanvasY - marginChartBottom + indentBottom;
            String text = i > 0 ? String.format("%.1f", absVal) : "0";
            Text sampleText = new Text(offsetX, textY + textSize, text);
            sampleText.setFont(font);
            sampleText.getTransforms().add(new Rotate(90, offsetX, textY));
            nodeList.add(sampleText);

            axisGC.strokeLine(offsetX - (double) (textSize / 2) - lineWidth, chartCanvasY - marginChartBottom, offsetX - (double) (textSize / 2) - lineWidth, chartCanvasY - marginChartBottom + notchLength);

            if (i == 0) ranges.wfAbscissaStart = offsetX - (float) textSize / 2 - (float) lineWidth / 2;
            else if (i == textCountX - 1) ranges.wfAbscissaEnd = offsetX - (float) textSize / 2 - lineWidth;

            offsetX += realIndentX + textSize;
            absVal += absValStep;
        }

        axisGC.strokeLine(marginChartLeft, margin, marginChartLeft - notchLength * 0.6, margin + notchLength);
        axisGC.strokeLine(marginChartLeft, margin, marginChartLeft + notchLength * 0.6, margin + notchLength);
        axisGC.strokeLine(chartCanvasX - marginChartRight, chartCanvasY - marginChartBottom, chartCanvasX - marginChartRight - notchLength, chartCanvasY - marginChartBottom - (notchLength * 0.6));
        axisGC.strokeLine(chartCanvasX - marginChartRight, chartCanvasY - marginChartBottom, chartCanvasX - marginChartRight - notchLength, chartCanvasY - marginChartBottom + (notchLength * 0.6));

        Stop[] stops = new Stop[]{new Stop(0, ranges.startCol), new Stop(1, ranges.endCol)};
        LinearGradient lg = new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE, stops);
        double width = chartCanvasX - margin - gradientTextWidth;
        Rectangle rec = new Rectangle(chartCanvasX - margin * 2 - gradientTextWidth, ranges.wfOrdinateStart, margin, ranges.wfOrdinateEnd - ranges.wfOrdinateStart);
        rec.setFill(lg);
        nodeList.add(rec);

        return nodeList;
    }

    //The entire chart consists of horizontally aligned rectangles which is stows one on one. And that is for each window
    private List<Node> drawWaterfall() {
        int chartCanvasX = ranges.windowWidth;
        int chartCanvasY = ranges.windowHeight;
        ArrayList<Node> nodeList = new ArrayList<>();
        Canvas wfCanvas = new Canvas(chartCanvasX, chartCanvasY);
        GraphicsContext wfGC = wfCanvas.getGraphicsContext2D();
        ranges.defineWfPixelRes();
        SparseArray<Color> colors = ranges.getDiscColors();


        {
            Text text = ranges.gradientStartText;
            text.setText(" " + buffer.magMin + "dB");
            nodeList.add(text);
        }
        {
            Text text = ranges.gradientEndText;
            text.setText(" " + buffer.magMax + "dB");
            nodeList.add(text);
        }

        //I don't like the way it's written, but yet, it's the less painful way to make it work
        double offsetX = ranges.wfAbscissaStart;
        int iMax = ranges.xSampleRes == -1 ? buffer.winCount : ranges.wfWidth;
        int kMax = ranges.wfHeight - 1;

        for (int i = 0; i < iMax; i++) {
            //Drawing rectangles from up to down
            int xSampleRes = ranges.xSampleRes == -1 ? (ranges.timeSampling[i + 1] - ranges.timeSampling[i]) : (int) ranges.xSampleRes;
            int ySampleRes = 1;

            double offsetY = ranges.wfOrdinateStart;
            for (int k = kMax; k > 0; k--) {
                DecimalFormat df = new DecimalFormat("0000");
                String timeIndex = df.format(i);
                String freqIndex = df.format(ranges.freqSampling[k]);
                String key = timeIndex + freqIndex;
                int mag = buffer.fftDataset.get(Integer.parseInt(key));
                wfGC.setFill(colors.get(mag, Color.WHITE));

                wfGC.fillRect(offsetX, offsetY, xSampleRes, ySampleRes);
                offsetY += ySampleRes;
            }
            offsetX += xSampleRes;
        }
        nodeList.add(wfCanvas);

        //Basically, it occupies the space given by the previous method, to draw the gradient

        return nodeList;
    }

    //Basically, it used to transfer some parameters to the "draw..." methods
    //And encapsulate some code outside this methods
    private class Ranges {

        private int windowWidth;
        private int windowHeight;
        private double wfAbscissaStart;
        private double wfOrdinateStart;
        private double wfAbscissaEnd;
        private double wfOrdinateEnd;

        private double xSampleRes;
        //Index represents a number of pixel, and value represents an index in the array that ships with the Dataset
        private int[] freqSampling;
        private int[] timeSampling;
        private int wfHeight;
        private int wfWidth;

        private Text gradientStartText;
        private Text gradientEndText;
        private Color startCol;
        private Color endCol;
        //We need to use it only one time after each fft generation


        public void setWindowSize(int windowWidth, int windowHeight) {
            this.windowWidth = windowWidth;
            this.windowHeight = windowHeight;
        }

        private void setGradColors(Color startCol, Color endCol) {
            this.startCol = startCol;
            this.endCol = endCol;
        }

        //We need this method to define resolution of each pixel
        private void defineWfPixelRes() {
            double xInterim = (float) (wfAbscissaEnd - wfAbscissaStart) / (float) buffer.winCount;
            double yInterim = (float) (wfOrdinateEnd - wfOrdinateStart) / (float) buffer.fftSize;
            wfWidth = (int) (wfAbscissaEnd - wfAbscissaStart);
            wfHeight = (int) (wfOrdinateEnd - wfOrdinateStart);

            if (xInterim >= 1) {
                timeSampling = new int[buffer.winCount + 1];
                xSampleRes = -1;
                double index = 0;
                for (int i = 0; i <= buffer.winCount; i++) {
                    //now, we are setting starting pixels indices for each window
                    timeSampling[i] = (int) Math.round(index);
                    index += xInterim;
                }
                int g = 0;
            } else {
                timeSampling = new int[wfWidth];
                xSampleRes = 1;
                double samplesInPixel = 1 / xInterim;
                double index = 0;
                for (int i = 0; i < wfWidth; i++) {
                    timeSampling[i] = (int) Math.round(index);
                    index += samplesInPixel;
                }
            }

            freqSampling = new int[wfHeight];
            double index = 0;
            for (int i = 0; i < wfHeight; i++) {
                freqSampling[i] = (int) Math.round(index);
                index += 1 / yInterim;
            }
        }

        //Still not able to choose any color. Full functionality needed
        private SparseArray<Color> getDiscColors() {
            int range = buffer.magMax - buffer.magMin;
            SparseArray<Color> colors = new SparseArray<>(range);

            double startRed = startCol.getRed();
            double endRed = endCol.getRed();
            double startGreen = startCol.getGreen();
            double endGreen = endCol.getGreen();
            double startBlue = startCol.getBlue();
            double endBlue = endCol.getBlue();

            double redStep = (endRed - startRed) / range;
            double greenStep = (endGreen - startGreen) / range;
            double blueStep = 0;

            double redI = startRed;
            double greenI = startGreen;
            for (int i = buffer.magMin; i < buffer.magMax; i++) {
                Color color = new Color(redI, greenI, 0, 1);
                redI += redStep;
                greenI += greenStep;
                colors.put(i, color);
            }
            colors.put(buffer.magMax, new Color(endRed, endGreen, 0, 1));

            return colors;
        }
    }

    //This class stores all fft-output related transferable variables, so we can keep em cached or something
    private class Buffer {
        private int sr;
        private int fftSize;
        private int winCount;
        private int winSize;

        private int magMin;
        private int magMax;

        private int[] timeValues;
        private int[] freqValues;
        private SparseIntArray fftDataset;

        private void setTransformParams(int SR, int fftSize, int winCount, int winSize) {
            sr = SR;
            this.fftSize = fftSize;
            this.winCount = winCount;
            this.winSize = winSize;
        }

        private void setMagnitudes(int magMin, int magMax) {
            this.magMin = magMin;
            this.magMax = magMax;
        }

        private void setData(int[] timeValues, int[] freqValues, SparseIntArray fftDataset) {
            this.timeValues = timeValues;
            this.freqValues = freqValues;
            this.fftDataset = fftDataset;
        }
    }

}
