package com.psychlog;

import java.util.Arrays;
import java.util.List;

public class DarkEntryDetector {

    private static final List<String> DARK_WORDS = Arrays.asList(
            "hopeless", "worthless", "empty", "numb", "trapped", "invisible",
            "burden", "pointless", "meaningless", "nothing", "nobody cares",
            "give up", "can't go on", "no point", "end it", "disappear",
            "hate myself", "useless", "broken", "destroyed", "suffocating",
            "drowning", "hollow", "dead inside", "no way out", "exhausted",
            "done", "tired of everything", "can't take it", "falling apart"
    );

    private static final List<String> CRISIS_WORDS = Arrays.asList(
            "suicide", "kill myself", "end my life", "don't want to live",
            "want to die", "self harm", "hurt myself", "cut myself"
    );

    public enum Level {
        NONE, DARK, CRISIS
    }

    public Level detect(String content) {
        String lower = content.toLowerCase();
        for (String word : CRISIS_WORDS) {
            if (lower.contains(word)) return Level.CRISIS;
        }
        int darkCount = 0;
        for (String word : DARK_WORDS) {
            if (lower.contains(word)) darkCount++;
        }
        if (darkCount >= 2) return Level.DARK;
        return Level.NONE;
    }

    public String getResponse(Level level) {
        if (level == Level.CRISIS) {
            return "💙  You matter. Whatever you're going through right now, " +
                    "please reach out to someone you trust. You don't have to face this alone.";
        }
        if (level == Level.DARK) {
            return "💙  It sounds like things are really heavy right now. " +
                    "Whatever you're feeling is valid. Be gentle with yourself today.";
        }
        return "";
    }
}