package com.psychlog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class RegisterScreen {

    private final Stage stage;

    public RegisterScreen(Stage stage) {
        this.stage = stage;
    }

    public Scene getScene() {

        AppSettings s = AppSettings.get();

        // ── Title ──────────────────────────────────────────────────────────
        Label title = new Label("Create Account");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        title.setTextFill(Color.web("#5C6BC0"));

        Label subtitle = new Label("Set up your private PsychLog");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web(s.getPrimaryText()));

        VBox titleBox = new VBox(4, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // ── Username ───────────────────────────────────────────────────────
        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("Segoe UI", 13));
        userLabel.setTextFill(Color.web(s.getPrimaryText()));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setPrefHeight(42);
        usernameField.setStyle(s.fieldStyle());

        VBox userBox = new VBox(5, userLabel, usernameField);

        // ── Password ───────────────────────────────────────────────────────
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("Segoe UI", 13));
        passLabel.setTextFill(Color.web(s.getPrimaryText()));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.setPrefHeight(42);
        passwordField.setStyle(s.fieldStyle());

        VBox passBox = new VBox(5, passLabel, passwordField);

        // ── Confirm Password ───────────────────────────────────────────────
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setFont(Font.font("Segoe UI", 13));
        confirmLabel.setTextFill(Color.web(s.getPrimaryText()));

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Repeat your password");
        confirmField.setPrefHeight(42);
        confirmField.setStyle(s.fieldStyle());

        VBox confirmBox = new VBox(5, confirmLabel, confirmField);

        // ── Folder Picker ──────────────────────────────────────────────────
        Label folderLabel = new Label("Data Storage Folder");
        folderLabel.setFont(Font.font("Segoe UI", 13));
        folderLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label folderPathLabel = new Label("No folder selected");
        folderPathLabel.setFont(Font.font("Segoe UI", 11));
        folderPathLabel.setTextFill(Color.web(s.getPrimaryText()));
        folderPathLabel.setWrapText(true);

        Button browseBtn = new Button("Choose Folder");
        browseBtn.setStyle(secondaryButtonStyle());
        browseBtn.setPrefHeight(38);
        browseBtn.setOnMouseEntered(e -> browseBtn.setStyle(secondaryButtonHoverStyle()));
        browseBtn.setOnMouseExited(e -> browseBtn.setStyle(secondaryButtonStyle()));

        final String[] selectedFolder = {null};

        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose where to store your PsychLog data");
            File chosen = chooser.showDialog(stage);
            if (chosen != null) {
                selectedFolder[0] = chosen.getAbsolutePath();
                folderPathLabel.setText(chosen.getAbsolutePath());
                folderPathLabel.setTextFill(Color.web("#5C6BC0"));
            }
        });

        VBox folderBox = new VBox(6, folderLabel, browseBtn, folderPathLabel);

        // ── Error label ────────────────────────────────────────────────────
        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("Segoe UI", 12));
        errorLabel.setTextFill(Color.web("#E53935"));
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // ── Register button ────────────────────────────────────────────────
        Button registerBtn = new Button("Create My Account");
        registerBtn.setPrefWidth(320);
        registerBtn.setPrefHeight(44);
        registerBtn.setStyle(primaryButtonStyle());
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(primaryButtonHoverStyle()));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(primaryButtonStyle()));

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm = confirmField.getText();

            if (username.isEmpty()) {
                showError(errorLabel, "Please enter a username.");
                return;
            }
            if (username.length() < 3) {
                showError(errorLabel, "Username must be at least 3 characters.");
                return;
            }
            if (password.isEmpty()) {
                showError(errorLabel, "Please enter a password.");
                return;
            }
            if (password.length() < 6) {
                showError(errorLabel, "Password must be at least 6 characters.");
                return;
            }
            if (!password.equals(confirm)) {
                showError(errorLabel, "Passwords do not match.");
                return;
            }
            if (selectedFolder[0] == null) {
                showError(errorLabel, "Please choose a folder to store your data.");
                return;
            }

            try {
                User newUser = new User(username, password, selectedFolder[0]);
                FileManager.saveUserIndex(newUser);
                FileManager fm = new FileManager(selectedFolder[0], password);
                fm.saveUser(newUser);
                ThemeManager.applyTheme("calm");
                stage.setScene(new DashboardScreen(stage, newUser).getScene());

            } catch (Exception ex) {
                showError(errorLabel, "Error creating account: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // ── Back to Login ──────────────────────────────────────────────────
        Button backBtn = new Button("Back to Login");
        backBtn.setPrefWidth(320);
        backBtn.setPrefHeight(38);
        backBtn.setStyle(ghostButtonStyle());
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(ghostButtonHoverStyle()));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(ghostButtonStyle()));

        backBtn.setOnAction(e -> {
            LoginScreen loginScreen = new LoginScreen(stage);
            stage.setScene(loginScreen.getScene());
        });

        // ── Card ───────────────────────────────────────────────────────────
        VBox card = new VBox(16,
                titleBox,
                separator(s),
                userBox,
                passBox,
                confirmBox,
                folderBox,
                errorLabel,
                registerBtn,
                backBtn
        );
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(40, 44, 40, 44));
        card.setMaxWidth(420);
        card.setStyle(s.cardStyle());

        // ── Root ───────────────────────────────────────────────────────────
        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");
        root.setPadding(new Insets(40));

        return new Scene(root, 520, 750);
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
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #5C6BC0;" +
                "-fx-border-radius: 18;" +
                "-fx-cursor: hand;";
    }

    private String secondaryButtonHoverStyle() {
        return "-fx-background-color: #D1C4E9;" +
                "-fx-text-fill: #3F51B5;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #3F51B5;" +
                "-fx-border-radius: 18;" +
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