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

public class WriteEntryScreen {

    private final Stage stage;
    private final User user;
    private final Analyzer analyzer = new Analyzer();

    public WriteEntryScreen(Stage stage, User user) {
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

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, backBtn, spacer, appName);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(16, 28, 16, 28));
        topBar.setStyle("-fx-background-color:" + s.getTopBarBg() + ";");

        // ── Title ──────────────────────────────────────────────────────────
        Label titleLabel = new Label("✏️  Write a Journal Entry");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label dateLabel = new Label(java.time.LocalDate.now().toString());
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(Color.web(s.getPrimaryText()));

        VBox titleBox = new VBox(4, titleLabel, dateLabel);

        // ── Text Area ──────────────────────────────────────────────────────
        Label promptLabel = new Label("How are you feeling today?");
        promptLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        promptLabel.setTextFill(Color.web("#5C6BC0"));

        TextArea journalArea = new TextArea();
        journalArea.setPromptText(
                "Write freely... this is your safe space. " +
                        "No one else can read this.");
        journalArea.setPrefRowCount(12);
        journalArea.setWrapText(true);
        journalArea.setStyle(s.fieldStyle() +
                "-fx-pref-row-count: 12;");

        // ── Mood Display ───────────────────────────────────────────────────
        Label moodTitleLabel = new Label("Detected Mood:");
        moodTitleLabel.setFont(Font.font("Segoe UI", 13));
        moodTitleLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label moodValueLabel = new Label("😐  Neutral");
        moodValueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        moodValueLabel.setTextFill(Color.web("#5C6BC0"));
        moodValueLabel.setStyle(
                "-fx-background-color: #EDE7F6;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 6 16 6 16;");

        HBox moodBox = new HBox(10, moodTitleLabel, moodValueLabel);
        moodBox.setAlignment(Pos.CENTER_LEFT);

        // ── Live mood detection ────────────────────────────────────────────
        journalArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                moodValueLabel.setText("😐  Neutral");
                moodValueLabel.setStyle(
                        "-fx-background-color: #EDE7F6;" +
                                "-fx-background-radius: 20;" +
                                "-fx-padding: 6 16 6 16;");
                return;
            }
            String mood = analyzer.detectMood(newVal);
            String emoji = ThemeManager.getMoodEmoji(mood);
            String color = ThemeManager.getCardColor(mood);
            String textColor = ThemeManager.getTextColor(mood);
            moodValueLabel.setText(emoji + "  " + mood);
            moodValueLabel.setStyle(
                    "-fx-background-color: " + color + ";" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 6 16 6 16;" +
                            "-fx-text-fill: " + textColor + ";");
        });

        // ── Character count ────────────────────────────────────────────────
        Label charCount = new Label("0 characters");
        charCount.setFont(Font.font("Segoe UI", 11));
        charCount.setTextFill(Color.web(s.getPrimaryText()));

        journalArea.textProperty().addListener((obs, oldVal, newVal) ->
                charCount.setText(newVal.length() + " characters"));

        // ── Status label ───────────────────────────────────────────────────
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setVisible(false);

        // ── Save Button ────────────────────────────────────────────────────
        Button saveBtn = new Button("💾  Save Entry");
        saveBtn.setPrefWidth(200);
        saveBtn.setPrefHeight(44);
        saveBtn.setStyle(primaryButtonStyle());
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(primaryButtonHoverStyle()));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(primaryButtonStyle()));

        saveBtn.setOnAction(e -> {
            String content = journalArea.getText().trim();

            if (content.isEmpty()) {
                showStatus(statusLabel, "Please write something before saving.", "#E53935");
                return;
            }

            if (user.isGuest()) {
                showStatus(statusLabel,
                        "⚠️ Guest mode — entries are not saved. Create an account!",
                        "#F57F17");
                return;
            }

            try {
                String mood = analyzer.detectMood(content);
                String id = UUID.randomUUID().toString().substring(0, 8);
                JournalEntry entry = new JournalEntry(
                        id,
                        user.getUsername(),
                        content,
                        LocalDateTime.now(),
                        mood
                );

                FileManager fm = new FileManager(
                        user.getDataFolderPath(),
                        user.getPasswordHash());
                fm.saveJournalEntry(entry);

                DarkEntryDetector detector = new DarkEntryDetector();
                DarkEntryDetector.Level level = detector.detect(content);

                if (level == DarkEntryDetector.Level.NONE) {
                    showStatus(statusLabel, "✅  Entry saved successfully!", "#388E3C");
                } else if (level == DarkEntryDetector.Level.DARK) {
                    showStatus(statusLabel, detector.getResponse(level), "#7B1FA2");
                } else if (level == DarkEntryDetector.Level.CRISIS) {
                    showStatus(statusLabel, detector.getResponse(level), "#C62828");
                }

                journalArea.clear();

            } catch (Exception ex) {
                showStatus(statusLabel,
                        "Error saving entry: " + ex.getMessage(), "#E53935");
                ex.printStackTrace();
            }
        });

        // ── Clear Button ───────────────────────────────────────────────────
        Button clearBtn = new Button("Clear");
        clearBtn.setPrefHeight(44);
        clearBtn.setStyle(secondaryButtonStyle());
        clearBtn.setOnMouseEntered(e -> clearBtn.setStyle(secondaryButtonHoverStyle()));
        clearBtn.setOnMouseExited(e -> clearBtn.setStyle(secondaryButtonStyle()));
        clearBtn.setOnAction(e -> {
            journalArea.clear();
            statusLabel.setVisible(false);
        });

        HBox buttonRow = new HBox(12, saveBtn, clearBtn);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        // ── Card ───────────────────────────────────────────────────────────
        VBox card = new VBox(16,
                titleBox,
                separator(s),
                promptLabel,
                journalArea,
                moodBox,
                charCount,
                buttonRow,
                statusLabel
        );
        card.setPadding(new Insets(32, 36, 32, 36));
        card.setStyle(s.cardStyle());
        card.setMaxWidth(720);

        ScrollPane scroll = new ScrollPane(card);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + s.getBg() +
                "; -fx-background-color:" + s.getBg() + ";");
        scroll.setPadding(new Insets(28));

        // ── Root ───────────────────────────────────────────────────────────
        VBox root = new VBox(topBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");

        // FIX: no hardcoded size — dashboard controls the window size
        return new Scene(root);
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void showStatus(Label label, String message, String color) {
        label.setText(message);
        label.setTextFill(Color.web(color));
        label.setVisible(true);
    }

    private Region separator(AppSettings s) {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color:" +
                (s.isNightMode() ? "#444444" : "#F0F0F0") + ";");
        return sep;
    }

    // ── Styles ─────────────────────────────────────────────────────────────
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

    private String secondaryButtonStyle() {
        return "-fx-background-color: #EDE7F6;" +
                "-fx-text-fill: #5C6BC0;" +
                "-fx-font-size: 14px;" +
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
                "-fx-font-size: 14px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 22;" +
                "-fx-border-color: #3F51B5;" +
                "-fx-border-radius: 22;" +
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