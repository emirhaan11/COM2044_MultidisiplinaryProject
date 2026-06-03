package com.audiometer.model;

public enum TestFrequency {

    HZ_250(250),
    HZ_500(500),
    HZ_1000(1000),
    HZ_2000(2000),
    HZ_4000(4000),
    HZ_8000(8000);

    private final int value;

    TestFrequency(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value + " Hz";
    }
}