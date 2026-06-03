package com.audiometer.model;

public class AudiogramPoint {

    private final Ear ear;
    private final int frequency;
    private final int dbHL;

    public AudiogramPoint(Ear ear, int frequency, int dbHL) {
        this.ear = ear;
        this.frequency = frequency;
        this.dbHL = dbHL;
    }

    public Ear getEar() {
        return ear;
    }
    public int getFrequency() {
        return frequency;
    }
    public int getDbHL() {
        return dbHL;
    }

    @Override
    public String toString() {
        return ear + " | " + frequency + " Hz | " + dbHL + " dB HL";
    }
}