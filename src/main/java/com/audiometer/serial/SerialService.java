package com.audiometer.serial;

import com.fazecast.jSerialComm.SerialPort;

import java.io.PrintWriter;
import java.util.Scanner;

public class SerialService {

    private SerialPort serialPort;
    private PrintWriter writer;
    private SerialMessageListener listener;

    public SerialPort[] getAvailablePorts() {
        return SerialPort.getCommPorts();
    }

    public boolean connect(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(9600);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

        if (!serialPort.openPort()) {
            return false;
        }

        writer = new PrintWriter(serialPort.getOutputStream(), true);
        startListening();

        return true;
    }

    public void setListener(SerialMessageListener listener) {
        this.listener = listener;
    }

    public void sendToneCommand(int frequency, int db, String ear) {
        sendMessage("TONE," + frequency + "," + db + "," + ear);
    }

    public void sendStopCommand() {
        sendMessage("STOP");
    }

    public void sendMessage(String message) {
        if (writer != null) {
            writer.println(message);
            writer.flush();
            System.out.println("SERIAL SENT -> " + message);
        }
    }

    private void startListening() {
        Thread thread = new Thread(() -> {
            try (Scanner scanner = new Scanner(serialPort.getInputStream())) {
                while (serialPort != null && serialPort.isOpen()) {
                    if (scanner.hasNextLine()) {
                        String message = scanner.nextLine().trim();
                        System.out.println("SERIAL RECEIVED -> " + message);

                        if (listener != null) {
                            listener.onMessageReceived(message);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Serial listen error: " + e.getMessage());
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }

    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }
}