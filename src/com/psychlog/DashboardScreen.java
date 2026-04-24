package com.psychlog;

import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.util.ArrayList;

import java.util.List;

public class DashboardScreen {

    private final Stage stage;
    private final User user;
    private final Analyzer analyzer = new Analyzer();
    private boolean fromModeSwitch = false;

    private static final java.util.Map<String, String> MOOD_QUOTES =
            new java.util.HashMap<>();
    static {
        MOOD_QUOTES.put("Happy",   "Keep shining — your joy is contagious. ☀️");
        MOOD_QUOTES.put("Sad",     "It's okay not to be okay. Tomorrow holds new light. 🌧️");
        MOOD_QUOTES.put("Angry",   "Breathe. This feeling will pass. You are stronger than it. 🔥");
        MOOD_QUOTES.put("Anxious", "One moment at a time. You've survived every hard day so far. 🌿");
        MOOD_QUOTES.put("Calm",    "Peace lives inside you. Carry it gently. 🕊️");
        MOOD_QUOTES.put("Neutral", "Every day is a fresh page. Write it well. 📖");
    }

    public DashboardScreen(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    private void navigateTo(Scene scene) {
        double w = stage.getWidth();
        double h = stage.getHeight();
        stage.setScene(scene);
        stage.setWidth(w);
        stage.setHeight(h);
    }

    public Scene getScene() {

        AppSettings s = AppSettings.get();
        VBox insightCard = new VBox();

        // ── Top Bar ────────────────────────────────────────────────────────
        Label appTitle = new Label("🧠  PsychLog");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        appTitle.setTextFill(Color.web("#FFFFFF"));
        appTitle.setStyle("-fx-cursor: hand;");
        appTitle.setOnMouseClicked(e ->
                stage.setScene(new DashboardScreen(stage, user).getScene()));

        Button nightBtn = new Button(s.isNightMode() ? "☀  Light" : "🌙  Dark");
        nightBtn.setStyle(topBarBtnStyle());
        nightBtn.setOnMouseEntered(e -> nightBtn.setStyle(topBarBtnHoverStyle()));
        nightBtn.setOnMouseExited(e -> nightBtn.setStyle(topBarBtnStyle()));
        nightBtn.setOnAction(e -> {
            s.setNightMode(!s.isNightMode());
            navigateTo(new DashboardScreen(stage, user).getSceneNoFade());
        });



        Button settingsBtn = new Button("⚙  Settings");
        settingsBtn.setStyle(topBarBtnStyle());
        settingsBtn.setOnMouseEntered(e -> settingsBtn.setStyle(topBarBtnHoverStyle()));
        settingsBtn.setOnMouseExited(e -> settingsBtn.setStyle(topBarBtnStyle()));
        settingsBtn.setOnAction(e ->
                navigateTo(new SettingsScreen(stage, user).getScene()));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Button logoutBtn = new Button("🚪  Logout");
        logoutBtn.setStyle(topBarBtnStyle());
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(topBarBtnHoverStyle()));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(topBarBtnStyle()));
        logoutBtn.setOnAction(e ->
                stage.setScene(new LoginScreen(stage).getScene()));

        HBox topBar = new HBox(10,
                appTitle, topSpacer, nightBtn, settingsBtn, logoutBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(14, 20, 14, 20));
        topBar.setStyle("-fx-background-color: #5C6BC0;");

        // ── Streak Banner ──────────────────────────────────────────────────
        int streak = 0;
        if (!user.isGuest()) {
            try {
                FileManager fm = new FileManager(
                        user.getDataFolderPath(),
                        user.getPasswordHash());
                List<JournalEntry> entries =
                        fm.loadAllEntries(user.getUsername());
                streak = new StreakTracker(entries).getCurrentStreak();
            } catch (Exception ignored) {}
        }

        String streakText;
        if (streak > 0) {
            streakText = "🔥  " + streak + " day streak — keep it up!";
        } else {
            try {
                FileManager fms = new FileManager(
                        user.getDataFolderPath(), user.getPasswordHash());
                List<JournalEntry> se = fms.loadAllEntries(user.getUsername());
                if (se.isEmpty()) {
                    streakText = "✨  Start your streak — write today's entry!";
                } else {
                    se.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                    long daysAway = java.time.temporal.ChronoUnit.DAYS.between(
                            se.get(0).getTimestamp().toLocalDate(),
                            java.time.LocalDate.now());
                    if (daysAway == 1)
                        streakText = "💫  You were on a roll. One entry today brings it back.";
                    else if (daysAway <= 3)
                        streakText = "🌱  " + daysAway + " days away. No pressure — just come back when ready.";
                    else
                        streakText = "💙  It's been a while. Whenever you're ready, this space is here.";
                }
            } catch (Exception ignored) {
                streakText = "✨  Start your streak — write today's entry!";
            }
        }
        Label streakLabel = new Label(streakText);
        streakLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        streakLabel.setTextFill(Color.web("#FFFFFF"));

        HBox streakBanner = new HBox(streakLabel);
        streakBanner.setAlignment(Pos.CENTER);
        streakBanner.setPadding(new Insets(10));
        streakBanner.setStyle("-fx-background-color: #7E57C2;");

        // ── Content ────────────────────────────────────────────────────────
        VBox contentBox = new VBox(18);
        contentBox.setPadding(new Insets(24, 28, 32, 28));
        contentBox.setStyle("-fx-background-color: " + s.getBg() + ";");

        // Welcome
        Label welcomeLabel = new Label(
                "Welcome back, " + user.getUsername() + " 👋");
        welcomeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        welcomeLabel.setTextFill(Color.web(s.getPrimaryText()));

// Contextual message
        String contextMsg = getContextualMessage();
        Label contextLabel = new Label(contextMsg);
        contextLabel.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14));
        contextLabel.setTextFill(Color.web(s.getSecondaryText()));
        contextLabel.setWrapText(true);

        contentBox.getChildren().addAll(welcomeLabel, contextLabel);

        // Guest warning
        if (user.isGuest()) {
            Label guestWarning = new Label(
                    "⚠️  Guest Mode — data will not be saved between sessions.");
            guestWarning.setFont(Font.font("Segoe UI", 13));
            guestWarning.setTextFill(Color.web("#795548"));
            guestWarning.setWrapText(true);
            guestWarning.setStyle(
                    "-fx-background-color: #FFF8E1;" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 10 16 10 16;");
            contentBox.getChildren().add(guestWarning);
        }

        // Quote card


        // Golden rule card

// ── Mental Health Score ────────────────────────────────────────────────
        if (!user.isGuest()) {
            try {
                FileManager fmScore = new FileManager(
                        user.getDataFolderPath(), user.getPasswordHash());
                List<JournalEntry> allEntries =
                        fmScore.loadAllEntries(user.getUsername());

                int score = calculateMentalHealthScore(allEntries);

                // Trajectory
                String trajectory = "Stable →";
                String trajectoryColor = s.getPrimaryText();
                if (allEntries.size() >= 6) {
                    java.util.Map<String, Integer> mv = new java.util.HashMap<>();
                    mv.put("Happy", 100); mv.put("Calm", 80);
                    mv.put("Neutral", 60); mv.put("Anxious", 35);
                    mv.put("Sad", 25); mv.put("Angry", 20);
                    List<JournalEntry> sorted = new ArrayList<>(allEntries);
                    sorted.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
                    int r = 0, o = 0;
                    for (int i = 0; i < 3; i++)
                        r += mv.getOrDefault(sorted.get(i).getMood(), 50);
                    for (int i = 3; i < 6; i++)
                        o += mv.getOrDefault(sorted.get(i).getMood(), 50);
                    if (r / 3 > o / 3) { trajectory = "Improving ↑"; trajectoryColor = "#388E3C"; }
                    else if (r / 3 < o / 3) { trajectory = "Declining ↓"; trajectoryColor = "#E53935"; }
                }

                // Weather
                String weather;
                String weatherEmoji;
                if (score >= 80) { weather = "Clear skies"; weatherEmoji = "☀️"; }
                else if (score >= 65) { weather = "Partly cloudy"; weatherEmoji = "⛅"; }
                else if (score >= 50) { weather = "Overcast"; weatherEmoji = "☁️"; }
                else if (score >= 35) { weather = "Stormy"; weatherEmoji = "🌧️"; }
                else { weather = "Heavy storm"; weatherEmoji = "⛈️"; }

                // Score color
                String scoreColor;
                if (score >= 70) scoreColor = "#7E57C2";
                else if (score >= 45) scoreColor = "#9575CD";
                else scoreColor = "#E53935";

                // Score box
                WellnessGauge gauge = new WellnessGauge(score);
                Label scoreTitle = new Label("Wellness Score");
                scoreTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                scoreTitle.setTextFill(javafx.scene.paint.Color.web(s.getPrimaryText()));

                Label scoreNumber = new Label(score + " / 100");
                scoreNumber.setStyle("-fx-text-fill: #9575CD; -fx-font-size: 28px; -fx-font-weight: bold;");

                VBox scoreBox = new VBox(4, scoreTitle, gauge, scoreNumber);
                scoreBox.setAlignment(Pos.CENTER_LEFT);

                // Trajectory box
                Label trajectoryTitle = new Label("7-Day Trend");
                trajectoryTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                trajectoryTitle.setTextFill(javafx.scene.paint.Color.web(s.getPrimaryText()));

                Label trajectoryLabel = new Label(trajectory);
                trajectoryLabel.setStyle("-fx-text-fill: #9575CD; -fx-font-size: 16px; -fx-font-weight: bold;");

                TrendGraph trendGraph = new TrendGraph(trajectory);
                VBox trajectoryBox = new VBox(4, trajectoryTitle, trajectoryLabel, trendGraph);
                trajectoryBox.setAlignment(Pos.CENTER_LEFT);

                // Weather box
                Label weatherTitle = new Label("Mental Weather");
                weatherTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                weatherTitle.setTextFill(javafx.scene.paint.Color.web(s.getPrimaryText()));

                Label weatherEmoji2 = new Label(weatherEmoji);
                weatherEmoji2.setStyle("-fx-font-size: 16px; -fx-text-fill: " + (AppSettings.get().isNightMode() ? "#FFFFFF" : "#000000") + ";");

                Label weatherLabel = new Label(weather);
                weatherLabel.setStyle("-fx-text-fill: #9575CD; -fx-font-size: 16px; -fx-font-weight: bold;");

                HBox weatherRow = new HBox(6, weatherEmoji2, weatherLabel);
                weatherRow.setAlignment(Pos.CENTER_LEFT);

                Label floatEmoji = new Label(weatherEmoji);
                floatEmoji.setStyle("-fx-font-size: 68px; -fx-opacity: 0.2;");

                javafx.animation.TranslateTransition floatAnim = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(2), floatEmoji);
                floatAnim.setByY(8);
                floatAnim.setAutoReverse(true);
                floatAnim.setCycleCount(javafx.animation.Animation.INDEFINITE);
                floatAnim.play();

                HBox weatherBox = new HBox(0);
                weatherBox.setAlignment(Pos.CENTER_LEFT);

                VBox weatherText = new VBox(4, weatherTitle, weatherRow);
                weatherText.setAlignment(Pos.CENTER_LEFT);

                javafx.scene.layout.StackPane weatherStack = new javafx.scene.layout.StackPane(weatherText, floatEmoji);
                javafx.scene.layout.StackPane.setAlignment(floatEmoji, Pos.CENTER_RIGHT);
                floatEmoji.setTranslateX(10);

                weatherBox.getChildren().add(weatherStack);

                // Privacy badge
                Label privacyBadge = new Label("🔒  No internet. No servers. Just you.");
                privacyBadge.setFont(Font.font("Segoe UI", 11));
                privacyBadge.setTextFill(javafx.scene.paint.Color.web(s.getPrimaryText()));

                // Dividers
                Region d1 = new Region();
                d1.setPrefWidth(1); d1.setPrefHeight(40);
                d1.setStyle("-fx-background-color: " + s.getBorderColor() + ";");

                Region d2 = new Region();
                d2.setPrefWidth(1); d2.setPrefHeight(40);
                d2.setStyle("-fx-background-color: " + s.getBorderColor() + ";");


                privacyBadge.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + s.getPrimaryText() + ";");

                javafx.animation.TranslateTransition privacyAnim = new javafx.animation.TranslateTransition(javafx.util.Duration.seconds(1.2), privacyBadge);
                privacyAnim.setByY(10);
                privacyAnim.setAutoReverse(true);
                privacyAnim.setCycleCount(javafx.animation.Animation.INDEFINITE);
                privacyAnim.play();

                Region d3 = new Region();
                d3.setPrefWidth(1); d3.setPrefHeight(40);
                d3.setStyle("-fx-background-color: " + s.getBorderColor() + ";");

                Label weeklyInsightLabel = new Label();
                weeklyInsightLabel.setStyle(
                        "-fx-font-size: 11px; " +
                                "-fx-text-fill: " + s.getPrimaryText() + "; " +
                                "-fx-font-style: italic; " +
                                "-fx-wrap-text: true;");
                weeklyInsightLabel.setMaxWidth(Double.MAX_VALUE);
                weeklyInsightLabel.setWrapText(true);
                weeklyInsightLabel.setTranslateX(20);

                try {
                    FileManager fmw = new FileManager(user.getDataFolderPath(), user.getPasswordHash());
                    List<JournalEntry> wEntries = fmw.loadAllEntries(user.getUsername());
                    MoodAnalyser wAnalyser = new MoodAnalyser(wEntries);
                    String insight = wAnalyser.getWeeklyInsight();
                    weeklyInsightLabel.setText("❝  " + insight);
                } catch (Exception ignored) {
                    weeklyInsightLabel.setText("❝  Keep journaling to see your weekly insight.");
                }

                weeklyInsightLabel.setMaxWidth(150);
                weeklyInsightLabel.setPrefWidth(150);
                weeklyInsightLabel.setWrapText(true);

                Region d4 = new Region();
                d4.setPrefWidth(1); d4.setPrefHeight(40);
                d4.setStyle("-fx-background-color: " + s.getBorderColor() + ";");

                HBox insightRow = new HBox(16, scoreBox, d1, trajectoryBox, d2, weatherStack, d3, privacyBadge, d4, weeklyInsightLabel);
                insightRow.setAlignment(Pos.CENTER_LEFT);


                insightCard = new VBox(10, insightRow);
                insightCard.setPadding(new Insets(16, 20, 16, 20));
                insightCard.setStyle(s.cardStyle());


                contentBox.getChildren().add(insightCard);

            } catch (Exception ignored) {}
        }
        // Nav label
        Label navLabel = new Label("What would you like to do?");
        navLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        navLabel.setTextFill(Color.web(s.getPrimaryText()));
        contentBox.getChildren().add(navLabel);

        // ── Nav Grid ───────────────────────────────────────────────────────
        String[][] navItems = {
                {"✍️",  "Write Entry",      "Record your thoughts"},
                {"📖",  "View Entries",     "Browse your journal"},
                {"📊",  "Analytics",        "Mood trends & streaks"},
                {"🔐",  "Password Vault",   "Secure stored passwords"},
                {"📝",  "Daily Questions",  "5 quick check-in questions"},
                {"📤",  "Export",           "TXT / CSV / HTML export"},
        };

        String[] accents = {
                "#7E57C2", "#42A5F5", "#26A69A",
                "#EF5350", "#FF7043", "#66BB6A"
        };

        Runnable[] actions = {
                () -> navigateTo(new WriteEntryScreen(stage, user).getScene()),
                () -> navigateTo(new ViewEntriesScreen(stage, user).getScene()),
                () -> navigateTo(new AnalyticsScreen(stage, user).getScene()),
                () -> navigateTo(new PasswordVaultScreen(stage, user).getScene()),
                () -> navigateTo(new DailyQuestionsScreen(stage, user).getScene()),
                () -> navigateTo(new ExportScreen(stage, user).getScene()),
        };

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setMaxWidth(Double.MAX_VALUE);

        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            String accent = accents[i];

            String baseStyle =
                    "-fx-background-color: " + s.getCardBg() + ";" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: " + accent + ";" +
                            "-fx-border-width: 0 0 0 4;" +
                            "-fx-border-radius: 0 14 14 0;" +
                            "-fx-effect: dropshadow(gaussian," +
                            "rgba(0,0,0,0.08),8,0,0,2);" +
                            "-fx-cursor: hand;";

            String hoverStyle =
                    "-fx-background-color: " + accent + ";" +
                            "-fx-background-radius: 14;" +
                            "-fx-border-color: " + accent + ";" +
                            "-fx-border-width: 0 0 0 4;" +
                            "-fx-border-radius: 0 14 14 0;" +
                            "-fx-effect: dropshadow(gaussian," +
                            "rgba(0,0,0,0.18),12,0,0,4);" +
                            "-fx-cursor: hand;";

            Label emoji = new Label(navItems[i][0]);
            emoji.setFont(Font.font(26));

            Label navTitle = new Label(navItems[i][1]);
            navTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            navTitle.setTextFill(Color.web(s.getPrimaryText()));

            Label navDesc = new Label(navItems[i][2]);
            navDesc.setFont(Font.font("Segoe UI", 11));
            navDesc.setTextFill(Color.web(s.getMutedText()));

            VBox card = new VBox(6, emoji, navTitle, navDesc);
            card.setPadding(new Insets(18, 16, 18, 16));
            card.setMinHeight(100);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle(baseStyle);

            javafx.scene.layout.StackPane cardWrapper = new javafx.scene.layout.StackPane(card);
            cardWrapper.setStyle("-fx-background-radius: 14;");
            cardWrapper.setClip(null);

            cardWrapper.setOnMouseEntered(e -> {
                card.setStyle(hoverStyle);
                navTitle.setTextFill(Color.WHITE);
                navDesc.setTextFill(Color.web("rgba(255,255,255,0.8)"));
            });
            cardWrapper.setOnMouseExited(e -> {
                card.setStyle(baseStyle);
                navTitle.setTextFill(Color.web(s.getPrimaryText()));
                navDesc.setTextFill(Color.web(s.getMutedText()));
            });
            card.setOnMousePressed(e -> {
                if (AppSettings.get().isRippleEnabled()) {
                    javafx.scene.shape.Circle ripple = new javafx.scene.shape.Circle(0);
                    ripple.setFill(javafx.scene.paint.Color.RED);
                    ripple.setCenterX(e.getX());
                    ripple.setCenterY(e.getY());
                    ripple.setMouseTransparent(true);

                    card.getChildren().add(ripple);

                    javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                            javafx.util.Duration.millis(400), ripple);
                    scale.setToX(60);
                    scale.setToY(60);

                    javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(400), ripple);
                    fade.setFromValue(0.4);
                    fade.setToValue(0);

                    javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(scale, fade);
                    pt.setOnFinished(ev -> card.getChildren().remove(ripple));
                    pt.play();
                }
            });
            cardWrapper.setOnMouseClicked(e -> {
                if (AppSettings.get().isRippleEnabled()) {
                    javafx.scene.shape.Circle ripple = new javafx.scene.shape.Circle(4);
                    ripple.setFill(javafx.scene.paint.Color.web("#FFFFFF", 0.35));
                    ripple.setTranslateX(e.getX() - cardWrapper.getWidth() / 2);
                    ripple.setTranslateY(e.getY() - cardWrapper.getHeight() / 2);
                    ripple.setMouseTransparent(true);
                    cardWrapper.getChildren().add(ripple);

                    javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(
                            javafx.util.Duration.millis(500), ripple);
                    scale.setToX(20);
                    scale.setToY(20);

                    javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(500), ripple);
                    fade.setFromValue(0.35);
                    fade.setToValue(0);

                    javafx.animation.ParallelTransition pt = new javafx.animation.ParallelTransition(scale, fade);
                    pt.setOnFinished(ev -> cardWrapper.getChildren().remove(ripple));
                    pt.play();
                }
                actions[idx].run();
            });

            grid.add(cardWrapper, i % 2, i / 2);
        }

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);
        contentBox.getChildren().add(grid);

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background: " + s.getBg() + ";" +
                        "-fx-background-color: " + s.getBg() + ";" +
                        "-fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Stagger fade
        java.util.List<javafx.scene.Node> fadeNodes = new java.util.ArrayList<>();
        fadeNodes.add(topBar);
        fadeNodes.add(streakBanner);
        fadeNodes.add(welcomeLabel);
        fadeNodes.add(contextLabel);
        fadeNodes.add(insightCard);
        fadeNodes.add(navLabel);
        fadeNodes.add(grid);

        if (!fromModeSwitch) {
            for (int i = 0; i < fadeNodes.size(); i++) {
                javafx.scene.Node node = fadeNodes.get(i);
                node.setOpacity(0);
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(150), node);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.setDelay(javafx.util.Duration.millis(i * 30));
                ft.play();
            }
        }

        VBox root = new VBox(topBar, streakBanner, scroll);
        root.setStyle(s.rootStyle());
        javafx.application.Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    new WriteEntryScreen(stage, user).getScene();
                    new ViewEntriesScreen(stage, user).getScene();
                    new AnalyticsScreen(stage, user).getScene();
                    new PasswordVaultScreen(stage, user).getScene();
                    new DailyQuestionsScreen(stage, user).getScene();
                    new ExportScreen(stage, user).getScene();
                    new SettingsScreen(stage, user).getScene();
                } catch (Exception ignored) {}
            }, "preloader").start();
        });

        stage.setResizable(true);
        stage.setMinWidth(480);
        stage.setMinHeight(400);
        stage.setTitle("PsychLog — Dashboard");

        return new Scene(root);
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private String detectLatestMood() {
        if (user.isGuest()) return "Neutral";
        try {
            FileManager fm = new FileManager(
                    user.getDataFolderPath(),
                    user.getPasswordHash());
            List<JournalEntry> entries =
                    fm.loadAllEntries(user.getUsername());
            if (!entries.isEmpty()) {
                entries.sort((a, b) ->
                        b.getTimestamp().compareTo(a.getTimestamp()));
                return entries.get(0).getMood();
            }
        } catch (Exception ignored) {}
        return "Neutral";
    }

    private String topBarBtnStyle() {
        return "-fx-background-color: rgba(255,255,255,0.22);" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 12px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.5);" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 6 14 6 14;" +
                "-fx-cursor: hand;";
    }

    private String topBarBtnHoverStyle() {
        return "-fx-background-color: rgba(255,255,255,0.38);" +
                "-fx-text-fill: #FFFFFF;" +
                "-fx-font-size: 12px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-border-color: rgba(255,255,255,0.9);" +
                "-fx-border-radius: 20;" +
                "-fx-padding: 6 14 6 14;" +
                "-fx-cursor: hand;";
    }
    private String getContextualMessage() {
        int hour = java.time.LocalTime.now().getHour();

        String timeMsg = "Good to see you.";
        if (hour >= 0 && hour < 5)
            timeMsg = "It's late. Whatever brought you here — you came. That matters.";
        else if (hour >= 5 && hour < 12)
            timeMsg = "Morning. Fresh start. Let's see what today brings.";
        else if (hour >= 12 && hour < 17)
            timeMsg = "Afternoon check in. How's the day treating you?";
        else if (hour >= 17 && hour < 21)
            timeMsg = "Evening. You made it through another day.";
        else
            timeMsg = "Late night. This is a safe place for whatever's on your mind.";

        if (user.isGuest()) return timeMsg;

        try {
            FileManager fm = new FileManager(
                    user.getDataFolderPath(), user.getPasswordHash());
            List<JournalEntry> entries = fm.loadAllEntries(user.getUsername());

            if (entries.isEmpty()) return "This is your space. Start whenever you're ready.";

            entries.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
            JournalEntry last = entries.get(0);

            long daysSinceLast = java.time.temporal.ChronoUnit.DAYS.between(
                    last.getTimestamp().toLocalDate(),
                    java.time.LocalDate.now());

            String lastMood = last.getMood();
            MoodAnalyser analyser = new MoodAnalyser(entries);
            String velocity = analyser.getMoodVelocity();
            String dominant = analyser.getDominantMood();
            String timePattern = analyser.getWritingTimePattern();
            int thisWeek = analyser.getEntriesThisWeek();

            // Been away a long time
            if (daysSinceLast >= 7)
                return "You've been away for " + daysSinceLast + " days. No judgment. You're here now.";

            // Came back after dark mood
            if (daysSinceLast >= 2 &&
                    (lastMood.equals("Sad") || lastMood.equals("Angry") || lastMood.equals("Anxious")))
                return "Last time felt heavy. You came back anyway. That says something.";

            // Strong streak
            StreakTracker tracker = new StreakTracker(entries);
            int streak = tracker.getCurrentStreak();
            if (streak >= 7)
                return "You've shown up " + streak + " days straight. That's not nothing.";

            // Mood improving
            if (velocity.equals("Improving"))
                return "Things have been looking up lately. Keep going.";

            // Mood declining
            if (velocity.equals("Declining") &&
                    (dominant.equals("Sad") || dominant.equals("Anxious")))
                return "This week has felt heavy. You don't have to figure it all out today.";

            // Active writer this week
            if (thisWeek >= 5)
                return "You've written " + thisWeek + " times this week. You're really showing up for yourself.";

            // Time pattern personalisation
            if (timePattern.equals("Night") && hour >= 21)
                return "You always come here at night. This space is ready whenever you are.";
            if (timePattern.equals("Morning") && hour >= 5 && hour < 12)
                return "Morning again. You make this a habit and it shows.";

            // Monday
            if (java.time.LocalDate.now().getDayOfWeek() == java.time.DayOfWeek.MONDAY)
                return "New week. Fresh page. Let's see what this one brings.";

            // Last mood was good
            if (lastMood.equals("Happy") || lastMood.equals("Calm"))
                return "Something good was happening last time. Holding onto that?";

            return timeMsg;

        } catch (Exception e) {
            return timeMsg;
        }
    }
    private int calculateMentalHealthScore(List<JournalEntry> entries) {
        if (entries == null || entries.isEmpty()) return 50;

        // Score 1 — Recent mood score (last 7 entries)
        java.util.Map<String, Integer> moodValues = new java.util.HashMap<>();
        moodValues.put("Happy", 100);
        moodValues.put("Calm", 80);
        moodValues.put("Neutral", 60);
        moodValues.put("Anxious", 35);
        moodValues.put("Sad", 25);
        moodValues.put("Angry", 20);

        List<JournalEntry> recent = new ArrayList<>(entries);
        recent.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        if (recent.size() > 7) recent = recent.subList(0, 7);

        int moodScore = 0;
        for (JournalEntry e : recent) {
            moodScore += moodValues.getOrDefault(e.getMood(), 50);
        }
        moodScore = moodScore / recent.size();

        // Score 2 — Streak score (max 100 at 14 day streak)
        StreakTracker tracker = new StreakTracker(entries);
        int streak = tracker.getCurrentStreak();
        int streakScore = Math.min(streak * 7, 100);

        // Score 3 — Frequency this week
        long thisWeek = entries.stream().filter(e ->
                        java.time.temporal.ChronoUnit.DAYS.between(
                                e.getTimestamp().toLocalDate(),
                                java.time.LocalDate.now()) <= 7)
                .count();
        int freqScore = (int) Math.min(thisWeek * 14, 100);

        // Score 4 — Trend (comparing last 3 vs previous 3)
        int trendScore = 50;
        if (entries.size() >= 6) {
            List<JournalEntry> sorted = new ArrayList<>(entries);
            sorted.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
            int recentAvg = 0;
            int olderAvg = 0;
            for (int i = 0; i < 3; i++)
                recentAvg += moodValues.getOrDefault(sorted.get(i).getMood(), 50);
            for (int i = 3; i < 6; i++)
                olderAvg += moodValues.getOrDefault(sorted.get(i).getMood(), 50);
            recentAvg /= 3;
            olderAvg /= 3;
            if (recentAvg > olderAvg) trendScore = 80;
            else if (recentAvg < olderAvg) trendScore = 30;
        }

        // Final weighted score
        return (moodScore * 40 + streakScore * 20 +
                freqScore * 20 + trendScore * 20) / 100;
    }
    public Scene getSceneNoFade() {
        fromModeSwitch = true;
        return getScene();
    }
}