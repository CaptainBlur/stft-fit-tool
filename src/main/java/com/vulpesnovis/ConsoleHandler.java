package com.vulpesnovis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private static ChangesListener listener;
    //In two in the morning I found out that my static methods in static context just becomes freaking Godless
    private static Args[] args;
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
                case (MAIN_MENU) -> mainMenuDisplay();
                case (FFT_SIZE) -> fftSizeDisplay();
                case (WINDOW_PARAMS), (WINDOW_LENGTH), (WINDOW_FUNC) -> winParamsDisplay();
                case (FFT_SIZE_ENTERED) -> displayEntered(Args.FFT_SIZE.name());
                case (WINDOW_LENGTH_ENTERED) -> displayEntered(Args.WINDOW_LENGTH.name());
                case (WINDOW_FUNC_ENTERED) -> displayEnteredWithMenu(Args.WINDOW_FUNC.name());
            }
        }
        display=desire;
        switch (display) {
            case (MAIN_MENU) -> mainMenuExec();
            case (FFT_SIZE), (FFT_SIZE_ENTERED) -> fftSizeExec();
            case (WINDOW_PARAMS) -> winParamsExec();
            case (WINDOW_LENGTH), (WINDOW_LENGTH_ENTERED) -> winLengthExec();
            case (WINDOW_FUNC), (WINDOW_FUNC_ENTERED) -> winFuncExec();
        }
    }

    private static void mainMenuDisplay(){
        printArgs();
        System.out.println("""
                What do you want to change?
                1) Fft size;
                2) Window parameters.
                
                Close window to exit the program.""");
    }
    private static void mainMenuExec() throws IOException {
        if (reader.ready()) {
            int input;
            try {
                input = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException e) {
                input=0;
            }
            switch (input) {
                case (1) -> desire = FFT_SIZE;
                case (2) -> desire = WINDOW_PARAMS;
                default -> System.out.println("No no no. Type \"1\" or \"2\", please");
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
                    display = FFT_SIZE;
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
        else System.out.println("""
                    Enter number of desired window function from the list below:
                    1) Rectangular;
                    2) Hann;
                    3) Hamming;
                    
                    Enter "q" to get back.""");
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
                    display = WINDOW_LENGTH;
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
//                desire=WINDOW_FUNC;
                winParamsDisplay();
            }
            else {
                try {
                    inputInt = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    inputInt=0;
                }
                switch (inputInt){
                    case (0) -> System.out.println("It must be non-zero numeric value.");
                    case (1) -> {
                        display = WINDOW_FUNC;
                        desire = WINDOW_FUNC_ENTERED;
                        Args.replaceVal(args, "WINDOW_FUNC", "rect");
                        listener.changeOne(args);
                    }
                    case (2) -> {
                        display = WINDOW_FUNC;
                        desire = WINDOW_FUNC_ENTERED;
                        Args.replaceVal(args, "WINDOW_FUNC", "hann");
                        listener.changeOne(args);
                    }
                    case (3) -> {
                        display = WINDOW_FUNC;
                        desire = WINDOW_FUNC_ENTERED;
                        Args.replaceVal(args, "WINDOW_FUNC", "hamming");
                        listener.changeOne(args);
                    }
                    default -> System.out.println("Stop fooling around.");
                }
            }
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
        StringBuilder argsString = new StringBuilder();
        for (Args arg : args) argsString.append(arg.toString());
        for (int i = 0; i < argsString.length()-1; i++) {
            System.out.print("—");
        }
        System.out.print("|");
        System.out.println("\n" + argsString + "\b\b |");
        for (int i = 0; i < argsString.length()-1; i++) {
            System.out.print("—");
        }
        System.out.print("|\n");
    }
    private static void printArgs(String name){
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED = "\u001B[31m";
        int HLStart=0;
        int HLEnd=0;

        StringBuilder argsString = new StringBuilder();
        for (Args arg : args){
            if (arg.name().matches(name)){
                HLStart = argsString.length();
                HLEnd = HLStart + arg.toString().length();
            }
            argsString.append(arg);
        }

        for (int i = 0; i < argsString.length()-1; i++) {
            if (i == HLStart-1) System.out.print(ANSI_RED);
            System.out.print("—");
            if (i == HLEnd-1) System.out.print(ANSI_RESET);
        }
        System.out.print(ANSI_RESET + "|");

        System.out.println("\n" + argsString + "\b\b |");

        for (int i = 0; i < argsString.length()-1; i++) {
            if (i == HLStart-1) System.out.print(ANSI_RED);
            System.out.print("—");
            if (i == HLEnd-1) System.out.print(ANSI_RESET);
        }
        System.out.print(ANSI_RESET + "|\n");
    }

}

