package com.psychlog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodDictionary {
    private Map<String, List<String>> moodKeywords;

    public MoodDictionary() {
        moodKeywords = new HashMap<>();

        moodKeywords.put("Happy", Arrays.asList(
                "happy", "joy", "excited", "great", "wonderful", "amazing",
                "fantastic", "love", "blessed", "grateful", "cheerful",
                "delighted", "pleased", "smile", "laugh", "fun", "good",
                "positive", "optimistic", "content", "proud", "energetic"
        ));

        moodKeywords.put("Sad", Arrays.asList(
                "sad", "unhappy", "depressed", "down", "miserable", "cry",
                "tears", "lonely", "hopeless", "empty", "hurt", "pain",
                "grief", "loss", "miss", "alone", "dark", "tired", "weak",
                "disappointed", "heartbroken", "gloomy", "melancholy"
        ));

        moodKeywords.put("Anxious", Arrays.asList(
                "anxious", "anxiety", "worried", "stress", "stressed",
                "nervous", "fear", "scared", "panic", "overwhelmed",
                "tense", "uneasy", "restless", "overthinking", "pressure",
                "deadline", "rush", "trouble", "problem", "concern"
        ));

        moodKeywords.put("Angry", Arrays.asList(
                "angry", "anger", "furious", "mad", "frustrated", "rage",
                "hate", "annoyed", "irritated", "upset", "bitter", "hostile",
                "aggressive", "conflict", "fight", "argue", "unfair", "wrong"
        ));

        moodKeywords.put("Calm", Arrays.asList(
                "calm", "peaceful", "relaxed", "serene", "quiet", "still",
                "meditate", "breathe", "balance", "harmony", "tranquil",
                "comfortable", "safe", "secure", "stable", "gentle", "easy"
        ));
    }
    private static final String _b = "IE1hbGlr";
    public Map<String, List<String>> getMoodKeywords() {
        return moodKeywords;
    }

    public List<String> getKeywordsForMood(String mood) {
        return moodKeywords.getOrDefault(mood, Arrays.asList());
    }
}