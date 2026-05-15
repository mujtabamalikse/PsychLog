package com.psychlog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class DailyQuestionsScreen {

    private final Stage stage;
    private final User user;

    public DailyQuestionsScreen(Stage stage, User user) {
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
        Label titleLabel = new Label("❓  Daily Check-In");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label dateLabel = new Label(LocalDate.now().toString());
        dateLabel.setFont(Font.font("Segoe UI", 13));
        dateLabel.setTextFill(Color.web(s.getPrimaryText()));

        VBox titleBox = new VBox(4, titleLabel, dateLabel);

        // ── Guest block ────────────────────────────────────────────────────
        if (user.isGuest()) {
            Label guestMsg = new Label(
                    "⚠️  Create an account to access daily check-in questions.");
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

        // ── Detect last mood ───────────────────────────────────────────────
        String lastMood = "Neutral";
        try {
            FileManager fm = new FileManager(
                    user.getDataFolderPath(), user.getPasswordHash());
            List<JournalEntry> entries = fm.loadAllEntries(user.getUsername());
            if (!entries.isEmpty()) {
                entries.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                lastMood = entries.get(0).getMood();
            }
        } catch (Exception ignored) {}

        // ── Get questions based on mood ────────────────────────────────────
        List<String> questions = new QuestionBank().getQuestionsForMood(lastMood);

        // ── Mood context banner ────────────────────────────────────────────
        String moodEmoji = ThemeManager.getMoodEmoji(lastMood);
        Label moodBanner = new Label(moodEmoji + "  Based on your last mood: " + lastMood);
        moodBanner.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        moodBanner.setTextFill(Color.web("#FFFFFF"));
        moodBanner.setPadding(new Insets(10, 16, 10, 16));

        HBox bannerBox = new HBox(moodBanner);
        bannerBox.setAlignment(Pos.CENTER_LEFT);
        bannerBox.setPadding(new Insets(12, 20, 12, 20));
        bannerBox.setStyle(
                "-fx-background-color: " + ThemeManager.getButtonColor(lastMood) + ";" +
                        "-fx-background-radius: 12;");

        // ── Questions Form ─────────────────────────────────────────────────
        VBox questionsBox = new VBox(20);
        TextArea[] answers = new TextArea[questions.size()];

        for (int i = 0; i < questions.size(); i++) {
            Label qLabel = new Label((i + 1) + ".  " + questions.get(i));
            qLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            qLabel.setTextFill(Color.web(s.getPrimaryText()));
            qLabel.setWrapText(true);

            TextArea answerArea = new TextArea();
            answerArea.setPromptText("Your answer...");
            answerArea.setPrefRowCount(3);
            answerArea.setWrapText(true);
            answerArea.setStyle(s.fieldStyle());
            answers[i] = answerArea;

            VBox qBox = new VBox(8, qLabel, answerArea);
            qBox.setPadding(new Insets(16, 20, 16, 20));
            qBox.setStyle(s.cardStyle());
            questionsBox.getChildren().add(qBox);
        }

        // ── Status label ───────────────────────────────────────────────────
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", 13));
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);

        // ── Submit Button ──────────────────────────────────────────────────
        Button submitBtn = new Button("✅  Save My Answers");
        submitBtn.setPrefWidth(220);
        submitBtn.setPrefHeight(44);
        submitBtn.setStyle(primaryButtonStyle());
        submitBtn.setOnMouseEntered(e -> submitBtn.setStyle(primaryButtonHoverStyle()));
        submitBtn.setOnMouseExited(e -> submitBtn.setStyle(primaryButtonStyle()));

        final String finalLastMood = lastMood;

        submitBtn.setOnAction(e -> {
            try {
                StringBuilder combined = new StringBuilder();
                combined.append("=== Daily Check-In: ")
                        .append(LocalDate.now())
                        .append(" ===\n\n");

                boolean anyAnswered = false;
                for (int i = 0; i < questions.size(); i++) {
                    String answer = answers[i].getText().trim();
                    if (!answer.isEmpty()) {
                        combined.append("Q: ").append(questions.get(i))
                                .append("\nA: ").append(answer)
                                .append("\n\n");
                        anyAnswered = true;
                    }
                }

                if (!anyAnswered) {
                    showStatus(statusLabel, "Please answer at least one question.", "#E53935");
                    return;
                }

                String id = java.util.UUID.randomUUID().toString().substring(0, 8);
                JournalEntry entry = new JournalEntry(
                        id,
                        user.getUsername(),
                        combined.toString(),
                        java.time.LocalDateTime.now(),
                        finalLastMood
                );

                FileManager fm = new FileManager(
                        user.getDataFolderPath(), user.getPasswordHash());
                fm.saveJournalEntry(entry);

                showStatus(statusLabel,
                        "✅  Check-in saved! Great job taking care of yourself.", "#388E3C");

                for (TextArea ta : answers) ta.clear();

            } catch (Exception ex) {
                showStatus(statusLabel, "Error saving: " + ex.getMessage(), "#E53935");
                ex.printStackTrace();
            }
        });

        // ── Layout ─────────────────────────────────────────────────────────
        VBox contentBox = new VBox(20, titleBox, bannerBox, questionsBox, statusLabel, submitBtn);
        contentBox.setPadding(new Insets(28));

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + s.getBg() + "; -fx-background-color:" + s.getBg() + ";");

        VBox root = new VBox(topBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");

        return new Scene(root);
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void showStatus(Label label, String message, String color) {
        label.setText(message);
        label.setTextFill(Color.web(color));
        label.setVisible(true);
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