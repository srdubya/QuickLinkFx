package com.srdubya.QuickLink.Crypto;

import java.util.Random;

public class Password {

    private static Random random;

    private static String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static String SPECIAL = "!@$&()-,:;?/\"'";
    private static String NUMERIC = "0123456789";
    private static String[] SETS = {
            LOWER,
            UPPER,
            NUMERIC,
            SPECIAL
    };

    static {
        random = new Random(System.currentTimeMillis());
    }

    public static String getPassword(int length) {

        if (length < 6) {
            throw new IllegalArgumentException("length must be at least 6");
        }

        String ret = aRandomChar(UPPER);

        for (int i = 1; i < length; i++) {
            ret += aRandomChar(SETS[random.nextInt(SETS.length)]);
        }

        return ret;
    }

    private static String aRandomChar(String set) {
        int index = random.nextInt(set.length());
        return set.substring(index, index + 1);
    }
}
