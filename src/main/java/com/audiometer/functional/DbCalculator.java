package com.audiometer.functional;

public final class DbCalculator {

    private static final int MIN_DB = -10;
    private static final int MAX_DB = 90;

    private DbCalculator() {
    }

    public static int nextDbLevel(int currentDb, boolean heard) {

        int nextDb;

        if (heard) {
            nextDb = currentDb - 10;
        } else {
            nextDb = currentDb + 5;
        }

        return clampDb(nextDb);
    }

    public static int clampDb(int db) {

        if (db < MIN_DB) {
            return MIN_DB;
        }

        if (db > MAX_DB) {
            return MAX_DB;
        }

        return db;
    }
}