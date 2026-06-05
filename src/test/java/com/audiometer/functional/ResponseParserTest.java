package com.audiometer.functional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseParserTest {

    @Test
    void shouldRecognizeResponseMessage() {
        assertTrue(
                ResponseParser.isResponseMessage("RESPONSE")
        );
    }

    @Test
    void shouldRecognizeResponseMessageIgnoringCase() {
        assertTrue(
                ResponseParser.isResponseMessage("response")
        );
    }

    @Test
    void shouldRejectInvalidMessage() {
        assertFalse(
                ResponseParser.isResponseMessage("HELLO")
        );
    }

    @Test
    void shouldRecognizeStopMessage() {
        assertTrue(
                ResponseParser.isStopMessage("STOP")
        );
    }

    @Test
    void shouldRejectNullMessage() {
        assertFalse(
                ResponseParser.isResponseMessage(null)
        );
    }

    @Test
    void shouldCountResponseMessagesUsingFunctionalPipeline() {

        long result = ResponseParser.countResponseMessages(
                "RESPONSE",
                "STOP",
                "response",
                "invalid"
        );

        assertEquals(2, result);
    }
}