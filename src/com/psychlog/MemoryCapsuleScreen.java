package com.psychlog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.util.UUID;

public class MemoryCapsuleScreen {
    private final Stage stage;
    private final User user;

    public MemoryCapsuleScreen(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public Scene getScene() {
        AppSettings s = AppSettings.get();

        // ── Top Bar ────────────────────────────────────────────────────────
        Label appName = new Label("PsychLog");
        appName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        appName.setTextFill(Color.web("#FFFFFF"));
        appName.setStyle("-fx-cursor: hand;");
        appName.setOnMouseClicked(e ->
                stage.setScene(new DashboardScreen(stage, user).getScene()));

        Button backBtn = new Button("← Dashboard");
        backBtn.setStyle(ghostButtonStyle());
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(ghostButtonHoverStyle()));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(ghostButtonStyle()));
        backBtn.setOnAction(e ->
                stage.setScene(new DashboardScreen(stage, user).getScene()));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, backBtn, topSpacer, appName);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 28, 16, 28));
        topBar.setStyle("-fx-background-color:" + s.getTopBarBg() + ";");

        // ── Title ──────────────────────────────────────────────────────────
        Label titleLabel = new Label("🎁  Memory Capsule");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label subtitleLabel = new Label("Write a message to your future self");
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web(s.getSecondaryText()));
        subtitleLabel.setWrapText(true);

        VBox titleBox = new VBox(6, titleLabel, subtitleLabel);

        // ── Guest check ────────────────────────────────────────────────────
        if (user.isGuest()) {
            Label guestMsg = new Label(
                    "⚠️  Create an account to use Memory Capsule.");
            guestMsg.setFont(Font.font("Segoe UI", 14));
            guestMsg.setTextFill(Color.web("#F57F17"));
            guestMsg.setWrapText(true);
            guestMsg.setStyle(
                    "-fx-background-color: #FFF9C4;" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 16;");

            VBox contentBox = new VBox(20, titleBox, guestMsg);
            contentBox.setPadding(new Insets(28));

            ScrollPane scroll = new ScrollPane(contentBox);
            scroll.setFitToWidth(true);
            scroll.setStyle("-fx-background:" + s.getBg() + "; -fx-background-color:" + s.getBg() + ";");

            VBox root = new VBox(topBar, scroll);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            root.setStyle("-fx-background-color:" + s.getBg() + ";");
            return new Scene(root);
        }

        // ── Message input ──────────────────────────────────────────────────
        Label messageLabel = new Label("Your Message:");
        messageLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        messageLabel.setTextFill(Color.web(s.getPrimaryText()));

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Write a message to yourself in the future...");
        messageArea.setPrefRowCount(8);
        messageArea.setWrapText(true);
        messageArea.setStyle(s.fieldStyle() + "-fx-pref-row-count: 8;");

        VBox messageBox = new VBox(6, messageLabel, messageArea);

        // ── Days until delivery ────────────────────────────────────────────
        Label daysLabel = new Label("Deliver in how many days?");
        daysLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        daysLabel.setTextFill(Color.web(s.getPrimaryText()));

        Spinner<Integer> daysSpinner = new Spinner<>(1, 365, 30);
        daysSpinner.setPrefWidth(120);
        daysSpinner.setStyle(s.fieldStyle());

        VBox daysBox = new VBox(6, daysLabel, daysSpinner);

        // ── Status ─────────────────────────────────────────────────────────
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);

        // ── Save button ────────────────────────────────────────────────────
        Button saveBtn = new Button("📮  Create Capsule");
        saveBtn.setPrefHeight(44);
        saveBtn.setStyle(primaryButtonStyle());
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(primaryButtonHoverStyle()));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(primaryButtonStyle()));

        saveBtn.setOnAction(e -> {
            String message = messageArea.getText().trim();
            if (message.isEmpty()) {
                showStatus(statusLabel, "Please write a message.", "#E53935");
                return;
            }

            try {
                int days = daysSpinner.getValue();
                LocalDateTime deliverAt = LocalDateTime.now().plusDays(days);

                String id = UUID.randomUUID().toString().substring(0, 8);
                String content = "🎁 MEMORY CAPSULE (Deliver: " + deliverAt.toLocalDate() + ")\n\n" + message;
                JournalEntry entry = new JournalEntry(
                        id,
                        user.getUsername(),
                        content,
                        LocalDateTime.now(),
                        "Capsule"
                );

                FileManager fm = new FileManager(
                        user.getDataFolderPath(),
                        user.getPasswordHash());
                fm.saveJournalEntry(entry);

                showStatus(statusLabel, "✅  Capsule created! Delivery: " + deliverAt.toLocalDate(), "#388E3C");
                messageArea.clear();
                daysSpinner.getValueFactory().setValue(30);

            } catch (Exception ex) {
                showStatus(statusLabel, "Error: " + ex.getMessage(), "#E53935");
                ex.printStackTrace();
            }
        });

        VBox card = new VBox(16,
                messageBox,
                daysBox,
                saveBtn,
                statusLabel
        );
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setStyle(s.cardStyle());

        VBox contentBox = new VBox(20, titleBox, card);
        contentBox.setPadding(new Insets(28));

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + s.getBg() + "; -fx-background-color:" + s.getBg() + ";");

        VBox root = new VBox(topBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");

        return new Scene(root);
    }

    private void showStatus(Label label, String message, String color) {
        label.setText(message);
        label.setTextFill(Color.web(color));
        label.setVisible(true);
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: #5C6BC0;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-cursor: hand;";
    }

    private String primaryButtonHoverStyle() {
        return "-fx-background-color: #3F51B5;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-cursor: hand;";
    }

    private String ghostButtonStyle() {
        return "-fx-background-color: transparent;" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 19;" +
                "-fx-border-color: rgba(255,255,255,0.5);" +
                "-fx-border-radius: 19;" +
                "-fx-cursor: hand;";
    }

    private String ghostButtonHoverStyle() {
        return "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-background-radius: 19;" +
                "-fx-border-color: rgba(255,255,255,0.8);" +
                "-fx-border-radius: 19;" +
                "-fx-cursor: hand;";
    }
}