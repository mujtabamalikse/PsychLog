package com.psychlog;

import java.util.*;

public class Analyzer {
    private MoodDictionary moodDictionary;
    private static final Map<String, Integer> WEIGHTS = new HashMap<>();
    static {
        WEIGHTS.put("hopeless", 3); WEIGHTS.put("devastated", 3);
        WEIGHTS.put("ecstatic", 3); WEIGHTS.put("overjoyed", 3);
        WEIGHTS.put("terrified", 3); WEIGHTS.put("furious", 3);
        WEIGHTS.put("sad", 2); WEIGHTS.put("happy", 2);
        WEIGHTS.put("angry", 2); WEIGHTS.put("anxious", 2);
        WEIGHTS.put("calm", 2); WEIGHTS.put("depressed", 2);
    }

    public Analyzer() {
        this.moodDictionary = new MoodDictionary();
    }

    public String detectMood(String text) {
        String lowerText = text.toLowerCase();
        Map<String, Integer> moodScores = new HashMap<>();
        Map<String, List<String>> keywords = moodDictionary.getMoodKeywords();

        for (Map.Entry<String, List<String>> entry : keywords.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    // Check if negated — "not happy", "not good", "not feeling"
                    String negated = "not " + keyword;
                    String negated2 = "n't " + keyword;
                    if (lowerText.contains(negated) || lowerText.contains(negated2)) {
                        score -= WEIGHTS.getOrDefault(keyword, 1); // subtract instead
                    } else {
                        score += WEIGHTS.getOrDefault(keyword, 1);
                    }
                }
            }
            moodScores.put(entry.getKey(), score);
        }

        String dominantMood = "Neutral";
        int maxScore = 0;
        for (Map.Entry<String, Integer> entry : moodScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                dominantMood = entry.getKey();
            }
        }
        return dominantMood;
    }

    public Map<String, Integer> getMoodScores(String text) {
        String lowerText = text.toLowerCase();
        Map<String, Integer> moodScores = new HashMap<>();
        Map<String, List<String>> keywords = moodDictionary.getMoodKeywords();

        for (Map.Entry<String, List<String>> entry : keywords.entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (lowerText.contains(keyword)) {
                    // Check if negated — "not happy", "not good", "not feeling"
                    String negated = "not " + keyword;
                    String negated2 = "n't " + keyword;
                    if (lowerText.contains(negated) || lowerText.contains(negated2)) {
                        score -= WEIGHTS.getOrDefault(keyword, 1); // subtract instead
                    } else {
                        score += WEIGHTS.getOrDefault(keyword, 1);
                    }
                }
            }
            moodScores.put(entry.getKey(), score);
        }
        return moodScores;
    }

    public List<String> extractKeywords(String text) {
        String lowerText = text.toLowerCase();
        List<String> found = new ArrayList<>();
        Map<String, List<String>> keywords = moodDictionary.getMoodKeywords();

        for (List<String> wordList : keywords.values()) {
            for (String keyword : wordList) {
                if (lowerText.contains(keyword)) {
                    found.add(keyword);
                }
            }
        }
        return found;
    }

    public Map<String, Long> getMoodDistribution(
            List<JournalEntry> entries) {
        Map<String, Long> distribution = new HashMap<>();
        for (JournalEntry entry : entries) {
            String mood = entry.getMood();
            distribution.put(mood,
                    distribution.getOrDefault(mood, 0L) + 1);
        }
        return distribution;
    }
}