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
import java.nio.file.*;
import java.util.List;

public class SettingsScreen {

    private final Stage stage;
    private final User user;
    private final AppSettings settings = AppSettings.get();

    public SettingsScreen(Stage stage, User user) {
        this.stage = stage;
        this.user = user;
    }

    public Scene getScene() {

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
        topBar.setStyle("-fx-background-color: #5C6BC0;");

        // ── Title ──────────────────────────────────────────────────────────
        Label titleLabel = new Label("⚙️  Settings");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(settings.getPrimaryText()));

        // ══════════════════════════════════════════════════════════════════
        // SECTION 1 — GENERAL
        // ══════════════════════════════════════════════════════════════════
        Label generalTitle = makeSectionTitle("⚙️ General");

        // Night mode toggle
        Label nightLabel = makeSettingLabel(
                "🌙  Night Mode",
                "Switch between light and dark appearance");
        ToggleButton nightToggle = makeToggle(settings.isNightMode());
        nightToggle.setOnAction(e -> {
            settings.setNightMode(nightToggle.isSelected());
            stage.setScene(new SettingsScreen(stage, user).getScene());
        });
        HBox nightRow = makeSettingRow(nightLabel, nightToggle);




        // Mood theme toggle


        // Show quotes toggle


        // Show golden rule toggle


        // ══════════════════════════════════════════════════════════════════
        // SECTION 2 — PRIVACY & SECURITY
        // ══════════════════════════════════════════════════════════════════
        Label privacyTitle = makeSectionTitle("🔒  Privacy & Security");

        if (user.isGuest()) {
            Label guestMsg = new Label(
                    "⚠️  Create an account to access Privacy & Security settings.");
            guestMsg.setFont(Font.font("Segoe UI", 13));
            guestMsg.setTextFill(Color.web("#F57F17"));
            guestMsg.setWrapText(true);

            VBox privacyCard = makeCard(privacyTitle, makeRowNode(guestMsg));

            VBox aboutCard = buildAboutCard();
            VBox contentBox = buildContentBox(
                    titleLabel,  privacyCard, aboutCard);
            return buildScene(topBar, contentBox);
        }

        // Password verification area
        Label verifyNote = new Label(
                "🔑  Enter your current password to access these settings:");
        verifyNote.setFont(Font.font("Segoe UI", 13));
        verifyNote.setTextFill(Color.web(settings.getSecondaryText()));

        PasswordField verifyField = new PasswordField();
        verifyField.setPromptText("Current password");
        verifyField.setPrefHeight(40);
        verifyField.setStyle(settings.fieldStyle());

        Label verifyError = new Label("");
        verifyError.setFont(Font.font("Segoe UI", 12));
        verifyError.setTextFill(Color.web("#E53935"));
        verifyError.setVisible(false);

        Button unlockBtn = new Button("🔓  Unlock");
        unlockBtn.setStyle(primaryButtonStyle());
        unlockBtn.setPrefHeight(40);
        unlockBtn.setOnMouseEntered(e -> unlockBtn.setStyle(primaryButtonHoverStyle()));
        unlockBtn.setOnMouseExited(e -> unlockBtn.setStyle(primaryButtonStyle()));

        VBox lockedContent = new VBox(16);
        lockedContent.setVisible(false);
        lockedContent.setManaged(false);

        VBox lockBox = new VBox(10,
                verifyNote, verifyField, verifyError, unlockBtn);

        unlockBtn.setOnAction(e -> {
            String pwd = verifyField.getText();
            if (!user.verifyPassword(pwd)) {
                verifyError.setText("Incorrect password.");
                verifyError.setVisible(true);
                return;
            }
            verifyError.setVisible(false);
            lockBox.setVisible(false);
            lockBox.setManaged(false);
            lockedContent.setVisible(true);
            lockedContent.setManaged(true);
        });

        verifyField.setOnAction(e -> unlockBtn.fire());

        // ── Change Username ────────────────────────────────────────────────
        Label changeUserTitle = new Label("👤  Change Username");
        changeUserTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        changeUserTitle.setTextFill(Color.web(settings.getPrimaryText()));

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("New username (min 3 characters)");
        newUsernameField.setPrefHeight(40);
        newUsernameField.setStyle(settings.fieldStyle());

        Label userChangeStatus = new Label("");
        userChangeStatus.setFont(Font.font("Segoe UI", 12));
        userChangeStatus.setVisible(false);

        Button changeUserBtn = new Button("Update Username");
        changeUserBtn.setStyle(secondaryButtonStyle());
        changeUserBtn.setOnMouseEntered(
                e -> changeUserBtn.setStyle(secondaryButtonHoverStyle()));
        changeUserBtn.setOnMouseExited(
                e -> changeUserBtn.setStyle(secondaryButtonStyle()));
        changeUserBtn.setOnAction(e -> {
            String newName = newUsernameField.getText().trim();
            if (newName.length() < 3) {
                showStatus(userChangeStatus,
                        "Username must be at least 3 characters.", "#E53935");
                return;
            }
            try {
                User updated = new User(
                        newName,
                        "",
                        user.getDataFolderPath()) {
                    @Override
                    public String getPasswordHash() {
                        return user.getPasswordHash();
                    }
                };
                FileManager.saveUserIndex(updated);
                showStatus(userChangeStatus,
                        "✅  Username updated! Please log in again.", "#388E3C");
            } catch (Exception ex) {
                showStatus(userChangeStatus,
                        "Error: " + ex.getMessage(), "#E53935");
            }
        });

        VBox changeUserBox = new VBox(8,
                changeUserTitle, newUsernameField,
                userChangeStatus, changeUserBtn);

        // ── Change Password ────────────────────────────────────────────────
        Label changePassTitle = new Label("🔑  Change Password");
        changePassTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        changePassTitle.setTextFill(Color.web(settings.getPrimaryText()));

        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New password (min 6 characters)");
        newPassField.setPrefHeight(40);
        newPassField.setStyle(settings.fieldStyle());

        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm new password");
        confirmPassField.setPrefHeight(40);
        confirmPassField.setStyle(settings.fieldStyle());

        Label passChangeStatus = new Label("");
        passChangeStatus.setFont(Font.font("Segoe UI", 12));
        passChangeStatus.setVisible(false);

        Button changePassBtn = new Button("Update Password");
        changePassBtn.setStyle(secondaryButtonStyle());
        changePassBtn.setOnMouseEntered(
                e -> changePassBtn.setStyle(secondaryButtonHoverStyle()));
        changePassBtn.setOnMouseExited(
                e -> changePassBtn.setStyle(secondaryButtonStyle()));
        changePassBtn.setOnAction(e -> {
            String np = newPassField.getText();
            String cp = confirmPassField.getText();
            if (np.length() < 6) {
                showStatus(passChangeStatus,
                        "Password must be at least 6 characters.", "#E53935");
                return;
            }
            if (!np.equals(cp)) {
                showStatus(passChangeStatus,
                        "Passwords do not match.", "#E53935");
                return;
            }
            showStatus(passChangeStatus,
                    "✅  Password updated! Please log in again.", "#388E3C");
        });

        VBox changePassBox = new VBox(8,
                changePassTitle, newPassField,
                confirmPassField, passChangeStatus, changePassBtn);

        // ── Change Data Folder ─────────────────────────────────────────────
        Label changeFolderTitle = new Label("📁  Change Data Folder");
        changeFolderTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        changeFolderTitle.setTextFill(Color.web(settings.getPrimaryText()));

        Label currentFolderLabel = new Label(
                "Current: " + user.getDataFolderPath());
        currentFolderLabel.setFont(Font.font("Segoe UI", 11));
        currentFolderLabel.setTextFill(Color.web(settings.getMutedText()));
        currentFolderLabel.setWrapText(true);

        Label folderStatus = new Label("");
        folderStatus.setFont(Font.font("Segoe UI", 12));
        folderStatus.setVisible(false);

        Button changeFolderBtn = new Button("📁  Choose New Folder");
        changeFolderBtn.setStyle(secondaryButtonStyle());
        changeFolderBtn.setOnMouseEntered(
                e -> changeFolderBtn.setStyle(secondaryButtonHoverStyle()));
        changeFolderBtn.setOnMouseExited(
                e -> changeFolderBtn.setStyle(secondaryButtonStyle()));
        changeFolderBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose new data folder");
            File chosen = chooser.showDialog(stage);
            if (chosen != null) {
                try {
                    Path oldPath = Paths.get(user.getDataFolderPath());
                    Path newPath = chosen.toPath();
                    java.io.File[] files = oldPath.toFile().listFiles(
                            (d, n) -> n.endsWith(".dat"));
                    if (files != null) {
                        for (java.io.File f : files) {
                            Files.move(f.toPath(),
                                    newPath.resolve(f.getName()),
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                    user.setDataFolderPath(chosen.getAbsolutePath());
                    FileManager.saveUserIndex(user);
                    currentFolderLabel.setText(
                            "Current: " + chosen.getAbsolutePath());
                    showStatus(folderStatus,
                            "✅  Data folder changed successfully!", "#388E3C");
                } catch (Exception ex) {
                    showStatus(folderStatus,
                            "Error: " + ex.getMessage(), "#E53935");
                }
            }
        });

        VBox changeFolderBox = new VBox(8,
                changeFolderTitle, currentFolderLabel,
                folderStatus, changeFolderBtn);

        // ── Danger Zone ────────────────────────────────────────────────────
        Label dangerTitle = new Label("⚠️  Danger Zone");
        dangerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        dangerTitle.setTextFill(Color.web("#E53935"));

        Label dangerNote = new Label(
                "These actions are permanent and cannot be undone.");
        dangerNote.setFont(Font.font("Segoe UI", 12));
        dangerNote.setTextFill(Color.web("#9E9E9E"));

        Label dangerStatus = new Label("");
        dangerStatus.setFont(Font.font("Segoe UI", 12));
        dangerStatus.setVisible(false);
        dangerStatus.setWrapText(true);

        Button clearEntriesBtn = makeDangerButton("🗑️  Clear All Journal Entries");
        clearEntriesBtn.setOnAction(e -> {
            try {
                java.io.File folder = new java.io.File(
                        user.getDataFolderPath());
                java.io.File[] files = folder.listFiles(
                        (d, n) -> n.startsWith("entry_")
                                && n.endsWith(".dat"));
                if (files != null) {
                    for (java.io.File f : files) f.delete();
                }
                showStatus(dangerStatus,
                        "✅  All journal entries cleared.", "#388E3C");
            } catch (Exception ex) {
                showStatus(dangerStatus,
                        "Error: " + ex.getMessage(), "#E53935");
            }
        });

        Button clearVaultBtn = makeDangerButton("🔐  Clear Password Vault");
        clearVaultBtn.setOnAction(e -> {
            try {
                Path vaultPath = Paths.get(
                        user.getDataFolderPath(), "vault.dat");
                Files.deleteIfExists(vaultPath);
                showStatus(dangerStatus,
                        "✅  Password vault cleared.", "#388E3C");
            } catch (Exception ex) {
                showStatus(dangerStatus,
                        "Error: " + ex.getMessage(), "#E53935");
            }
        });

        Button deleteAccountBtn = makeDangerButton("💀  Delete My Account");
        deleteAccountBtn.setOnAction(e -> {
            try {
                java.io.File folder = new java.io.File(
                        user.getDataFolderPath());
                java.io.File[] files = folder.listFiles();
                if (files != null) {
                    for (java.io.File f : files) f.delete();
                }
                Path indexPath = Paths.get(
                        System.getProperty("user.home"),
                        "PsychLog", "index.dat");
                if (Files.exists(indexPath)) {
                    List<String> lines = new java.util.ArrayList<>(
                            Files.readAllLines(indexPath));
                    lines.removeIf(l -> l.startsWith(
                            user.getUsername() + "|"));
                    Files.write(indexPath, lines);
                }
                LoginScreen login = new LoginScreen(stage);
                stage.setScene(login.getScene());
            } catch (Exception ex) {
                showStatus(dangerStatus,
                        "Error: " + ex.getMessage(), "#E53935");
            }
        });

        VBox dangerBox = new VBox(10,
                dangerTitle, dangerNote,
                clearEntriesBtn, clearVaultBtn, deleteAccountBtn,
                dangerStatus);
        dangerBox.setStyle(
                "-fx-background-color: " + (settings.isNightMode() ? "#3E2020" : "#FFF5F5") + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: " + (settings.isNightMode() ? "#7B2C2C" : "#FFCDD2") + ";" +
                        "-fx-border-radius: 12;" +
                        "-fx-padding: 16;");

        lockedContent.getChildren().addAll(
                separator(),
                changeUserBox,
                separator(),
                changePassBox,
                separator(),
                changeFolderBox,
                separator(),
                dangerBox
        );


        VBox generalCard = makeCard(generalTitle, nightRow);
        VBox privacyCard = makeCard(
                privacyTitle,
                makeRowNode(lockBox),
                makeRowNode(lockedContent));

        // ══════════════════════════════════════════════════════════════════
        // SECTION 3 — ABOUT
        // ══════════════════════════════════════════════════════════════════
        VBox aboutCard = buildAboutCard();

        VBox contentBox = buildContentBox(
                titleLabel, generalCard, privacyCard, aboutCard);
        return buildScene(topBar, contentBox);
    }

    // ── About Card ─────────────────────────────────────────────────────────
    private VBox buildAboutCard() {
        Label aboutTitle = makeSectionTitle("ℹ️  About");

        Label appNameLabel = new Label("🧠  PsychLog");
        appNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        appNameLabel.setTextFill(Color.web("#5C6BC0"));

        Label versionLabel = new Label("Version " +
                new String(java.util.Base64.getDecoder()
                        .decode("MS4w")));
        versionLabel.setFont(Font.font("Segoe UI", 13));
        versionLabel.setTextFill(Color.web(settings.getMutedText()));

        String sig = ThemeManager.getSignature();
        String devName = sig.contains("—") ? sig.split("—")[0].trim() : sig;
        Label devLabel = new Label("Developed by  " + devName);
        devLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        devLabel.setTextFill(Color.web(settings.getSecondaryText()));

        Region divider1 = new Region();
        divider1.setPrefHeight(1);
        divider1.setStyle("-fx-background-color: #EDE7F6;");

        Region divider2 = new Region();
        divider2.setPrefHeight(1);
        divider2.setStyle("-fx-background-color: #EDE7F6;");

        Label goldenRuleTitle = new Label("🔐  The Golden Rule of Security");
        goldenRuleTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        goldenRuleTitle.setTextFill(Color.web("#5C6BC0"));

        Label goldenRuleText = new Label(
                "Security is not about being uncrackable —\n" +
                        "it's about making the attack too expensive\n" +
                        "and too slow to be worth it.");
        goldenRuleText.setFont(Font.font("Segoe UI Italic", 14));
        goldenRuleText.setTextFill(Color.web("#7E57C2"));
        goldenRuleText.setWrapText(true);
        goldenRuleText.setStyle(
                "-fx-background-color: " + (settings.isNightMode() ? "#2A2A3E" : "#EDE7F6") + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 16;");
        goldenRuleText.setTextFill(Color.web(settings.isNightMode() ? "#CE93D8" : "#7E57C2"));

        Label copyrightLabel = new Label(
                "© 2026 PsychLog. Built with 💜 for mental wellness.");
        copyrightLabel.setFont(Font.font("Segoe UI", 11));
        copyrightLabel.setTextFill(Color.web(settings.getMutedText()));

        VBox aboutContent = new VBox(12,
                appNameLabel, versionLabel, devLabel,
                divider1,
                goldenRuleTitle, goldenRuleText,
                divider2,
                copyrightLabel);

        return makeCard(aboutTitle, makeRowNode(aboutContent));
    }

    // ── Builders ───────────────────────────────────────────────────────────
    private VBox buildContentBox(javafx.scene.Node... nodes) {
        VBox box = new VBox(20, nodes);
        box.setPadding(new Insets(28));
        return box;
    }

    private Scene buildScene(HBox topBar, VBox contentBox) {
        ScrollPane scroll = new ScrollPane(contentBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + settings.getBg() +
                "; -fx-background-color: " + settings.getBg() + ";");

        VBox root = new VBox(topBar, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        root.setStyle(settings.rootStyle());

        return new Scene(root);
    }

    private VBox makeCard(javafx.scene.Node title,
                          javafx.scene.Node... rows) {
        VBox card = new VBox(14);
        card.getChildren().add(title);
        card.getChildren().add(separator());
        for (javafx.scene.Node row : rows) {
            card.getChildren().add(row);
        }
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setStyle(settings.cardStyle());
        return card;
    }

    private HBox makeSettingRow(Label labelBox, ToggleButton toggle) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(spacer, toggle);
        row.setAlignment(Pos.CENTER_LEFT);

        VBox full = new VBox(4, labelBox);
        HBox outer = new HBox(full, spacer, toggle);
        outer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(full, Priority.ALWAYS);
        return outer;
    }

    private javafx.scene.Node makeRowNode(javafx.scene.Node node) {
        return node;
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private Label makeSectionTitle(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        label.setTextFill(Color.web("#5C6BC0"));
        return label;
    }

    private Label makeSettingLabel(String title, String subtitle) {
        Label label = new Label(title + "\n" + subtitle);
        label.setFont(Font.font("Segoe UI", 13));
        label.setTextFill(Color.web(settings.getPrimaryText()));
        label.setWrapText(true);
        return label;
    }

    private ToggleButton makeToggle(boolean initialState) {
        ToggleButton btn = new ToggleButton(initialState ? "ON" : "OFF");
        btn.setSelected(initialState);
        btn.setStyle(initialState ? toggleOnStyle() : toggleOffStyle());
        btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            btn.setText(newVal ? "ON" : "OFF");
            btn.setStyle(newVal ? toggleOnStyle() : toggleOffStyle());
        });
        return btn;
    }

    private Button makeDangerButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(40);
        btn.setStyle(dangerButtonStyle());
        btn.setOnMouseEntered(e -> btn.setStyle(dangerButtonHoverStyle()));
        btn.setOnMouseExited(e -> btn.setStyle(dangerButtonStyle()));
        return btn;
    }

    private void showStatus(Label label, String message, String color) {
        label.setText(message);
        label.setTextFill(Color.web(color));
        label.setVisible(true);
    }

    private Region separator() {
        Region sep = new Region();
        sep.setPrefHeight(1);
        sep.setStyle("-fx-background-color: " +
                settings.getBorderColor() + ";");
        return sep;
    }

    // ── Styles ─────────────────────────────────────────────────────────────
    private String toggleOnStyle() {
        return "-fx-background-color: #5C6BC0;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 6 18 6 18;" +
                "-fx-cursor: hand;";
    }

    private String toggleOffStyle() {
        return "-fx-background-color: #E0E0E0;" +
                "-fx-text-fill: #9E9E9E;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-padding: 6 18 6 18;" +
                "-fx-cursor: hand;";
    }

    private String primaryButtonStyle() {
        return "-fx-background-color: #5C6BC0;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;";
    }

    private String primaryButtonHoverStyle() {
        return "-fx-background-color: #3F51B5;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 20;" +
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

    private String dangerButtonStyle() {
        return "-fx-background-color: #FFEBEE;" +
                "-fx-text-fill: #E53935;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #FFCDD2;" +
                "-fx-border-radius: 18;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 16 6 16;";
    }

    private String dangerButtonHoverStyle() {
        return "-fx-background-color: #E53935;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: #E53935;" +
                "-fx-border-radius: 18;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 16 6 16;";
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