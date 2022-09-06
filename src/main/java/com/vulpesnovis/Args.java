package com.vulpesnovis;

import com.vulpesnovis.StftFilter.Processor;
import com.vulpesnovis.StftFilter.Windows;

enum Args{
    W_WIDTH("--width", "-W"),
    W_HEIGHT("--height", "-H"),
    FFT_SIZE("--fftSize", "-s"),
    WINDOW_LENGTH("--winLength", "-l"),
    WINDOW_FUNC("--func", "-f"),
    FILE_MODE("--file", "-F"),
    AUDIO_PATH;

    private final String[] names;
    private String val;
    private boolean assigned = false;
    Args (String...names){
        this.names = names;
    }

    public boolean isIt(String var){
        for (String name: names) {
            if (name.compareTo(var) == 0) return true;
        }
        return false;
    }
    //Self check implemented here
    private void setVal(String val){
        int valInt;
        boolean passed=false;

        try {valInt = Integer.parseInt(val);
        } catch (NumberFormatException e){
            valInt = 0;
        }
        switch (name()){
            case ("W_WIDTH"), ("W_HEIGHT"), ("WINDOW_LENGTH") -> {
                if (valInt>0) passed=true;
            }
            case ("FFT_SIZE") -> {
                if (valInt>4 & valInt<14 | valInt==0) passed=true;
            }
            case ("WINDOW_FUNC") -> {
                for (String func: Windows.getFuncNames()) {
                    if (func.matches(val)){
                        passed=true;
                        break;
                    }
                }
            }
            case ("FILE_MODE") -> {
                if (val.matches("default") | val.matches("one") | val.matches("multiple")) passed=true;
            }
            case ("AUDIO_PATH") -> passed=true;
        }

        if (passed){
            this.val = val;
            assigned=true;
        }
        else {
            System.out.println("\u001B[33mValue of the parameter \"" + name() + "\" has denied. Passing default val.\u001B[0m");
        }

    }
    private String getValString(){return val;};
    @Override
    public String toString() {
        return name() + "=" + getValString() + ", ";
    }

    public static Args[] enterArgs(String[] inputArgs){
        Args[] args = Args.values();
        for (int i = 0; i < inputArgs.length; i++) {
            if (i % 2 == 1){
                for (Args arg: args) if (arg.isIt(inputArgs[i-1])) arg.setVal(inputArgs[i]);
            }
        }
        return args;
    }
    public static String getNotAssigned(Args[] args){
        for (Args arg : args) if (!arg.assigned) return arg.name();
        return "none";
    }
    public static void replaceVal(Args[] args, String name, String val){
        for (Args arg : args) if (arg.name().compareTo(name) == 0) arg.setVal(val);
    }
    public static String getValString(Args[] args, String name){
        for (Args arg: args) if (arg.name().compareTo(name) == 0) return arg.val;
        return null;
    }
    public static int getValInt(Args[] args, String name){
        for (Args arg: args){
            if (arg.name().compareTo(name) == 0 & arg.val != null) return Integer.parseInt(arg.val);
        }
        return -1;
    }
}

