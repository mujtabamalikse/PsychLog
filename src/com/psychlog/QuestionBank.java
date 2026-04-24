package com.psychlog;

import java.util.*;

public class QuestionBank {
    private Map<String, List<String>> questions;
    private Random random;

    public QuestionBank() {
        random = new Random();
        questions = new HashMap<>();

        questions.put("Happy", Arrays.asList(
                "What made you feel so good yesterday?",
                "How can you keep this positive energy going today?",
                "Did you share your happiness with someone?",
                "What are you most grateful for right now?",
                "How can you make someone else smile today?"
        ));

        questions.put("Sad", Arrays.asList(
                "What brought your energy down yesterday?",
                "Did you talk to anyone you trust about how you feel?",
                "What is one small thing that could brighten your day?",
                "Are you being too hard on yourself lately?",
                "What do you need most right now?"
        ));

        questions.put("Anxious", Arrays.asList(
                "What made you feel most overwhelmed yesterday?",
                "Did you get enough sleep last night?",
                "What is one thing worrying you the most today?",
                "Did you eat and drink water properly yesterday?",
                "What would make today feel calmer for you?"
        ));

        questions.put("Angry", Arrays.asList(
                "What triggered your frustration yesterday?",
                "Did you get a chance to calm down and breathe?",
                "Is there someone you need to talk to about how you feel?",
                "What would help you feel more at peace today?",
                "Can you look at yesterday's situation differently?"
        ));

        questions.put("Calm", Arrays.asList(
                "What helped you feel so peaceful yesterday?",
                "How can you maintain this calm energy today?",
                "Did you do anything relaxing that you enjoyed?",
                "What is one thing you appreciate about your life?",
                "How can you share this peaceful energy with others?"
        ));

        questions.put("Neutral", Arrays.asList(
                "How are you truly feeling deep inside today?",
                "What is one goal you want to achieve today?",
                "Is there something on your mind you haven't expressed?",
                "What would make today a really good day for you?",
                "What is one thing you are looking forward to?"
        ));
    }

    public List<String> getQuestionsForMood(String mood) {
        List<String> moodQuestions = questions.getOrDefault(
                mood, questions.get("Neutral"));
        List<String> shuffled = new ArrayList<>(moodQuestions);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, Math.min(5, shuffled.size()));
    }
}