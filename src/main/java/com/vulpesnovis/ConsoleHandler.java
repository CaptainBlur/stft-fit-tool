package com.vulpesnovis;

import com.vulpesnovis.StftFilter.Windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * This class counts on "polling" type of work.
 * And it holds the info to which method we should access during next polling call. It's based on previous user input and the menu structure
 */
public class ConsoleHandler {
    private static int display;
    private static int desire;
    private static final int MAIN_MENU = 0b1;

    private static final int FFT_SIZE = 0b11;
    private static final int FFT_SIZE_ENTERED = FFT_SIZE<<1;

    private static final int WINDOW_PARAMS = 0b101;

    private static final int WINDOW_LENGTH = WINDOW_PARAMS<<1;
    private static final int WINDOW_LENGTH_ENTERED = WINDOW_PARAMS<<2;

    private static final int WINDOW_FUNC = WINDOW_PARAMS<<3;
    private static final int WINDOW_FUNC_ENTERED = WINDOW_PARAMS<<4;

    private static final int FILE_MENU = 0b1011;

    private static final int FILE_NEW_SELECTED = FILE_MENU<<1;
    private static final int FILE_LIST_SELECTED = FILE_MENU<<2;
    private static final int FILE_NUMBER_CHANGED = FILE_MENU<<3;


    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static ChangesListener listener;
    //In two in the morning I found out that my static methods in static context just becomes freaking Godless
    private static Args[] args;

    final static String ANSI_RESET = "\u001B[0m";
    final static String HL_COL = "\u001B[32m";
    public static void initListener (ChangesListener listener, Args[] args){
        ConsoleHandler.listener = listener;
        ConsoleHandler.args = args;
        display = 0;
        desire = MAIN_MENU;
    }
    //Only ...Exec methods can change display and desire values outside this method
    //And ...Display methods meant to be engaged only ofter some change
    public static void poll() throws IOException {
        if (display!=desire){
            switch (desire) {
                case (MAIN_MENU), (FILE_NEW_SELECTED) -> mainMenuDisplay();
                case (FFT_SIZE) -> fftSizeDisplay();
                case (WINDOW_PARAMS), (WINDOW_LENGTH), (WINDOW_FUNC) -> winParamsDisplay();
                case (FILE_MENU), (FILE_LIST_SELECTED), (FILE_NUMBER_CHANGED) -> fileMenuDisplay();
                case (FFT_SIZE_ENTERED) -> displayEntered(Args.FFT_SIZE.name());
                case (WINDOW_LENGTH_ENTERED) -> displayEntered(Args.WINDOW_LENGTH.name());
                case (WINDOW_FUNC_ENTERED) -> displayEnteredWithMenu(Args.WINDOW_FUNC.name());
            }
        }
        display=desire;
        switch (display) {
            case (MAIN_MENU), (FILE_NEW_SELECTED) -> mainMenuExec();
            case (FFT_SIZE), (FFT_SIZE_ENTERED) -> fftSizeExec();
            case (WINDOW_PARAMS) -> winParamsExec();
            case (WINDOW_LENGTH), (WINDOW_LENGTH_ENTERED) -> winLengthExec();
            case (WINDOW_FUNC), (WINDOW_FUNC_ENTERED) -> winFuncExec();
            case (FILE_MENU), (FILE_LIST_SELECTED), (FILE_NUMBER_CHANGED) -> fileMenuExec();
        }
    }

    private static void mainMenuDisplay(){
        if (desire==MAIN_MENU) {
            printArgs();
            System.out.println("""
                What do you want to change?
                1) Fft size;
                2) Window parameters;
                3) Input file(s).
                
                Close window to exit the program.""");
        }
        else if (desire==FILE_NEW_SELECTED) {
            printArgs(Args.AUDIO_PATH.name());
            System.out.println("""
                New file selected (or not).
                                
                What do you want to change?
                1) Fft size;
                2) Window parameters;
                3) Input file(s).
                                
                Close window to exit the program.""");
        }
    }
    private static void mainMenuExec() throws IOException {
        if (reader.ready()) {
            int input;
            try {
                input = (Integer.parseInt(reader.readLine()));
            } catch (NumberFormatException e) {
                input=0;
            }
            switch (input) {
                case (1) -> desire = FFT_SIZE;
                case (2) -> desire = WINDOW_PARAMS;
                case (3) -> {
                    if (Objects.requireNonNull(Args.getValString(args, "FILE_MODE")).matches("multiple")) desire = FILE_MENU;
                    else if (Objects.requireNonNull(Args.getValString(args, "FILE_MODE")).matches("one")){
                        listener.changeTwo(args, ChangesHandler.CH_ONE);
                        display = 0;
                        desire = FILE_NEW_SELECTED;
                    }
                    else System.out.println("Changing input is not allowed in \"default\" mode.");
                }
                default -> System.out.println("No no no. Type \"1\", \"2\", or \"3\", please");
            }
        }
    }

    private static void fftSizeDisplay(){
        printArgs();
        System.out.println("""
                Please, enter a number of fft bins, given by number to the power of 2.
                For example, default value is 256, which means it's like you entered "8"
                Enter "q" to get back""");
    }
    private static void fftSizeExec() throws IOException {
        if (reader.ready()){
            int inputInt;
            String input = reader.readLine();
            if (input.matches("q")){
                System.out.println("Exiting");
                desire = MAIN_MENU;
            }
            else {
                try {
                    inputInt = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    inputInt=0;
                }
                if (inputInt == 0) System.out.println("It must be non-zero numeric value.");
                else if (inputInt <= 4) System.out.println("You gotta be kidding. It can't bee less than \"5\".");
                else if (inputInt >= 14) System.out.println("Ha-ha, this is too much for now. Can't be more than \"13\".");
                else{
                    display = 0;
                    desire = FFT_SIZE_ENTERED;
                    Args.replaceVal(args, "FFT_SIZE", input);
                    listener.changeOne(args);
                }
            }
        }
    }

    private static void winParamsDisplay(){
        printArgs();
        if (desire==WINDOW_PARAMS)
            System.out.println("""
                            "1" to change STFT window length.
                            "2" to change window function.
                            "q" to get back.""");
        else if (desire==WINDOW_LENGTH)
            System.out.println("Enter desired window length in milliseconds.\n" +
                    "Enter \"q\" to get back");
        else{
            System.out.println("Enter number of desired window function from the list below:");
            String[] fancyNames = Windows.getFancyNames();
            for (int i = 0; i < fancyNames.length; i++) {
                String name = fancyNames[i];
                System.out.print("\n" + (i+1) + ") " + name);
                if (i!=fancyNames.length-1) System.out.print(";");
                else System.out.print(".");
            }
            System.out.println("\n\nEnter \"q\" to get back.");
        }
    }
    private static void winParamsExec() throws IOException {
        if (reader.ready()) {
            int inputInt;
            String input = reader.readLine();
            if (input.matches("q")) {
                System.out.println("Exiting");
                desire = MAIN_MENU;
            }
            else{
                try {
                    inputInt = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    inputInt=0;
                }
                switch (inputInt) {
                    case (1) -> desire = WINDOW_LENGTH;
                    case (2) -> desire = WINDOW_FUNC;
                    default -> System.out.println("No no no. Type \"1\" or \"2\", please");
                }
            }
        }
    }
    private static void winLengthExec() throws IOException {
        if (reader.ready()){
            int inputInt;
            String input = reader.readLine();
            if (input.matches("q")){
                System.out.println("Exiting");
                desire = WINDOW_PARAMS;
            }
            else {
                try {
                    inputInt = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    inputInt=0;
                }
                if (inputInt == 0) System.out.println("It must be non-zero numeric value.");
                else{
                    display = 0;
                    desire = WINDOW_LENGTH_ENTERED;
                    Args.replaceVal(args, "WINDOW_LENGTH", input);
                    listener.changeOne(args);
                }
            }
        }
    }
    private static void winFuncExec() throws IOException {
        if (reader.ready()){
            int inputInt;
            String input = reader.readLine();
            if (input.matches("q")){
                System.out.println("Exiting");
                desire = WINDOW_PARAMS;
            }
            else if (input.matches("m")){
                winParamsDisplay();
            }
            else {
                try {
                    inputInt = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    inputInt=0;
                }
                if (inputInt==0) System.out.println("It must be non-zero numeric value.");
                else if (inputInt>0 & inputInt<=Windows.getFancyNames().length) {
                    display = 0;
                    desire = WINDOW_FUNC_ENTERED;
                    Args.replaceVal(args, "WINDOW_FUNC", Windows.getFuncNames()[inputInt-1]);
                    listener.changeOne(args);
                }
                else System.out.println("Invalid number.");
            }
        }
    }

    private static void fileMenuDisplay(){
        if (desire==FILE_MENU) {
            printArgs();
            System.out.println("""
                    "e" to change the set of input files.
                    "w" or "s" to change the number of file of the given set.
                    "q" to get back.""");
        }
        else if (desire==FILE_LIST_SELECTED){
            printArgs(Args.AUDIO_PATH.name());
            System.out.println("""
                    New set of files loaded (of not).
                    Processing first file of the set.""");
        }
        else {
            printArgs(Args.AUDIO_PATH.name());
            System.out.println("""
                    Other file selected (or not).""");
        }
    }
    private static void fileMenuExec() throws IOException {
        if (Args.getValString(args, "FILE_MODE").matches("default")) {
            desire=MAIN_MENU;
            return;
        }
        if (reader.ready()){
            String input = reader.readLine();
            switch (input){
                case ("q") -> {
                    System.out.println("Exiting.");
                    desire = MAIN_MENU;
                }
                case ("e")-> {
                    listener.changeTwo(args, ChangesHandler.CH_LIST);
                    desire = FILE_LIST_SELECTED;
                }
                case ("w")-> {
                    listener.changeTwo(args, ChangesHandler.CH_NUM_UP);
                    desire = FILE_NUMBER_CHANGED;
                }
                case ("s")-> {
                    listener.changeTwo(args, ChangesHandler.CH_NUM_DOWN);
                    desire = FILE_NUMBER_CHANGED;
                }
                default -> System.out.println("Invalid key.");
            }
            display = 0;
        }
    }

    private static void displayEntered(String name){
        printArgs(name);
        System.out.println("""
                Value changed.

                Enter "q" to get back.""");
    }
    private static void displayEnteredWithMenu(String name){
        printArgs(name);
        System.out.println("""
                Value changed.

                Enter "m" to display menu.
                Enter "q" to get back.""");
    }
    private static void printArgs(){
        StringBuilder argsStringFirst = new StringBuilder();
        StringBuilder argsStringSecond = new StringBuilder();
        Pattern nlp1 = Pattern.compile(Args.FILE_MODE.name());
        Pattern nlp2 = Pattern.compile(Args.AUDIO_PATH.name());
        for (Args arg : args) {
            if (nlp1.matcher(arg.name()).matches()) break;
            argsStringFirst.append(arg);
        }
        for (Args arg : args) {
            if (nlp1.matcher(arg.name()).matches() | nlp2.matcher(arg.name()).matches())
                argsStringSecond.append(arg);
        }

        for (int i = 0; i < argsStringFirst.length()-1; i++) {
            System.out.print("—");
        }
        System.out.print("|");

        System.out.println("\n" + argsStringFirst + "\b\b |");
        int length = Math.max(argsStringFirst.length(), argsStringSecond.length());
        for (int i = 0; i < length-1; i++) {
            if (i != argsStringSecond.length()-1)
                System.out.print("—");
            else
                System.out.print("|");
        }
        System.out.print("|");
        System.out.println("\n" + argsStringSecond + "\b\b |");

        for (int i = 0; i < argsStringSecond.length()-1; i++) {
            System.out.print("—");
        }
        System.out.print("|\n");
    }
    private static void printArgs(String hName){
        StringBuilder argsStringFirst = new StringBuilder();
        StringBuilder argsStringSecond = new StringBuilder();
        Pattern nlp1 = Pattern.compile(Args.FILE_MODE.name());
        Pattern nlp2 = Pattern.compile(Args.AUDIO_PATH.name());
        int HLLine=0;
        int HLStart=0;
        int HLEnd=0;

        for (Args arg : args){
            if (arg.name().matches(hName) && !nlp1.matcher(arg.name()).matches() & !nlp2.matcher(arg.name()).matches()){
                HLLine = 1;
                HLStart = argsStringFirst.length();
                HLEnd = HLStart + arg.toString().length();
                argsStringFirst.append(arg);
            }
            else if (!nlp1.matcher(arg.name()).matches() & !nlp2.matcher(arg.name()).matches())
                argsStringFirst.append(arg);

            if (arg.name().matches(hName) && nlp1.matcher(arg.name()).matches() | nlp2.matcher(arg.name()).matches()) {
                HLLine = 2;
                HLStart = argsStringSecond.length();
                HLEnd = HLStart + arg.toString().length();
                argsStringSecond.append(arg);
            }
            else if (nlp1.matcher(arg.name()).matches() | nlp2.matcher(arg.name()).matches())
                argsStringSecond.append(arg);
        }

        for (int i = 0; i < argsStringFirst.length()-1; i++) {
            if (i == HLStart-1 & HLLine==1) System.out.print(HL_COL);
            System.out.print("—");
            if (i == HLEnd-1 & HLLine==1) System.out.print(ANSI_RESET);
        }
        System.out.print(ANSI_RESET + "|");

        System.out.println("\n" + argsStringFirst + "\b\b |");

        int length = Math.max(argsStringFirst.length(), argsStringSecond.length());
        for (int i = 0; i < length-1; i++) {
            if (i == HLStart-1) System.out.print(HL_COL);

            if (i != argsStringSecond.length()-1)
                System.out.print("—");
            else
                System.out.print("|");

            if (i == HLEnd-2) System.out.print(ANSI_RESET);
        }
        System.out.print(ANSI_RESET + "|\n");

        System.out.println(argsStringSecond + "\b\b |");

        for (int i = 0; i < argsStringSecond.length()-1; i++) {
            if (i == HLStart-1 & HLLine==2) System.out.print(HL_COL);

            System.out.print("—");

            if (i == HLEnd-1 & HLLine==2) System.out.print(ANSI_RESET);
        }
        System.out.print(ANSI_RESET + "|\n");
    }

}

