package com.psychlog;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class WellnessGauge extends Canvas {

    private double score;
    private double waveOffset = 0;
    private double bobOffset = 0;
    private double bobDirection = 1;

    public WellnessGauge(double score) {
        super(120, 200);
        this.score = score;
        startAnimation();
    }

    private void startAnimation() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                waveOffset += 0.05;
                bobOffset += 0.03 * bobDirection;
                if (Math.abs(bobOffset) > 3) bobDirection *= -1;
                draw();
            }
        };
        timer.start();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        double w = getWidth();
        double h = getHeight();

        // Water level based on score
        double fillRatio = score / 100.0;
        double waterTop = h - (h * fillRatio);

        // Draw wave
        gc.setFill(Color.web("#9575CD", 0.7));
        gc.beginPath();
        gc.moveTo(0, waterTop);
        for (double x = 0; x <= w; x += 1) {
            double y = waterTop + Math.sin((x * 0.05) + waveOffset) * 4;
            gc.lineTo(x, y);
        }
        gc.lineTo(w, h);
        gc.lineTo(0, h);
        gc.closePath();
        gc.fill();

        // Second wave layer for depth
        gc.setFill(Color.web("#7E57C2", 0.4));
        gc.beginPath();
        gc.moveTo(0, waterTop);
        for (double x = 0; x <= w; x += 1) {
            double y = waterTop + Math.sin((x * 0.07) + waveOffset + 1) * 3;
            gc.lineTo(x, y);
        }
        gc.lineTo(w, h);
        gc.lineTo(0, h);
        gc.closePath();
        gc.fill();

        // Stick figure on water surface
        double figX = w / 2;
        double figY = waterTop + bobOffset;

        gc.setStroke(Color.web("#7E57C2"));
        gc.setLineWidth(2);

        // Head
        gc.strokeOval(figX - 5, figY - 18, 10, 10);
        // Body
        gc.strokeLine(figX, figY - 8, figX, figY + 4);
        // Arms
        gc.strokeLine(figX - 7, figY - 4, figX + 7, figY - 4);
        // Legs
        gc.strokeLine(figX, figY + 4, figX - 5, figY + 12);
        gc.strokeLine(figX, figY + 4, figX + 5, figY + 12);

        // Score number in center
        gc.setFill(Color.web("#FFFFFF"));
        gc.setFont(javafx.scene.text.Font.font("Segoe UI",
                javafx.scene.text.FontWeight.BOLD, 22));
        gc.fillText(String.valueOf((int) score), figX - 13, waterTop + (h - waterTop) / 2 + 8);
    }
}