package com.psychlog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class LoginScreen {

    private final Stage stage;

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {

        AppSettings s = AppSettings.get();

        // ── Title ──────────────────────────────────────────────────────────
        Label appName = new Label("PsychLog");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 38));
        appName.setTextFill(Color.web("#5C6BC0"));

        Label tagline = new Label("Your private mental health companion");
        tagline.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        tagline.setTextFill(Color.web(s.getPrimaryText()));

        VBox titleBox = new VBox(4, appName, tagline);
        titleBox.setAlignment(Pos.CENTER);

        // ── Username field ─────────────────────────────────────────────────
        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("Segoe UI", 13));
        userLabel.setTextFill(Color.web(s.getPrimaryText()));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(42);
        usernameField.setStyle(s.fieldStyle());

        VBox userBox = new VBox(5, userLabel, usernameField);

        // ── Password field ─────────────────────────────────────────────────
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Segoe UI", 13));
        passLabel.setTextFill(Color.web(s.getPrimaryText()));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(42);
        passwordField.setStyle(s.fieldStyle());

        VBox passBox = new VBox(5, passLabel, passwordField);

        // ── Error label ────────────────────────────────────────────────────
        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("Segoe UI", 12));
        errorLabel.setTextFill(Color.web("#E53935"));
        errorLabel.setVisible(false);

        // ── Sign In button ─────────────────────────────────────────────────
        Button signInBtn = new Button("Sign In");
        signInBtn.setPrefWidth(320);
        signInBtn.setPrefHeight(44);
        signInBtn.setStyle(primaryButtonStyle());
        signInBtn.setOnMouseEntered(e -> signInBtn.setStyle(primaryButtonHoverStyle()));
        signInBtn.setOnMouseExited(e -> signInBtn.setStyle(primaryButtonStyle()));

        passwordField.setOnAction(e -> signInBtn.fire());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        signInBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showError(errorLabel, "Please enter both username and password.");
                return;
            }

            User user = FileManager.loadUser(username);

            if (user == null) {
                showError(errorLabel, "No account found with that username.");
                return;
            }

            if (!user.verifyPassword(password)) {
                showError(errorLabel, "Incorrect password. Please try again.");
                return;
            }

            ThemeManager.applyTheme("calm");
            stage.setScene(new DashboardScreen(stage, user).getScene());
        });

        // ── Register button ────────────────────────────────────────────────
        Button registerBtn = new Button("Create Account");
        registerBtn.setPrefWidth(320);
        registerBtn.setPrefHeight(44);
        registerBtn.setStyle(secondaryButtonStyle());
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(secondaryButtonHoverStyle()));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(secondaryButtonStyle()));

        registerBtn.setOnAction(e -> {
            RegisterScreen registerScreen = new RegisterScreen(stage);
            stage.setScene(registerScreen.getScene());
        });

        // ── Guest Mode button ──────────────────────────────────────────────
        Button guestBtn = new Button("Continue as Guest");
        guestBtn.setPrefWidth(320);
        guestBtn.setPrefHeight(38);
        guestBtn.setStyle(ghostButtonStyle());
        guestBtn.setOnMouseEntered(e -> guestBtn.setStyle(ghostButtonHoverStyle()));
        guestBtn.setOnMouseExited(e -> guestBtn.setStyle(ghostButtonStyle()));

        guestBtn.setOnAction(e -> {
            User guestUser = new User("Guest", null, null, true);
            ThemeManager.applyTheme("calm");
            stage.setScene(new DashboardScreen(stage, guestUser).getScene());
        });

        // ── Divider with "or" ──────────────────────────────────────────────
        Label orLabel = new Label("— or —");
        orLabel.setFont(Font.font("Segoe UI", 12));
        orLabel.setTextFill(Color.web(s.getPrimaryText()));

        // ── Card layout ────────────────────────────────────────────────────
        VBox card = new VBox(16,
                titleBox,
                separator(s),
                userBox,
                passBox,
                errorLabel,
                signInBtn,
                registerBtn,
                orLabel,
                guestBtn
        );
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(40, 44, 40, 44));
        card.setMaxWidth(400);
        card.setStyle(s.cardStyle());

        // ── Root ───────────────────────────────────────────────────────────
        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");
        root.setPadding(new Insets(40));

        passwordField.setOnAction(e -> signInBtn.fire());

        return new Scene(root, 520, 680);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }

    private Region separator(AppSettings s) {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color:" + (s.isNightMode() ? "#444444" : "#F0F0F0") + ";");
        return sep;
    }

    // ── Styles ─────────────────────────────────────────────────────────────

    private String primaryButtonStyle() {
        return "-fx-background-color: #5C6BC0;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-cursor: hand;";
    }

    private String primaryButtonHoverStyle() {
        return "-fx-background-color: #3F51B5;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-cursor: hand;";
    }

    private String secondaryButtonStyle() {
        return "-fx-background-color: #EDE7F6;" +
                "-fx-text-fill: #5C6BC0;" +
                "-fx-font-size: 15px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-border-color: #5C6BC0;" +
                "-fx-border-radius: 22;" +
                "-fx-cursor: hand;";
    }

    private String secondaryButtonHoverStyle() {
        return "-fx-background-color: #D1C4E9;" +
                "-fx-text-fill: #3F51B5;" +
                "-fx-font-size: 15px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-border-color: #3F51B5;" +
                "-fx-border-radius: 22;" +
                "-fx-cursor: hand;";
    }

    private String ghostButtonStyle() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #9E9E9E;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 19;" +
                "-fx-border-color: #E0E0E0;" +
                "-fx-border-radius: 19;" +
                "-fx-cursor: hand;";
    }

    private String ghostButtonHoverStyle() {
        return "-fx-background-color: #F5F5F5;" +
                "-fx-text-fill: #757575;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 19;" +
                "-fx-border-color: #BDBDBD;" +
                "-fx-border-radius: 19;" +
                "-fx-cursor: hand;";
    }
}