package com.psychlog;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class TrendGraph extends Canvas {

    private final String trajectory;
    private double progress = 0;

    public TrendGraph(String trajectory) {
        super(70, 30);
        this.trajectory = trajectory;
        startAnimation();
    }

    private void startAnimation() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                progress += 0.008;
                if (progress > 1.2) progress = 0;
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
        double midY = h / 2;

        int points = 5;
        double[] xPoints = new double[points];
        double[] yPoints = new double[points];

        for (int i = 0; i < points; i++) {
            xPoints[i] = (w / (points - 1)) * i;
            if (trajectory.contains("Improving")) {
                yPoints[i] = midY + 8 - (16.0 / (points - 1)) * i;
            } else if (trajectory.contains("Declining")) {
                yPoints[i] = midY - 8 + (16.0 / (points - 1)) * i;
            } else {
                yPoints[i] = midY;
            }
        }

        double drawUpTo = progress * w;
        double alpha = Math.min(1.0, progress < 1.0 ? progress + 0.2 : 1.2 - progress + 0.2);

        gc.setStroke(Color.web("#9575CD", Math.max(0, Math.min(1, alpha))));
        gc.setLineWidth(2);
        gc.beginPath();

        for (int i = 0; i < points; i++) {
            if (xPoints[i] > drawUpTo) break;
            if (i == 0) gc.moveTo(xPoints[i], yPoints[i]);
            else gc.lineTo(xPoints[i], yPoints[i]);

            gc.setFill(Color.web("#9575CD", Math.max(0, Math.min(1, alpha))));
            gc.fillOval(xPoints[i] - 3, yPoints[i] - 3, 6, 6);
        }
        gc.stroke();
    }
}