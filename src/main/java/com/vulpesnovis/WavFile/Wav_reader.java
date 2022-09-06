package com.vulpesnovis.WavFile;

import com.vulpesnovis.Drawer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Wav_reader {
    private WavFile wavFile;
    private Stage dialogStage;
    private final FileChooser fileChooser = new FileChooser();
    private File startDirectory;
    private File path = null;
    private List<File> files;
    private int filesSize = -1;

    private int readOffset = 0;

    private final String ANSI_RESET = "\u001B[0m";
    private final String ANSI_YELLOW = "\u001B[33m";
    public final String ANSI_RED = "\u001B[31m";
    public Wav_reader(){
        try {
            startDirectory = new File(".").getCanonicalFile();
        } catch (IOException e) {
            startDirectory = new File("/");
        }

        fileChooser.setTitle(Drawer.APP_NAME);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.wav"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
    }

    //todo need to implement deep checking after each file been read
    public void readDefault(){
        try {
            path = new File("audio.wav").getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            wavFile = WavFile.openWavFile(path);
        } catch (IOException | WavFileException e) {
            System.out.println(ANSI_RED + "Cannot read default \"audio.wav\" file in the root directory." + ANSI_RESET);
            System.exit(1);
        }
        System.out.println(ANSI_YELLOW + "Reading default file." + ANSI_RESET);
    }
    public void readPrev(){
        try {
            wavFile = WavFile.openWavFile(path);
        } catch (IOException | WavFileException e) {
            System.out.println(ANSI_RED + "Cannot read previous file." + ANSI_RESET);
            System.exit(1);
        }
    }

    public void readOne(int fileNumber){
        File newPath;
        if (fileNumber==-1) {
            dialogStage = new Stage();
            dialogStage.show();
            dialogStage.hide();

            fileChooser.setInitialDirectory(startDirectory);
            newPath = fileChooser.showOpenDialog(dialogStage);
            dialogStage.close();
        } else newPath = files.get(fileNumber);

        if (newPath ==null){
            System.out.println(ANSI_YELLOW + "\nNo file selected.\n" + ANSI_RESET);
            if (path==null) readDefault();
            else{
                readPrev();
                System.out.println(ANSI_YELLOW + "Selecting last valid file.\n" + ANSI_RESET);
            }
            return;
        }

        try {
            wavFile = WavFile.openWavFile(newPath);
        } catch (IOException | WavFileException e) {
            System.out.println(ANSI_YELLOW + "\nWrong file extension.\n" + ANSI_RESET);
            if (path==null) readDefault();
            else{
                readPrev();
                System.out.println(ANSI_YELLOW + "Selecting last valid file.\n" + ANSI_RESET);
            }
            return;
            }

        path = newPath;
        startDirectory = path.getParentFile();
    }

    public void setList() {
        dialogStage = new Stage();
        dialogStage.show();
        dialogStage.hide();

        fileChooser.setInitialDirectory(startDirectory);
        files = fileChooser.showOpenMultipleDialog(dialogStage);
        if (files == null) filesSize = -1;
        else filesSize = files.size();
        dialogStage.close();
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
    public String getFilePath(){return path.getAbsolutePath();}
    public int getFilesSize(){return filesSize;}
}
