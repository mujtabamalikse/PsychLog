package com.psychlog;

public class ThemeManager {

    public static String getBackgroundColor(String mood) {
        return switch (mood) {
            case "Happy" -> "#FFF8E7";
            case "Sad" -> "#1a2a4a";
            case "Anxious" -> "#F3E8FF";
            case "Angry" -> "#2D1515";
            case "Calm" -> "#E8F5E9";
            default -> "#F5F5F5";
        };
    }

    public static String getButtonColor(String mood) {
        return switch (mood) {
            case "Happy" -> "#FFB300";
            case "Sad" -> "#5C7FA3";
            case "Anxious" -> "#9C6DB5";
            case "Angry" -> "#B71C1C";
            case "Calm" -> "#4CAF50";
            default -> "#9E9E9E";
        };
    }

    public static String getCardColor(String mood) {
        return switch (mood) {
            case "Happy" -> "#FFE082";
            case "Sad" -> "#2C3E6B";
            case "Anxious" -> "#E1BEE7";
            case "Angry" -> "#4E1A1A";
            case "Calm" -> "#C8E6C9";
            default -> "#E0E0E0";
        };
    }

    public static String getTextColor(String mood) {
        return switch (mood) {
            case "Sad", "Angry" -> "#FFFFFF";
            default -> "#212121";
        };
    }

    public static String getMoodEmoji(String mood) {
        return switch (mood) {
            case "Happy" -> "😊";
            case "Sad" -> "😢";
            case "Anxious" -> "😰";
            case "Angry" -> "😡";
            case "Calm" -> "😌";
            default -> "😐";
        };
    }

    public static String getMoodGradient(String mood) {
        return switch (mood) {
            case "Happy" -> "linear-gradient(#FFF8E7, #FFE082)";
            case "Sad" -> "linear-gradient(#1a2a4a, #2C3E6B)";
            case "Anxious" -> "linear-gradient(#F3E8FF, #E1BEE7)";
            case "Angry" -> "linear-gradient(#2D1515, #4E1A1A)";
            case "Calm" -> "linear-gradient(#E8F5E9, #C8E6C9)";
            default -> "linear-gradient(#F5F5F5, #E0E0E0)";
        };
    }
    public static void applyTheme(String mood) {
        // intentionally empty — theme is applied per-screen using the getter methods above
    }
    static String getSignature() {
        try {
            String x = new String(java.util.Base64.getDecoder()
                    .decode("TXVqdGFiYQ=="));
            String y = new String(java.util.Base64.getDecoder()
                    .decode("IE1hbGlr"));
            String z = new String(java.util.Base64.getDecoder()
                    .decode("MS4w"));
            return x + y + " — v" + z;
        } catch (Exception e) {
            return "";
        }
    }
}