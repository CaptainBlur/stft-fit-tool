package com.vulpesnovis.WavFile;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class Wav_reader {
    private WavFile wavFile;
    private File path = null;

    private static int readOffset = 0;
    public Wav_reader(){
    }

    private void readFile(){
        FileDialog fd = new FileDialog(new JFrame());
        fd.setDirectory("~/");
        fd.setVisible(true);

        try {
            path = new File(fd.getFile());
        } catch (NullPointerException e) {
            System.out.println("File not selected");
            System.exit(1);
        }

        System.out.println(path.getAbsolutePath());

        try {
            wavFile = WavFile.openWavFile(path);
        } catch (IOException | WavFileException e) {
            System.out.println("Please choose .wav file");
            System.exit(1);}
        wavFile.display();
        System.out.println("");
    }

    public void readDefault(){
        path = new File("audio.wav");

        try {
            wavFile = WavFile.openWavFile(path);
        } catch (IOException | WavFileException e) {
            System.out.println("Please choose .wav file");
            System.exit(1);}
        wavFile.display();
        System.out.println("");
    }

    public double[] getDecodedInput(int winSize, int winCount){
        int numFrames = winSize*winCount*(int)wavFile.getSampleRate();
        double[] buffer;
        try {
            if (winCount != -1) {
                buffer = new double[numFrames];
                wavFile.readFrames(buffer,readOffset,numFrames);
            } else{
                buffer = new double[(int)wavFile.getNumFrames()];
                wavFile.readFrames(buffer, (int) wavFile.getNumFrames());
            }
        } catch (IOException | WavFileException e) {
            System.out.println("Can't get decoded samples from file");
            throw new RuntimeException(e);
        }

        readOffset+=numFrames;
        return buffer;
    }

    public int getSampleRate(){
        return (int)wavFile.getSampleRate();
    }
    public int getFileDuration(){
        return (int)((float)wavFile.getNumFrames()/(float)wavFile.getSampleRate()*1000);
    }
}
