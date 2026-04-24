package com.psychlog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class AnalyticsScreen {

    private final Stage stage;
    private final User user;
    private final Analyzer analyzer = new Analyzer();

    public AnalyticsScreen(Stage stage, User user) {
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
        Label titleLabel = new Label("📊  Your Mood Analytics");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(s.getPrimaryText()));

        // ── Load Data ──────────────────────────────────────────────────────
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(28));
        contentBox.getChildren().add(titleLabel);

        if (user.isGuest()) {
            Label guestMsg = new Label(
                    "⚠️  Create an account to track your mood analytics.");
            guestMsg.setFont(Font.font("Segoe UI", 14));
            guestMsg.setTextFill(Color.web("#F57F17"));
            guestMsg.setWrapText(true);
            guestMsg.setStyle(
                    "-fx-background-color: #FFF9C4;" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 16;");
            contentBox.getChildren().add(guestMsg);

        } else {
            try {
                FileManager fm = new FileManager(
                        user.getDataFolderPath(),
                        user.getPasswordHash());
                List<JournalEntry> entries =
                        fm.loadAllEntries(user.getUsername());

                if (entries.isEmpty()) {
                    Label emptyMsg = new Label(
                            "✨  No data yet. Write some journal entries to see your analytics!");
                    emptyMsg.setFont(Font.font("Segoe UI", 14));
                    emptyMsg.setTextFill(Color.web(s.getPrimaryText()));
                    emptyMsg.setWrapText(true);
                    emptyMsg.setStyle(s.cardStyle() + "-fx-padding: 24;");
                    contentBox.getChildren().add(emptyMsg);

                } else {
                    contentBox.getChildren().add(makeStatsRow(entries, s));
                    contentBox.getChildren().add(makeSectionTitle("Mood Distribution", s));
                    contentBox.getChildren().add(makeMoodBarChart(entries, s));
                    contentBox.getChildren().add(makeSectionTitle("Streak Info", s));
                    contentBox.getChildren().add(makeStreakCard(entries, s));
                    contentBox.getChildren().add(makeSectionTitle("Recent Mood Timeline", s));
                    contentBox.getChildren().add(makeMoodTimeline(entries, s));
                }

            } catch (Exception ex) {
                Label errorMsg = new Label(
                        "Error loading analytics: " + ex.getMessage());
                errorMsg.setTextFill(Color.web("#E53935"));
                contentBox.getChildren().add(errorMsg);
                ex.printStackTrace();
            }
        }

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + s.getBg() + "; -fx-background-color:" + s.getBg() + ";");

        VBox root = new VBox(topBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");

        return new Scene(root);
    }

    // ── Stats Row ──────────────────────────────────────────────────────────
    private HBox makeStatsRow(List<JournalEntry> entries, AppSettings s) {
        StreakTracker tracker = new StreakTracker(entries);

        VBox totalCard   = makeStatCard("📝", String.valueOf(tracker.getTotalEntries()), "Total Entries", s);
        VBox streakCard  = makeStatCard("🔥", String.valueOf(tracker.getCurrentStreak()), "Current Streak", s);
        VBox longestCard = makeStatCard("🏆", String.valueOf(tracker.getLongestStreak()), "Longest Streak", s);

        Map<String, Long> dist = analyzer.getMoodDistribution(entries);
        String topMood = dist.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
        VBox moodCard = makeStatCard(ThemeManager.getMoodEmoji(topMood), topMood, "Top Mood", s);

        HBox row = new HBox(16, totalCard, streakCard, longestCard, moodCard);
        row.setAlignment(Pos.CENTER_LEFT);
        for (javafx.scene.Node n : row.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        return row;
    }

    private VBox makeStatCard(String emoji, String value, String label, AppSettings s) {
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(28));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.web("#5C6BC0"));

        Label labelLabel = new Label(label);
        labelLabel.setFont(Font.font("Segoe UI", 12));
        labelLabel.setTextFill(Color.web(s.getPrimaryText()));

        VBox card = new VBox(6, emojiLabel, valueLabel, labelLabel);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(s.cardStyle());
        return card;
    }

    // ── Mood Bar Chart ─────────────────────────────────────────────────────
    private VBox makeMoodBarChart(List<JournalEntry> entries, AppSettings s) {
        Map<String, Long> dist = analyzer.getMoodDistribution(entries);
        long maxVal = dist.values().stream().max(Long::compareTo).orElse(1L);

        VBox bars = new VBox(10);
        bars.setPadding(new Insets(16));

        String[] moodOrder = {"Happy", "Calm", "Anxious", "Sad", "Angry", "Neutral"};

        for (String mood : moodOrder) {
            long count = dist.getOrDefault(mood, 0L);
            double pct = (double) count / maxVal;

            Label moodLabel = new Label(ThemeManager.getMoodEmoji(mood) + "  " + mood);
            moodLabel.setFont(Font.font("Segoe UI", 13));
            moodLabel.setTextFill(Color.web(s.getPrimaryText()));
            moodLabel.setPrefWidth(100);

            Region bar = new Region();
            bar.setPrefHeight(28);
            bar.setPrefWidth(pct * 400);
            bar.setMinWidth(4);
            bar.setStyle("-fx-background-color: " + ThemeManager.getButtonColor(mood) + ";" +
                    "-fx-background-radius: 6;");

            Label countLabel = new Label(String.valueOf(count));
            countLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            countLabel.setTextFill(Color.web(s.getPrimaryText()));

            HBox row = new HBox(12, moodLabel, bar, countLabel);
            row.setAlignment(Pos.CENTER_LEFT);
            bars.getChildren().add(row);
        }

        VBox card = new VBox(bars);
        card.setStyle(s.cardStyle());
        return card;
    }

    // ── Streak Card ────────────────────────────────────────────────────────
    private HBox makeStreakCard(List<JournalEntry> entries, AppSettings s) {
        StreakTracker tracker = new StreakTracker(entries);

        Label currentLabel = new Label("🔥  Current streak: " + tracker.getCurrentStreak() + " days");
        currentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        currentLabel.setTextFill(Color.web("#5C6BC0"));

        Label longestLabel = new Label("🏆  Longest streak: " + tracker.getLongestStreak() + " days");
        longestLabel.setFont(Font.font("Segoe UI", 14));
        longestLabel.setTextFill(Color.web(s.getPrimaryText()));

        String todayText = tracker.hasWrittenToday()
                ? "✅  You've written today — great job!"
                : "📝  You haven't written today yet. Keep your streak alive!";
        Label todayLabel = new Label(todayText);
        todayLabel.setFont(Font.font("Segoe UI", 13));
        todayLabel.setTextFill(tracker.hasWrittenToday()
                ? Color.web("#388E3C") : Color.web("#F57F17"));

        VBox info = new VBox(10, currentLabel, longestLabel, todayLabel);
        info.setPadding(new Insets(20));
        info.setStyle(s.cardStyle());
        HBox.setHgrow(info, Priority.ALWAYS);

        return new HBox(info);
    }

    // ── Mood Timeline ──────────────────────────────────────────────────────
    private VBox makeMoodTimeline(List<JournalEntry> entries, AppSettings s) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");

        List<JournalEntry> recent = new ArrayList<>(entries);
        recent.sort(Comparator.comparing(JournalEntry::getTimestamp));
        if (recent.size() > 10) {
            recent = recent.subList(recent.size() - 10, recent.size());
        }

        HBox timeline = new HBox(12);
        timeline.setAlignment(Pos.BOTTOM_CENTER);
        timeline.setPadding(new Insets(16, 16, 8, 16));

        for (JournalEntry entry : recent) {
            String mood  = entry.getMood();
            String color = ThemeManager.getButtonColor(mood);
            String emoji = ThemeManager.getMoodEmoji(mood);

            Label emojiLabel = new Label(emoji);
            emojiLabel.setFont(Font.font(20));
            emojiLabel.setAlignment(Pos.CENTER);

            Label dateLabel = new Label(entry.getTimestamp().format(fmt));
            dateLabel.setFont(Font.font("Segoe UI", 10));
            dateLabel.setTextFill(Color.web(s.getPrimaryText()));

            Region dot = new Region();
            dot.setPrefSize(14, 14);
            dot.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 7;");

            VBox col = new VBox(4, emojiLabel, dot, dateLabel);
            col.setAlignment(Pos.CENTER);
            timeline.getChildren().add(col);
        }

        Label timelineTitle = new Label("Last " + recent.size() + " entries");
        timelineTitle.setFont(Font.font("Segoe UI", 11));
        timelineTitle.setTextFill(Color.web(s.getPrimaryText()));
        timelineTitle.setPadding(new Insets(0, 0, 0, 16));

        VBox card = new VBox(timeline, timelineTitle);
        card.setStyle(s.cardStyle());
        return card;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private Label makeSectionTitle(String text, AppSettings s) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        label.setTextFill(Color.web("#5C6BC0"));
        return label;
    }

    // ── Styles ─────────────────────────────────────────────────────────────
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