package com.audiometer.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdEvaluatorTest {

    @Test
    void shouldReturnTrueWhenThresholdIsReached() {

        boolean result =
                ThresholdEvaluator.isThresholdReached(3, 2);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenTrialCountIsTooLow() {

        boolean result =
                ThresholdEvaluator.isThresholdReached(2, 2);

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenHeardCountIsTooLow() {

        boolean result =
                ThresholdEvaluator.isThresholdReached(3, 1);

        assertFalse(result);
    }
}