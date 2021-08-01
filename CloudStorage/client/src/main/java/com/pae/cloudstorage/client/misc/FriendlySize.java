package com.pae.cloudstorage.client.misc;

public abstract class FriendlySize {
    static double[] dividers = new double[3];

    static {
        dividers[0] = 1_000_000_000d;   // Gigabytes
        dividers[1] = 1_000_000d;       // Megabytes
        dividers[2] = 1_000d;           // Kilobytes
    }
    // Converts size to user friendly values
    public static String getFriendlySize(long bytes){
        String res;
        switch (getMaxDivider(bytes, dividers)){
            case 0: res = String.format("%.2f Gb", bytes / dividers[0]); break;
            case 1: res = String.format("%.2f Mb", bytes / dividers[1]); break;
            case 2: res = String.format("%.2f Kb", bytes / dividers[2]); break;
            default: res = String.format("%s b", bytes);           break;
        }
        return res;
    }

    private static int getMaxDivider(long val, double[] dividers){
        for (int i = 0; i < dividers.length ; i++) {
            if(val / dividers[i] > 1){
                return i;
            }
        }
        return -1;
    }
}
