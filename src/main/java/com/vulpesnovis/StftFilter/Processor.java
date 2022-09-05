package com.vulpesnovis.StftFilter;

import com.android.sdklib.util.SparseIntArray;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public class Processor {
    private final int sampleRate; //inHz
    private final short winSize; //in mS
    private final int fftSize;
    private final float winSizeinSamples;
    private final String func;

    //I'm introducing this block of code, because we cannot just pass fft result of entire audio file, to Output
    //It will be too much for an array, especially when audio file is relatively long
    //So, we need to split the entire fft result into several groups of windows

    private final FFTDataListener givenListener;
    private final boolean dbOutput;
    private final boolean oneWindow;

    /**
     *
     * @param fftSize number of samples (bins) in the output array, given to the power of two.
     * @param winSize width of the STFT window in mS.
     * @param dbOutput present output values normalized by dBFS.
     * @param oneWindow use this to process only one first window. For testing purposes.
     */
    public Processor(int sampleRate, int fftSize, int winSize, String func, FFTDataListener listener, boolean dbOutput, boolean oneWindow){
        this.sampleRate = sampleRate;
        this.func = Objects.requireNonNull(func);
        this.fftSize = (int)Math.pow(2,fftSize);
        this.winSize = (short)winSize;
        givenListener = Objects.requireNonNull(listener);
        this.dbOutput = dbOutput;
        this.oneWindow = oneWindow;
        winSizeinSamples = ((float)winSize/1000) / (1/(float)sampleRate);
    }

    /**
     * JTransforms processing library is just brilliant
     * (but I don't think there's some kinda magic involved).
     * It somehow detects the Nyquist frequency of the source and makes the whole output calculation up to it.
     * Meanwhile, "n" argument of the constructor is our desired FFT size in bins.
     * And we just pass our windows one after another, to make a snapshot of the whole input array
     *
     * @param buffer array of decoded samples of the whole source file (or whatever we had in input)
     */
    public void process (double[] buffer){

        int winTotal = buffer.length / (int) winSizeinSamples;
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

        for (int i = skipIdle(buffer); i < winTotal; i++) {//cycle for each window in the whole output sequence
            if (oneWindow) i = buffer.length/(int)winSizeinSamples;

            DoubleFFT_1D transformer = new DoubleFFT_1D(fftSize*2);
            double[] window = new double [(int)winSizeinSamples];
            double[] windowNResult = new double[Integer.max(fftSize * 2, (int) winSizeinSamples)];
            System.arraycopy(buffer, samplesCount, window, 0, (int)winSizeinSamples);

            System.arraycopy(Windows.applyWindow(window, func), 0, windowNResult, 0, window.length);

            transformer.realForward(windowNResult);
//            transformer.realForward(windowNResult);
            float j = fftStep; //iterator for frequency val in output array (key)

            //cycle for each bin in every single window transform.
            boolean replaceBroken = false;
            for (int k = 1; k < fftSize; k++) {
                double re = windowNResult[2*k];
                double im = windowNResult[2*k+1];
                double mag = Math.sqrt(re*re + im*im);

                if (dbOutput){//writing array in absolute values
                    double ref = 470; //dBFS reference value measured on pure sine wave with no window function applied
                    mag = 20 * Math.log10(mag/ref);
                }
                //These two values coded into one key
                //Number of bin consists of 4 digits, and number of windows is 4 digits either (let's assume it's our input file limitation)

                DecimalFormat df = new DecimalFormat("0000");
                String timeIndex = df.format(i);
                String freqIndex = df.format(k);
                String key = timeIndex + freqIndex;

                arrayFreqValues[k] = (int)(j*100);
                if (re==0&im==0) mag=magMin;
                if (magMax < mag & mag != 0) magMax = mag;
                if (magMin > mag) magMin=mag;

                if (replaceBroken & k>1){
                    String memKey = df.format(i) + df.format(k-2);
                    String prevKey = df.format(i) + df.format(k-1);
                    int prevMag = (int)((fftDataset.get(Integer.parseInt(memKey))+mag)/2);
                    fftDataset.put(Integer.parseInt(prevKey), prevMag);
                    if (i==0) System.out.println("\u001B[34mFreaking hell. Value approximated again. Key: " + df.format(k-1 + ". Something wrong with ur input") + "\u001B[0m");
                    replaceBroken = false;
                }
                replaceBroken = re == 0 & im == 0;

                fftDataset.append(Integer.parseInt(key), (int)mag);

                j+=fftStep;
            }

            if (!oneWindow) arrayTimeValues[i] = timeOfInput;
            timeOfInput += winSize;
            samplesCount += winSizeinSamples;
        }
        givenListener.onDataComputed(arrayTimeValues, arrayFreqValues, fftDataset, (int)magMin, (int)magMax);
    }
    //This method takes control over the data processing, to skip the initial amount of zero values of the input dataset.
    //And returns value for the window counter to start real processing
    private int skipIdle(double[] buffer){
        int start=0;
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i]!=0){
                start = Math.ceilDiv(i, (int)winSizeinSamples);
                break;
            }
        }
        if (start==0)
            throw new NullPointerException("Input buffer is null");
        return start-1;
    }
}
