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

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ExportScreen {

    private final Stage stage;
    private final User user;

    public ExportScreen(Stage stage, User user) {
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
        Label titleLabel = new Label("📤  Export Your Report");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label subtitleLabel = new Label(
                "Download a readable summary of your journal and mood data.");
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web(s.getPrimaryText()));
        subtitleLabel.setWrapText(true);

        VBox titleBox = new VBox(6, titleLabel, subtitleLabel);

        // ── Guest block ────────────────────────────────────────────────────
        if (user.isGuest()) {
            Label guestMsg = new Label(
                    "⚠️  Create an account to export your wellness report.");
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

        // ── Export Options Card ────────────────────────────────────────────
        Label optionsTitle = new Label("Export Options");
        optionsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        optionsTitle.setTextFill(Color.web("#5C6BC0"));

        ToggleGroup formatGroup = new ToggleGroup();

        RadioButton txtOption = new RadioButton("Plain Text (.txt)");
        txtOption.setToggleGroup(formatGroup);
        txtOption.setSelected(true);
        txtOption.setFont(Font.font("Segoe UI", 13));
        txtOption.setTextFill(Color.web(s.getPrimaryText()));

        RadioButton csvOption = new RadioButton("CSV Spreadsheet (.csv)");
        csvOption.setToggleGroup(formatGroup);
        csvOption.setFont(Font.font("Segoe UI", 13));
        csvOption.setTextFill(Color.web(s.getPrimaryText()));

        RadioButton htmlOption = new RadioButton("HTML Report (.html)");
        htmlOption.setToggleGroup(formatGroup);
        htmlOption.setFont(Font.font("Segoe UI", 13));
        htmlOption.setTextFill(Color.web(s.getPrimaryText()));

        Label formatHeading = new Label("Format:");
        formatHeading.setFont(Font.font("Segoe UI", 13));
        formatHeading.setTextFill(Color.web(s.getPrimaryText()));

        VBox formatBox = new VBox(8, formatHeading, txtOption, csvOption, htmlOption);

        Label rangeLabel = new Label("Include:");
        rangeLabel.setFont(Font.font("Segoe UI", 13));
        rangeLabel.setTextFill(Color.web(s.getPrimaryText()));

        ToggleGroup rangeGroup = new ToggleGroup();
        RadioButton allEntries = new RadioButton("All entries");
        RadioButton last30     = new RadioButton("Last 30 days");
        RadioButton last7      = new RadioButton("Last 7 days");
        allEntries.setToggleGroup(rangeGroup);
        allEntries.setSelected(true);
        last30.setToggleGroup(rangeGroup);
        last7.setToggleGroup(rangeGroup);
        allEntries.setFont(Font.font("Segoe UI", 13));
        last30.setFont(Font.font("Segoe UI", 13));
        last7.setFont(Font.font("Segoe UI", 13));
        allEntries.setTextFill(Color.web(s.getPrimaryText()));
        last30.setTextFill(Color.web(s.getPrimaryText()));
        last7.setTextFill(Color.web(s.getPrimaryText()));

        VBox rangeBox = new VBox(8, rangeLabel, allEntries, last30, last7);

        HBox optionsRow = new HBox(60, formatBox, rangeBox);
        optionsRow.setAlignment(Pos.TOP_LEFT);

        // ── Folder picker ──────────────────────────────────────────────────
        Label folderLabel = new Label("Save to folder:");
        folderLabel.setFont(Font.font("Segoe UI", 13));
        folderLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label folderPathLabel = new Label("No folder selected — will save to your data folder");
        folderPathLabel.setFont(Font.font("Segoe UI", 11));
        folderPathLabel.setTextFill(Color.web(s.getPrimaryText()));
        folderPathLabel.setWrapText(true);

        final String[] exportFolder = {user.getDataFolderPath()};

        Button browseBtn = new Button("Choose Folder");
        browseBtn.setStyle(secondaryButtonStyle());
        browseBtn.setOnMouseEntered(e -> browseBtn.setStyle(secondaryButtonHoverStyle()));
        browseBtn.setOnMouseExited(e -> browseBtn.setStyle(secondaryButtonStyle()));
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose export folder");
            File chosen = chooser.showDialog(stage);
            if (chosen != null) {
                exportFolder[0] = chosen.getAbsolutePath();
                folderPathLabel.setText(chosen.getAbsolutePath());
                folderPathLabel.setTextFill(Color.web("#5C6BC0"));
            }
        });

        VBox folderBox = new VBox(6, folderLabel, browseBtn, folderPathLabel);

        // ── Status ─────────────────────────────────────────────────────────
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", 13));
        statusLabel.setVisible(false);
        statusLabel.setWrapText(true);

        // ── Export Button ──────────────────────────────────────────────────
        Button exportBtn = new Button("📤  Export Now");
        exportBtn.setPrefWidth(200);
        exportBtn.setPrefHeight(44);
        exportBtn.setStyle(primaryButtonStyle());
        exportBtn.setOnMouseEntered(e -> exportBtn.setStyle(primaryButtonHoverStyle()));
        exportBtn.setOnMouseExited(e -> exportBtn.setStyle(primaryButtonStyle()));

        exportBtn.setOnAction(e -> {
            try {
                FileManager fm = new FileManager(
                        user.getDataFolderPath(), user.getPasswordHash());
                List<JournalEntry> entries = fm.loadAllEntries(user.getUsername());

                if (entries.isEmpty()) {
                    showStatus(statusLabel, "No entries to export yet.", "#F57F17");
                    return;
                }

                java.time.LocalDateTime cutoff = null;
                if (last30.isSelected()) {
                    cutoff = java.time.LocalDateTime.now().minusDays(30);
                } else if (last7.isSelected()) {
                    cutoff = java.time.LocalDateTime.now().minusDays(7);
                }
                if (cutoff != null) {
                    final java.time.LocalDateTime fc = cutoff;
                    entries.removeIf(en -> en.getTimestamp().isBefore(fc));
                }

                if (entries.isEmpty()) {
                    showStatus(statusLabel, "No entries found in that date range.", "#F57F17");
                    return;
                }

                entries.sort(Comparator.comparing(JournalEntry::getTimestamp));

                String filePath;
                if (csvOption.isSelected()) {
                    filePath = exportCSV(entries, exportFolder[0]);
                } else if (htmlOption.isSelected()) {
                    filePath = exportHTML(entries, exportFolder[0]);
                } else {
                    filePath = exportTXT(entries, exportFolder[0]);
                }

                showStatus(statusLabel, "✅  Exported successfully!\n📁  " + filePath, "#388E3C");

            } catch (Exception ex) {
                showStatus(statusLabel, "Export failed: " + ex.getMessage(), "#E53935");
                ex.printStackTrace();
            }
        });

        VBox optionsCard = new VBox(16,
                optionsTitle,
                separator(s),
                optionsRow,
                separator(s),
                folderBox,
                statusLabel,
                exportBtn
        );
        optionsCard.setPadding(new Insets(24, 28, 24, 28));
        optionsCard.setStyle(s.cardStyle());

        VBox previewCard = makePreviewCard(s);

        VBox contentBox = new VBox(20, titleBox, optionsCard, previewCard);
        contentBox.setPadding(new Insets(28));

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + s.getBg() + "; -fx-background-color:" + s.getBg() + ";");

        VBox root = new VBox(topBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");

        return new Scene(root);
    }

    // ── Export Methods ─────────────────────────────────────────────────────

    private String exportTXT(List<JournalEntry> entries, String folder) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMM d yyyy  hh:mm a");
        StringBuilder sb = new StringBuilder();
        sb.append("PsychLog — Wellness Report\n");
        sb.append("User: ").append(user.getUsername()).append("\n");
        sb.append("Generated: ").append(java.time.LocalDateTime.now().format(fmt)).append("\n");
        sb.append("Total Entries: ").append(entries.size()).append("\n");
        sb.append("=".repeat(50)).append("\n\n");

        for (JournalEntry entry : entries) {
            sb.append("Date: ").append(entry.getTimestamp().format(fmt)).append("\n");
            sb.append("Mood: ").append(ThemeManager.getMoodEmoji(entry.getMood()))
                    .append(" ").append(entry.getMood()).append("\n");
            sb.append("-".repeat(40)).append("\n");
            sb.append(entry.getContent()).append("\n\n");
        }

        String fileName = "PsychLog_Report_" + java.time.LocalDate.now() + ".txt";
        Path path = Paths.get(folder, fileName);
        Files.writeString(path, sb.toString());
        return path.toString();
    }

    private String exportCSV(List<JournalEntry> entries, String folder) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("Date,Time,Mood,Content\n");

        for (JournalEntry entry : entries) {
            String date    = entry.getTimestamp().toLocalDate().toString();
            String time    = entry.getTimestamp().toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            String mood    = entry.getMood();
            String content = entry.getContent().replace("\"", "\"\"").replace("\n", " ");
            sb.append("\"").append(date).append("\",");
            sb.append("\"").append(time).append("\",");
            sb.append("\"").append(mood).append("\",");
            sb.append("\"").append(content).append("\"\n");
        }

        String fileName = "PsychLog_Report_" + java.time.LocalDate.now() + ".csv";
        Path path = Paths.get(folder, fileName);
        Files.writeString(path, sb.toString());
        return path.toString();
    }

    private String exportHTML(List<JournalEntry> entries, String folder) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, MMM d yyyy  hh:mm a");

        Map<String, Long> moodCount = new LinkedHashMap<>();
        for (JournalEntry e : entries) moodCount.merge(e.getMood(), 1L, Long::sum);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head>")
                .append("<meta charset='UTF-8'>")
                .append("<title>PsychLog Report</title>")
                .append("<style>")
                .append("body{font-family:'Segoe UI',sans-serif;background:#F3F0FB;margin:0;padding:32px;}")
                .append("h1{color:#5C6BC0;}h2{color:#7E57C2;font-size:16px;margin-top:32px;}")
                .append(".card{background:#fff;border-radius:14px;padding:20px 24px;margin-bottom:16px;")
                .append("box-shadow:0 2px 8px rgba(0,0,0,0.08);}")
                .append(".mood{display:inline-block;padding:4px 14px;border-radius:20px;")
                .append("font-weight:bold;font-size:13px;}")
                .append(".date{color:#9E9E9E;font-size:12px;margin-bottom:8px;}")
                .append(".content{color:#424242;line-height:1.7;white-space:pre-wrap;}")
                .append(".summary{background:#EDE7F6;border-radius:14px;padding:16px 24px;margin-bottom:24px;}")
                .append("</style></head><body>");

        sb.append("<h1>🧠 PsychLog — Wellness Report</h1>");
        sb.append("<p style='color:#9E9E9E'>User: <b>").append(user.getUsername())
                .append("</b> &nbsp;|&nbsp; Generated: <b>").append(java.time.LocalDate.now())
                .append("</b> &nbsp;|&nbsp; Total Entries: <b>").append(entries.size()).append("</b></p>");

        sb.append("<div class='summary'><b>Mood Summary</b><br><br>");
        for (Map.Entry<String, Long> m : moodCount.entrySet()) {
            sb.append(ThemeManager.getMoodEmoji(m.getKey())).append(" ").append(m.getKey())
                    .append(": <b>").append(m.getValue()).append("</b> entries &nbsp;&nbsp;");
        }
        sb.append("</div><h2>Journal Entries</h2>");

        for (JournalEntry entry : entries) {
            String bgColor   = ThemeManager.getCardColor(entry.getMood());
            String textColor = ThemeManager.getTextColor(entry.getMood());
            sb.append("<div class='card'>")
                    .append("<div class='date'>").append(entry.getTimestamp().format(fmt)).append("</div>")
                    .append("<span class='mood' style='background:").append(bgColor)
                    .append(";color:").append(textColor).append("'>")
                    .append(ThemeManager.getMoodEmoji(entry.getMood())).append(" ").append(entry.getMood())
                    .append("</span>")
                    .append("<p class='content'>")
                    .append(entry.getContent().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"))
                    .append("</p></div>");
        }

        sb.append("</body></html>");

        String fileName = "PsychLog_Report_" + java.time.LocalDate.now() + ".html";
        Path path = Paths.get(folder, fileName);
        Files.writeString(path, sb.toString());
        return path.toString();
    }

    // ── Preview Card ───────────────────────────────────────────────────────
    private VBox makePreviewCard(AppSettings s) {
        Label previewTitle = new Label("What gets exported");
        previewTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        previewTitle.setTextFill(Color.web("#5C6BC0"));

        String[] items = {
                "📅  Date and time of each entry",
                "😊  Detected mood for each entry",
                "📝  Full journal entry content",
                "📊  Mood summary counts",
                "🔥  Streak information (TXT format)"
        };

        VBox list = new VBox(8);
        for (String item : items) {
            Label l = new Label(item);
            l.setFont(Font.font("Segoe UI", 13));
            l.setTextFill(Color.web(s.getPrimaryText()));
            list.getChildren().add(l);
        }

        Label noteLabel = new Label(
                "🔒  Passwords from your vault are never included in exports.");
        noteLabel.setFont(Font.font("Segoe UI", 12));
        noteLabel.setTextFill(Color.web(s.getPrimaryText()));
        noteLabel.setWrapText(true);

        VBox card = new VBox(12, previewTitle, separator(s), list, separator(s), noteLabel);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle(s.cardStyle());
        return card;
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
        sep.setStyle("-fx-background-color:" + (s.isNightMode() ? "#444444" : "#F0F0F0") + ";");
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