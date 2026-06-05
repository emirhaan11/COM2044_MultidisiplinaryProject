package com.audiometer.service;

import com.audiometer.model.*;

import java.util.HashMap;
import java.util.Map;
import com.audiometer.functional.DbCalculator;
import com.audiometer.functional.ThresholdEvaluator;

public class HughsonWestlakeService {

    private Ear currentEar = Ear.RIGHT;
    private final int[] frequencies = {1000, 2000, 4000, 8000, 500, 250};

    private int frequencyIndex = 0;
    private int currentDb = 40;

    private boolean lastMoveWasUp = false;
    private TestState state = TestState.WAITING_RESPONSE;

    private final Map<Integer, Integer> ascendingTrialCounts = new HashMap<>();
    private final Map<Integer, Integer> ascendingHeardCounts = new HashMap<>();

    public Ear getCurrentEar() {
        return currentEar;
    }

    public int getCurrentFrequency() {
        if (frequencyIndex >= frequencies.length) {
            return frequencies[frequencies.length - 1];
        }

        return frequencies[frequencyIndex];
    }

    public int getCurrentDb() {
        return currentDb;
    }

    public TestState getState() {
        return state;
    }

    public int getAscendingTrialCountAtCurrentDb() {
        return ascendingTrialCounts.getOrDefault(currentDb, 0);
    }

    public int getAscendingHeardCountAtCurrentDb() {
        return ascendingHeardCounts.getOrDefault(currentDb, 0);
    }

    public HearingThreshold processResponse(boolean heard) {
        if (state == TestState.TEST_FINISHED) {
            return null;
        }

        if (lastMoveWasUp) {
            int trialCount = ascendingTrialCounts.getOrDefault(currentDb, 0) + 1;
            ascendingTrialCounts.put(currentDb, trialCount);

            if (heard) {
                int heardCount = ascendingHeardCounts.getOrDefault(currentDb, 0) + 1;
                ascendingHeardCounts.put(currentDb, heardCount);
            }

            int heardCount = ascendingHeardCounts.getOrDefault(currentDb, 0);

            System.out.println(
                    "ASCENDING TRIAL at " + currentDb + " dB -> "
                            + heardCount + "/" + trialCount
            );

            if (ThresholdEvaluator.isThresholdReached(
                    trialCount,
                    heardCount
            )) {
                HearingThreshold threshold = new HearingThreshold(
                        currentEar,
                        getCurrentFrequency(),
                        currentDb
                );

                moveNextFrequency();
                return threshold;
            }
        }

        currentDb = DbCalculator.nextDbLevel(currentDb, heard);
        lastMoveWasUp = !heard;
        return null;
    }

    private void clampDb() {
        if (currentDb < -10) {
            currentDb = -10;
        }

        if (currentDb > 90) {
            currentDb = 90;
        }
    }

    private void moveNextFrequency() {
        frequencyIndex++;
        currentDb = 40;
        lastMoveWasUp = false;

        ascendingTrialCounts.clear();
        ascendingHeardCounts.clear();

        if (frequencyIndex >= frequencies.length) {
            if (currentEar == Ear.RIGHT) {
                currentEar = Ear.LEFT;
                frequencyIndex = 0;
            } else {
                state = TestState.TEST_FINISHED;
                frequencyIndex = frequencies.length - 1;
            }
        }
    }


    public int getCompletedThresholdCount() {
        int completedPerEar = frequencyIndex;

        if (currentEar == Ear.LEFT) {
            completedPerEar += frequencies.length;
        }

        if (state == TestState.TEST_FINISHED) {
            return frequencies.length * 2;
        }

        return completedPerEar;
    }

    public int getTotalThresholdCount() {
        return frequencies.length * 2;
    }
}