package com.audiometer.serial;

public interface SerialMessageListener {
    void onMessageReceived(String message);
}