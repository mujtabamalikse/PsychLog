package com.psychlog;

public class AppSettings {

    // ── Singleton ──────────────────────────────────────────────────────────
    private static AppSettings instance;

    public static AppSettings get() {
        if (instance == null) instance = new AppSettings();
        return instance;
    }

    private AppSettings() {}

    // ── Settings ───────────────────────────────────────────────────────────
    private boolean nightMode      = false;
    private boolean rippleEnabled  = true;
    private boolean soundEnabled   = true;
    private boolean moodTheme      = true;
    private boolean showQuotes     = true;
    private boolean showGoldenRule = true;

    // ── Getters & Setters ──────────────────────────────────────────────────
    public boolean isNightMode()             { return nightMode; }
    public void setNightMode(boolean v)      { nightMode = v; }

    public boolean isMoodTheme()             { return moodTheme; }
    public void setMoodTheme(boolean v)      { moodTheme = v; }

    public boolean isShowQuotes()            { return showQuotes; }
    public void setShowQuotes(boolean v)     { showQuotes = v; }

    public boolean isShowGoldenRule()        { return showGoldenRule; }
    public void setShowGoldenRule(boolean v) { showGoldenRule = v; }

    public boolean isRippleEnabled()         { return rippleEnabled; }
    public void setRippleEnabled(boolean v)  { rippleEnabled = v; }

    public boolean isSoundEnabled()          { return soundEnabled; }
    public void setSoundEnabled(boolean v)   { soundEnabled = v; }

    // ── Theme helpers ──────────────────────────────────────────────────────
    public String getBg() {
        return nightMode ? "#1E1E2E" : "#F3F0FB";
    }

    public String getCardBg() {
        return nightMode ? "#2A2A3E" : "#FFFFFF";
    }

    public String getTopBarBg() {
        return nightMode ? "#16161E" : "#5C6BC0";
    }

    public String getPrimaryText() {
        return nightMode ? "#E0E0E0" : "#37474F";
    }

    public String getSecondaryText() {
        return nightMode ? "#9E9E9E" : "#616161";
    }

    public String getMutedText() {
        return nightMode ? "#616161" : "#9E9E9E";
    }

    public String getBorderColor() {
        return nightMode ? "#3A3A4E" : "#E0E0E0";
    }

    public String getFieldBg() {
        return nightMode ? "#2A2A3E" : "#F5F5F5";
    }

    public String cardStyle() {
        return "-fx-background-color: " + getCardBg() + ";" +
                "-fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 12, 0, 0, 4);";
    }

    public String topBarStyle() {
        return "-fx-background-color: " + getTopBarBg() + ";" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);";
    }

    public String rootStyle() {
        return "-fx-background-color: " + getBg() + ";";
    }

    public String fieldStyle() {
        return "-fx-background-color: " + getFieldBg() + ";" +
                "-fx-background-radius: 10;" +
                "-fx-border-color: " + getBorderColor() + ";" +
                "-fx-border-radius: 10;" +
                "-fx-padding: 8 14 8 14;" +
                "-fx-font-size: 13px;" +
                "-fx-font-family: 'Segoe UI';" +
                "-fx-text-fill: " + getPrimaryText() + ";" +
                "-fx-control-inner-background: " + getFieldBg() + ";" +
                "-fx-prompt-text-fill: " + (isNightMode() ? "rgba(255,255,255,0.45)" : "rgba(0,0,0,0.35)") + ";";
    }

    // ── NEW: Apply field style to TextField/TextArea/PasswordField ─────────
    // JavaFX ignores -fx-text-fill in stylesheets for text inputs,
    // so we must apply text color directly on the control node.
    public void applyFieldStyle(javafx.scene.control.TextInputControl field) {
        field.setStyle(fieldStyle());
        if (nightMode) {
            field.setStyle(fieldStyle());
            // Force text color via lookup after scene is shown
            field.styleProperty().set(fieldStyle());
        }
        field.lookupAll(".text").forEach(node ->
                node.setStyle("-fx-fill: " + getPrimaryText() + ";"));
    }

    // ── Apply to any TextField ─────────────────────────────────────────────
    public void styleField(javafx.scene.control.TextInputControl field) {
        field.setStyle(fieldStyle());
        field.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                field.lookupAll(".text").forEach(n ->
                        n.setStyle("-fx-fill: " + getPrimaryText() + ";"));
            }
        });
        // Also apply immediately if skin already exists
        field.lookupAll(".text").forEach(n ->
                n.setStyle("-fx-fill: " + getPrimaryText() + ";"));
    }
}