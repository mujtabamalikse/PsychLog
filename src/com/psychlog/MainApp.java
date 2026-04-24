package com.psychlog;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("PsychLog - Your Safe Space");
        primaryStage.setResizable(false);

        LoginScreen loginScreen = new LoginScreen(primaryStage);
        primaryStage.setScene(loginScreen.getScene());

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}