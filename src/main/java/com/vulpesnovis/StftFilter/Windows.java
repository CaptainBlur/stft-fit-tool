package com.vulpesnovis.StftFilter;

import java.util.Arrays;

public class Windows {
    public static double[] applyHann(double[] window){
        double[] result = new double[window.length];
        int size = window.length;
        for (int i = 0; i < size; i++) {
            double deltaTheta = 2*Math.PI * i / (size - 1);
            double val = .5 - .5 * Math.cos(deltaTheta);
            result[i] = val * window[i];
            System.out.println(val);
        }
        return result;
    }
    public static double[] applyHamming(double[] window){
        double[] result = new double[window.length];
        int size = window.length;
        for (int i = 0; i < size; i++) {
            double deltaTheta = 2*Math.PI * i / (size - 1);
            double val = .54 - .46 * Math.cos(deltaTheta);
            result[i] = val * window[i];
            System.out.println(val);
        }
        return result;
    }
}
