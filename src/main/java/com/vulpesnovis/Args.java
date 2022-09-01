package com.vulpesnovis;

enum Args{
    W_WIDTH("--width", "-W"),
    W_HEIGHT("--height", "-H"),
    FFT_SIZE("--fftSize", "-s"),
    WINDOW_LENGTH("--winLength", "-l"),
    WINDOW_FUNC("--win", "-w");

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
    private void setVal(String val){
        this.val = val;
        assigned = true;
    }
    public String getValString(){return val;};
    public int getValInt(){return Integer.parseInt(val);};
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

