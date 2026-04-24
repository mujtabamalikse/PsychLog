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
import java.util.List;

public class ViewEntriesScreen {

    private final Stage stage;
    private final User user;

    public ViewEntriesScreen(Stage stage, User user) {
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
        topBar.setStyle("-fx-background-color: #5C6BC0;");

        // ── Title ──────────────────────────────────────────────────────────
        Label titleLabel = new Label("📖  Your Journal Entries");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(s.getPrimaryText()));

        // ── Load Entries ───────────────────────────────────────────────────
        VBox entriesBox = new VBox(16);
        entriesBox.setPadding(new Insets(4));

        if (user.isGuest()) {
            Label guestMsg = new Label(
                    "⚠️  You're in guest mode. Create an account to save and view entries.");
            guestMsg.setFont(Font.font("Segoe UI", 14));
            guestMsg.setTextFill(Color.web("#F57F17"));
            guestMsg.setWrapText(true);
            guestMsg.setStyle(
                    "-fx-background-color: #FFF9C4;" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 16;");
            entriesBox.getChildren().add(guestMsg);

        } else {
            try {
                FileManager fm = new FileManager(
                        user.getDataFolderPath(),
                        user.getPasswordHash());
                List<JournalEntry> entries =
                        fm.loadAllEntries(user.getUsername());

                if (entries.isEmpty()) {
                    Label emptyMsg = new Label(
                            "✨  No entries yet. Start writing your first journal entry!");
                    emptyMsg.setFont(Font.font("Segoe UI", 14));
                    emptyMsg.setTextFill(Color.web(s.getPrimaryText()));
                    emptyMsg.setWrapText(true);
                    emptyMsg.setStyle(s.cardStyle() + "-fx-padding: 24;");
                    entriesBox.getChildren().add(emptyMsg);

                } else {
                    entries.sort((a, b) ->
                            b.getTimestamp().compareTo(a.getTimestamp()));

                    for (JournalEntry entry : entries) {
                        entriesBox.getChildren().add(
                                makeEntryCard(entry, s));
                    }
                }

            } catch (Exception ex) {
                Label errorMsg = new Label(
                        "Error loading entries: " + ex.getMessage());
                errorMsg.setTextFill(Color.web("#E53935"));
                entriesBox.getChildren().add(errorMsg);
                ex.printStackTrace();
            }
        }

        // ── Content ────────────────────────────────────────────────────────
        VBox contentBox = new VBox(16, titleLabel, entriesBox);
        contentBox.setPadding(new Insets(28));
        contentBox.setStyle("-fx-background-color: " + s.getBg() + ";");

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background: " + s.getBg() + ";" +
                        "-fx-background-color: " + s.getBg() + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // ── Root ───────────────────────────────────────────────────────────
        VBox root = new VBox(topBar, scroll);
        root.setStyle("-fx-background-color: " + s.getBg() + ";");

        return new Scene(root);
    }

    // ── Entry Card Builder ─────────────────────────────────────────────────
    private VBox makeEntryCard(JournalEntry entry, AppSettings s) {

        String mood = entry.getMood();
        String bgColor = ThemeManager.getCardColor(mood);
        String textColor = ThemeManager.getTextColor(mood);
        String emoji = ThemeManager.getMoodEmoji(mood);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                "EEEE, MMM d yyyy  •  hh:mm a");
        Label dateLabel = new Label(entry.getTimestamp().format(fmt));
        dateLabel.setFont(Font.font("Segoe UI", 12));
        dateLabel.setTextFill(Color.web(s.getMutedText()));

        Label moodBadge = new Label(emoji + "  " + mood);
        moodBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        moodBadge.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 4 12 4 12;" +
                        "-fx-text-fill: " + textColor + ";");

        Region badgeSpacer = new Region();
        HBox.setHgrow(badgeSpacer, Priority.ALWAYS);

        HBox topRow = new HBox(8, dateLabel, badgeSpacer, moodBadge);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String content = entry.getContent();
        String preview = content.length() > 300
                ? content.substring(0, 300) + "..."
                : content;

        Label contentLabel = new Label(preview);
        contentLabel.setFont(Font.font("Segoe UI", 14));
        contentLabel.setTextFill(Color.web(s.getPrimaryText()));
        contentLabel.setWrapText(true);

        VBox cardContent = new VBox(10, topRow, separator(s), contentLabel);

        if (content.length() > 300) {
            Button expandBtn = new Button("Read more");
            expandBtn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #5C6BC0;" +
                            "-fx-font-size: 12px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0;");

            final boolean[] expanded = {false};
            expandBtn.setOnAction(e -> {
                if (!expanded[0]) {
                    contentLabel.setText(content);
                    expandBtn.setText("Show less");
                    expanded[0] = true;
                } else {
                    contentLabel.setText(preview);
                    expandBtn.setText("Read more");
                    expanded[0] = false;
                }
            });
            cardContent.getChildren().add(expandBtn);
        }

        VBox card = new VBox(cardContent);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle(s.cardStyle());

        return card;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private Region separator(AppSettings s) {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " +
                (s.isNightMode() ? "#444444" : "#F0F0F0") + ";");
        return sep;
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