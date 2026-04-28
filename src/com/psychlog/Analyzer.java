package com.psychlog;

import java.util.*;

public class Analyzer {
    private MoodDictionary moodDictionary;

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
                    score++;
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
                    score++;
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