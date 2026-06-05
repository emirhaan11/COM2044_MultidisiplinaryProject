package com.audiometer.functional;

import java.util.Arrays;
import java.util.Optional;

public final class ResponseParser {

    private ResponseParser() {
    }

    public static Optional<String> normalizeMessage(String message) {

        return Optional.ofNullable(message)
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .map(String::toUpperCase);
    }

    public static boolean isResponseMessage(String message) {

        return normalizeMessage(message)
                .filter(text -> text.equals("RESPONSE"))
                .isPresent();
    }

    public static boolean isStopMessage(String message) {

        return normalizeMessage(message)
                .filter(text -> text.equals("STOP"))
                .isPresent();
    }

    public static long countResponseMessages(String... messages) {

        return Arrays.stream(messages)
                .map(ResponseParser::normalizeMessage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(text -> text.equals("RESPONSE"))
                .map(text -> 1L)
                .reduce(0L, Long::sum);
    }
}