package com.psychlog;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class RippleButton extends StackPane {

    private final Button button;

    public RippleButton(Button button) {
        this.button = button;
        setMaxWidth(button.getPrefWidth());
        getChildren().add(button);
        setClip(null);

        button.setOnMousePressed(e -> {
            if (!AppSettings.get().isRippleEnabled()) return;

            Circle ripple = new Circle(0);
            ripple.setFill(Color.web("#FFFFFF", 0.25));
            ripple.setCenterX(e.getX());
            ripple.setCenterY(e.getY());
            ripple.setMouseTransparent(true);

            getChildren().add(ripple);

            ScaleTransition scale = new ScaleTransition(Duration.millis(400), ripple);
            scale.setToX(60);
            scale.setToY(60);

            FadeTransition fade = new FadeTransition(Duration.millis(400), ripple);
            fade.setFromValue(0.4);
            fade.setToValue(0);

            ParallelTransition pt = new ParallelTransition(scale, fade);
            pt.setOnFinished(ev -> getChildren().remove(ripple));
            pt.play();
        });
    }

    public Button getButton() {
        return button;
    }
}