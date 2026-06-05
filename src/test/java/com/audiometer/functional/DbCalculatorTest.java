package com.audiometer.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbCalculatorTest {

    @Test
    void shouldDecreaseDbByTenWhenPatientHeardTone() {
        int result = DbCalculator.nextDbLevel(40, true);

        assertEquals(30, result);
    }

    @Test
    void shouldIncreaseDbByFiveWhenPatientDidNotHearTone() {
        int result = DbCalculator.nextDbLevel(40, false);

        assertEquals(45, result);
    }

    @Test
    void shouldNotGoBelowMinimumDbLimit() {
        int result = DbCalculator.nextDbLevel(-10, true);

        assertEquals(-10, result);
    }

    @Test
    void shouldNotGoAboveMaximumDbLimit() {
        int result = DbCalculator.nextDbLevel(90, false);

        assertEquals(90, result);
    }
}