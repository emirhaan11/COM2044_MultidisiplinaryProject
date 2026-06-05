package com.audiometer.ui;

import com.audiometer.model.AudiogramPoint;
import com.audiometer.model.Ear;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class AudiogramCanvas extends Canvas {

    private final List<AudiogramPoint> points = new ArrayList<>();

    private final int[] frequencies = {250, 500, 1000, 2000, 4000, 8000};
    private final int[] dbLevels = {-10, 0, 10, 20, 30, 40, 50, 60, 70, 80, 90};

    private final double leftMargin = 65;
    private final double topMargin = 35;
    private final double rightMargin = 35;
    private final double bottomMargin = 55;

    public AudiogramCanvas(double width, double height) {
        super(width, height);
        draw();
    }

    public void addPoint(AudiogramPoint point) {
        points.removeIf(p ->
                p.getEar() == point.getEar()
                        && p.getFrequency() == point.getFrequency()
        );

        points.add(point);
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        double w = getWidth();
        double h = getHeight();

        gc.clearRect(0, 0, w, h);

        drawBackground(gc, w, h);
        drawGrid(gc, w, h);
        drawLabels(gc, w, h);
        drawConnectingLines(gc, w, h);
        drawPoints(gc, w, h);
        drawPoints(gc, w, h);
    }

    private void drawBackground(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, w, h);

        gc.setStroke(Color.BLACK);
        gc.strokeRect(leftMargin, topMargin, w - leftMargin - rightMargin, h - topMargin - bottomMargin);
    }

    private void drawGrid(GraphicsContext gc, double w, double h) {
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        for (int i = 0; i < frequencies.length; i++) {
            double x = xForFrequency(frequencies[i], w);
            gc.strokeLine(x, topMargin, x, h - bottomMargin);
        }

        for (int db : dbLevels) {
            double y = yForDb(db, h);
            gc.strokeLine(leftMargin, y, w - rightMargin, y);
        }
    }

    private void drawLabels(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font(12));

        for (int freq : frequencies) {
            double x = xForFrequency(freq, w);
            gc.fillText(String.valueOf(freq), x - 14, h - bottomMargin + 22);
        }

        for (int db : dbLevels) {
            double y = yForDb(db, h);
            gc.fillText(String.valueOf(db), leftMargin - 35, y + 4);
        }

        gc.setFont(Font.font(14));
        gc.fillText("Frequency (Hz)", w / 2 - 45, h - 12);
        gc.fillText("dB HL", 15, 25);

        gc.setFill(Color.RED);
        gc.fillText("O = Right Ear", w - 150, 22);

        gc.setFill(Color.BLUE);
        gc.fillText("X = Left Ear", w - 150, 42);
    }

    private void drawPoints(GraphicsContext gc, double w, double h) {
        gc.setLineWidth(2);
        gc.setFont(Font.font(18));

        for (AudiogramPoint point : points) {
            double x = xForFrequency(point.getFrequency(), w);
            double y = yForDb(point.getDbHL(), h);

            if (point.getEar() == Ear.RIGHT) {
                gc.setStroke(Color.RED);
                gc.strokeOval(x - 8, y - 8, 16, 16);
            } else {
                gc.setStroke(Color.BLUE);
                gc.strokeLine(x - 8, y - 8, x + 8, y + 8);
                gc.strokeLine(x + 8, y - 8, x - 8, y + 8);
            }
        }
    }

    private double xForFrequency(int frequency, double w) {
        double chartWidth = w - leftMargin - rightMargin;

        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] == frequency) {
                return leftMargin + i * (chartWidth / (frequencies.length - 1));
            }
        }

        return leftMargin;
    }

    private double yForDb(int db, double h) {
        double chartHeight = h - topMargin - bottomMargin;
        double minDb = -10;
        double maxDb = 90;

        return topMargin + ((db - minDb) / (maxDb - minDb)) * chartHeight;
    }

    public void setHighlightedFrequency(int frequency) {
        draw();
    }

    private void drawConnectingLines(GraphicsContext gc, double w, double h) {
        drawLineForEar(gc, w, h, Ear.RIGHT, Color.RED);
        drawLineForEar(gc, w, h, Ear.LEFT, Color.BLUE);
    }

    private void drawLineForEar(GraphicsContext gc, double w, double h, Ear ear, Color color) {
        List<AudiogramPoint> earPoints = points.stream()
                .filter(p -> p.getEar() == ear)
                .sorted(Comparator.comparingInt(p -> frequencyIndex(p.getFrequency())))
                .toList();

        if (earPoints.size() < 2) {
            return;
        }

        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.setLineDashes(0);

        for (int i = 0; i < earPoints.size() - 1; i++) {
            AudiogramPoint p1 = earPoints.get(i);
            AudiogramPoint p2 = earPoints.get(i + 1);

            double x1 = xForFrequency(p1.getFrequency(), w);
            double y1 = yForDb(p1.getDbHL(), h);

            double x2 = xForFrequency(p2.getFrequency(), w);
            double y2 = yForDb(p2.getDbHL(), h);

            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    private int frequencyIndex(int frequency) {
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] == frequency) {
                return i;
            }
        }
        return 0;
    }

}