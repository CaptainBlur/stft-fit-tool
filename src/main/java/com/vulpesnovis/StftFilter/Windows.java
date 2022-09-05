package com.vulpesnovis.StftFilter;

import java.util.Arrays;

public class Windows {
    private final static String[] funcNames = new String[]{"rect", "hann", "hamming", "blackman"};
    private final static String[] fancyNames = new String[]{"Rectangular", "Hann", "Hamming", "Blackman"};

    public static double[] applyWindow(double[] samples, String funcName){
        switch (funcName){
            case ("hann") -> {
                return applyHann(samples);
            }
            case ("hamming") -> {
                return applyHamming(samples);
            }
            case ("blackman") -> {
                return applyBlackman(samples);
            }
        }
        return samples;
    }
    private static double[] applyHann(double[] samples){
        double[] result = new double[samples.length];
        int size = samples.length;
        for (int i = 0; i < size; i++) {
            double deltaTheta = 2*Math.PI * i / (size - 1);
            double val = .5 - .5 * Math.cos(deltaTheta);
            result[i] = val * samples[i];
        }
        return result;
    }
    private static double[] applyHamming(double[] samples){
        double[] result = new double[samples.length];
        int size = samples.length;
        for (int i = 0; i < size; i++) {
            double deltaTheta = 2*Math.PI * i / (size - 1);
            double val = .54 - .46 * Math.cos(deltaTheta);
            result[i] = val * samples[i];
        }
        return result;
    }
    private static double[] applyBlackman(double[] samples){
        double[] result = new double[samples.length];
        int size = samples.length;
        for (int i = 0; i < size; i++) {
            double a0=0.42;
            double a1=0.5;
            double a2=0.08;
            double deltaTheta = 2*Math.PI * i / (size - 1);
            double val = a0 - a1*Math.cos(deltaTheta) + a2*Math.cos(2*deltaTheta);
            result[i] = val * samples[i];
        }
        return result;
    }
    public static String[] getFuncNames(){return funcNames;}
    public static String[] getFancyNames(){return fancyNames;}
}
