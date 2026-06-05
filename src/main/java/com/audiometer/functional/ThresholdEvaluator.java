package com.audiometer.functional;

public final class ThresholdEvaluator {

    private ThresholdEvaluator() {
    }

    public static boolean isThresholdReached(
            int trialCount,
            int heardCount
    ) {
        return trialCount >= 3 && heardCount >= 2;
    }
}