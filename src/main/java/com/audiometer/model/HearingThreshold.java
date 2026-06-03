package com.audiometer.model;

public class HearingThreshold {

    private final Ear ear;
    private final int frequency;
    private final int thresholdDb;

    public HearingThreshold(Ear ear, int frequency, int thresholdDb) {
        this.ear = ear;
        this.frequency = frequency;
        this.thresholdDb = thresholdDb;
    }

    public Ear getEar() {
        return ear;
    }
    public int getFrequency() {
        return frequency;
    }
    public int getThresholdDb() {
        return thresholdDb;
    }

    @Override
    public String toString() {
        return ear + " " + frequency + " Hz -> " + thresholdDb + " dB HL";
    }
}