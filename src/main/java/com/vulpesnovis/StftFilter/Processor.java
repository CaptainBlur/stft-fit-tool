package com.vulpesnovis.StftFilter;

import com.android.sdklib.util.SparseIntArray;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import java.text.DecimalFormat;
import java.util.Arrays;

public class Processor {
    private final int sampleRate; //inHz
    private short winSize = 20; //in mS
    private int winTotal;
    private int fftSize = 256;
    private float winSizeinSamples;

    //I'm introducing this block of code, because we cannot just pass fft result of entire audio file, to Output
    //It will be too much for an array, especially when audio file is relatively long
    //So, we need to split the entire fft result into several groups of windows
    private short chunkSize = 10;

    private double[] buffer;
    private double[] windowNResult;
    private final FFTDataListener givenListener;
    private final boolean dbOutput;
    private final boolean oneWindow;

    /**
     *
     * @param fftSize number of samples (bins) in the output array. Put -1 as the value for default 256
     * @param winSize width of the STFT window in mS. Pass 0 for a default value which is 20.
     * @param dbOutput present output values normalized by dBFS.
     * @param oneWindow use this to process only one first window. For testing purposes.
     */
    public Processor(int sampleRate, int fftSize, int winSize, FFTDataListener listener, boolean dbOutput, boolean oneWindow){
        this.sampleRate = sampleRate;
        if(fftSize != -1) this.fftSize = fftSize;
        if (winSize != 0) this.winSize = (short)winSize;
        givenListener = listener;
        this.dbOutput = dbOutput;
        this.oneWindow = oneWindow;
        winSizeinSamples = ((float)winSize/1000) / (1/(float)sampleRate);
    }

    /**
     * JTransforms processing library is just brilliant
     * (but I don't think there's some kinda magic involved).
     * It somehow detects the Nyquist frequency of the source and makes the whole output plot up to it.
     * Meanwhile, "n" argument of the constructor is our desired FFT size in bins.
     * And we just pass our windows one after another, to make a snapshot of the whole input array
     *
     * @param buffer array of decoded samples of the whole source file (or whatever we had in input)
     */
    public void process (double[] buffer){
        this.buffer = buffer;
        winTotal = buffer.length/(int)winSizeinSamples;
        int samplesCount = 0; //total samples counter through all windows
        short timeOfInput = 0; //elapsed time in input signal
        float fftStep = (float)(sampleRate/2)/fftSize;

        //Keys starts from "00000000", including values at zero time and frequency
        SparseIntArray fftDataset = new SparseIntArray();
        //Time indexes starts from "0", from zero time value
        int[] arrayTimeValues = new int[winTotal];
        //Freq indexes starts from "1", excluding zero freq value, because it's not valid
        int[] arrayFreqValues = new int[fftSize];
        double magMax = -170;
        double magMin = 0;

        for (int i = 0; i < winTotal; i++) {//cycle for each window in the whole output sequence
            if (oneWindow) i = buffer.length/(int)winSizeinSamples;

            windowNResult = new double[Integer.max(fftSize*2, (int)winSizeinSamples)];

            System.arraycopy(buffer, samplesCount, windowNResult, 0, (int)winSizeinSamples);
            DoubleFFT_1D transformer = new DoubleFFT_1D(fftSize*2);
            transformer.realForward(windowNResult);
            float j = fftStep; //iterator for frequency val in output array (key)

            //cycle for each bin in every single window transform.
            for (int k = 1; k < fftSize; k++) {
                double re = windowNResult[2*k];
                double im = windowNResult[2*k+1];
                double mag = Math.sqrt(re*re + im*im);

                if (dbOutput){//writing array in absolute values
                    double ref = 470; //dBFS reference value measured on pure sine wave with no window function applied
                    mag = 20 * Math.log10(mag/ref);
                    System.out.println(mag + " " + k + " " + timeOfInput);
                    if (mag<-300) throw new IllegalArgumentException("Error while converting to dBFS. Please, change window height a bit");
                }
                //We need to code to values into one key int
                //Number of bin consists of 4 digits, and number of windows is 4 digits either (let's assume it's our input file limitation)

                DecimalFormat df = new DecimalFormat("0000");
                String timeIndex = df.format(i);
                String freqIndex = df.format(k);
                String key = timeIndex + freqIndex;

                arrayFreqValues[k] = (int)(j*100);
                if (magMax < mag) magMax=mag;
                if (magMin > mag) magMin=mag;

                fftDataset.append(Integer.parseInt(key), (int)mag);
//                if (i==0&k==0)System.out.println(mag + " " + j + " " + timeOfInput);

                j+=fftStep;
            }

            arrayTimeValues[i] = timeOfInput;
            timeOfInput += winSize;
            samplesCount += winSizeinSamples;
        }
        givenListener.onDataComputed(arrayTimeValues, arrayFreqValues, fftDataset, (int)magMin, (int)magMax);
    }

    //This method implies one idle run of fft transform of the entire audio file,
    //just in order to find out min and max values of Z axis
//    public void getGradientRange(int )

    public int getWinSizeInSamples(){
        return (int)winSizeinSamples;
    }
}