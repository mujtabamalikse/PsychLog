package com.psychlog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PasswordVaultScreen {

    private final Stage stage;
    private final User user;
    private VBox vaultList;
    private AppSettings s;

    public PasswordVaultScreen(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public Scene getScene() {

        s = AppSettings.get();

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
        Label titleLabel = new Label("🔐  Password Vault");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label subtitleLabel = new Label("Your passwords are encrypted and stored locally.");
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web(s.getPrimaryText()));

        VBox titleBox = new VBox(4, titleLabel, subtitleLabel);

        // ── Add Password Form ──────────────────────────────────────────────
        Label formTitle = new Label("Add New Password");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        formTitle.setTextFill(Color.web("#5C6BC0"));

        TextField siteField = new TextField();
        siteField.setPromptText("Website / App name");
        siteField.setPrefHeight(40);
        siteField.setStyle(s.fieldStyle());

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username or Email");
        usernameField.setPrefHeight(40);
        usernameField.setStyle(s.fieldStyle());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle(s.fieldStyle());

        TextField notesField = new TextField();
        notesField.setPromptText("Notes (optional)");
        notesField.setPrefHeight(40);
        notesField.setStyle(s.fieldStyle());

        Label formStatus = new Label("");
        formStatus.setFont(Font.font("Segoe UI", 12));
        formStatus.setVisible(false);

        Button addBtn = new Button("＋  Add to Vault");
        addBtn.setPrefHeight(42);
        addBtn.setStyle(primaryButtonStyle());
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(primaryButtonHoverStyle()));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(primaryButtonStyle()));

        addBtn.setOnAction(e -> {
            String site  = siteField.getText().trim();
            String uname = usernameField.getText().trim();
            String pass  = passwordField.getText();

            if (site.isEmpty() || uname.isEmpty() || pass.isEmpty()) {
                showStatus(formStatus, "Please fill in site, username and password.", "#E53935");
                return;
            }
            if (user.isGuest()) {
                showStatus(formStatus, "⚠️ Guest mode — vault entries are not saved.", "#F57F17");
                return;
            }
            try {
                saveVaultEntry(site, uname, pass, notesField.getText().trim());
                showStatus(formStatus, "✅  Saved to vault!", "#388E3C");
                siteField.clear();
                usernameField.clear();
                passwordField.clear();
                notesField.clear();
                refreshVaultList();
            } catch (Exception ex) {
                showStatus(formStatus, "Error saving: " + ex.getMessage(), "#E53935");
                ex.printStackTrace();
            }
        });

        VBox formBox = new VBox(10,
                formTitle,
                siteField, usernameField, passwordField, notesField,
                formStatus, addBtn);
        formBox.setPadding(new Insets(20, 24, 20, 24));
        formBox.setStyle(s.cardStyle());

        // ── Vault List ─────────────────────────────────────────────────────
        Label listTitle = new Label("Saved Passwords");
        listTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        listTitle.setTextFill(Color.web("#5C6BC0"));

        vaultList = new VBox(12);
        refreshVaultList();

        VBox listSection = new VBox(12, listTitle, vaultList);

        // ── Guest notice ───────────────────────────────────────────────────
        if (user.isGuest()) {
            Label guestMsg = new Label("⚠️  Create an account to use the Password Vault.");
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

        // ── Main Layout ────────────────────────────────────────────────────
        VBox contentBox = new VBox(20, titleBox, formBox, listSection);
        contentBox.setPadding(new Insets(28));

        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:" + s.getBg() + "; -fx-background-color:" + s.getBg() + ";");

        VBox root = new VBox(topBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle("-fx-background-color:" + s.getBg() + ";");

        return new Scene(root);
    }

    // ── Vault Entry Card ───────────────────────────────────────────────────
    private VBox makeVaultCard(String site, String username,
                               String password, String notes, int index) {

        Label siteLabel = new Label("🌐  " + site);
        siteLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        siteLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label userLabel = new Label("👤  " + username);
        userLabel.setFont(Font.font("Segoe UI", 13));
        userLabel.setTextFill(Color.web(s.getPrimaryText()));

        Label passLabel = new Label("🔑  ••••••••");
        passLabel.setFont(Font.font("Segoe UI", 13));
        passLabel.setTextFill(Color.web(s.getPrimaryText()));

        Button toggleBtn = new Button("Show");
        toggleBtn.setStyle(smallButtonStyle());
        final boolean[] shown = {false};
        toggleBtn.setOnAction(e -> {
            if (!shown[0]) {
                passLabel.setText("🔑  " + password);
                toggleBtn.setText("Hide");
                shown[0] = true;
            } else {
                passLabel.setText("🔑  ••••••••");
                toggleBtn.setText("Show");
                shown[0] = false;
            }
        });

        Button copyBtn = new Button("Copy");
        copyBtn.setStyle(smallButtonStyle());
        copyBtn.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard =
                    javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content =
                    new javafx.scene.input.ClipboardContent();
            content.putString(password);
            clipboard.setContent(content);
            copyBtn.setText("Copied!");
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override public void run() {
                    javafx.application.Platform.runLater(() -> copyBtn.setText("Copy"));
                }
            }, 1500);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(deleteButtonStyle());
        deleteBtn.setOnAction(e -> {
            try {
                deleteVaultEntry(index);
                refreshVaultList();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox passRow = new HBox(10, passLabel, toggleBtn, copyBtn);
        passRow.setAlignment(Pos.CENTER_LEFT);

        Region cardSpacer = new Region();
        HBox.setHgrow(cardSpacer, Priority.ALWAYS);

        HBox topRow = new HBox(cardSpacer, deleteBtn);
        topRow.setAlignment(Pos.CENTER_RIGHT);

        VBox card = new VBox(6, topRow, siteLabel, userLabel, passRow);

        if (!notes.isEmpty()) {
            Label notesLabel = new Label("📝  " + notes);
            notesLabel.setFont(Font.font("Segoe UI", 12));
            notesLabel.setTextFill(Color.web(s.getPrimaryText()));
            card.getChildren().add(notesLabel);
        }

        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(s.cardStyle());

        return card;
    }

    // ── Load & Refresh ─────────────────────────────────────────────────────
    private void refreshVaultList() {
        vaultList.getChildren().clear();
        if (user.isGuest()) return;

        try {
            List<String[]> entries = loadVaultEntries();
            if (entries.isEmpty()) {
                Label empty = new Label("No passwords saved yet.");
                empty.setFont(Font.font("Segoe UI", 13));
                empty.setTextFill(Color.web(s != null ? s.getPrimaryText() : "#9E9E9E"));
                vaultList.getChildren().add(empty);
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    String[] e = entries.get(i);
                    String notes = e.length > 3 ? e[3] : "";
                    vaultList.getChildren().add(makeVaultCard(e[0], e[1], e[2], notes, i));
                }
            }
        } catch (Exception ex) {
            Label err = new Label("Error loading vault: " + ex.getMessage());
            err.setTextFill(Color.web("#E53935"));
            vaultList.getChildren().add(err);
        }
    }

    // ── File I/O ───────────────────────────────────────────────────────────
    private Path getVaultPath() {
        return Paths.get(user.getDataFolderPath(), "vault.dat");
    }

    private void saveVaultEntry(String site, String username,
                                String password, String notes) throws Exception {
        EncryptionManager enc = new EncryptionManager(user.getPasswordHash());
        List<String[]> existing = loadVaultEntries();
        existing.add(new String[]{site, username, password, notes});

        StringBuilder sb = new StringBuilder();
        for (String[] entry : existing) {
            sb.append(entry[0]).append("|||")
                    .append(entry[1]).append("|||")
                    .append(entry[2]).append("|||")
                    .append(entry.length > 3 ? entry[3] : "")
                    .append("\n");
        }
        Files.writeString(getVaultPath(), enc.encrypt(sb.toString()));
    }

    private List<String[]> loadVaultEntries() throws Exception {
        List<String[]> entries = new ArrayList<>();
        Path path = getVaultPath();
        if (!Files.exists(path)) return entries;

        EncryptionManager enc = new EncryptionManager(user.getPasswordHash());
        String decrypted = enc.decrypt(Files.readString(path));

        for (String line : decrypted.split("\n")) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split("\\|\\|\\|", -1);
            if (parts.length >= 3) entries.add(parts);
        }
        return entries;
    }

    private void deleteVaultEntry(int index) throws Exception {
        List<String[]> entries = loadVaultEntries();
        if (index >= 0 && index < entries.size()) entries.remove(index);

        EncryptionManager enc = new EncryptionManager(user.getPasswordHash());
        StringBuilder sb = new StringBuilder();
        for (String[] entry : entries) {
            sb.append(entry[0]).append("|||")
                    .append(entry[1]).append("|||")
                    .append(entry[2]).append("|||")
                    .append(entry.length > 3 ? entry[3] : "")
                    .append("\n");
        }
        Files.writeString(getVaultPath(), enc.encrypt(sb.toString()));
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

    private String smallButtonStyle() {
        return "-fx-background-color: #EDE7F6;" +
                "-fx-text-fill: #5C6BC0;" +
                "-fx-font-size: 12px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 4 12 4 12;";
    }

    private String deleteButtonStyle() {
        return "-fx-background-color: #FFEBEE;" +
                "-fx-text-fill: #E53935;" +
                "-fx-font-size: 12px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 4 12 4 12;";
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