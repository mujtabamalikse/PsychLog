package com.psychlog;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MoodAnalyser {

    private final List<JournalEntry> entries;

    public MoodAnalyser(List<JournalEntry> entries) {
        this.entries = entries.stream()
                .sorted(Comparator.comparing(JournalEntry::getTimestamp))
                .collect(Collectors.toList());
    }

    // Most frequent mood in last 7 days
    public String getDominantMood() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        Map<String, Long> counts = entries.stream()
                .filter(e -> e.getTimestamp().isAfter(weekAgo))
                .collect(Collectors.groupingBy(JournalEntry::getMood, Collectors.counting()));
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
    }

    // Improving, Declining, Stable
    public String getMoodVelocity() {
        LocalDateTime now = LocalDateTime.now();
        List<JournalEntry> lastWeek = entries.stream()
                .filter(e -> e.getTimestamp().isAfter(now.minusDays(7)))
                .collect(Collectors.toList());
        List<JournalEntry> prevWeek = entries.stream()
                .filter(e -> e.getTimestamp().isAfter(now.minusDays(14))
                        && e.getTimestamp().isBefore(now.minusDays(7)))
                .collect(Collectors.toList());

        double lastScore = averageMoodScore(lastWeek);
        double prevScore = averageMoodScore(prevWeek);

        if (prevWeek.isEmpty()) return "Stable";
        if (lastScore > prevScore + 0.5) return "Improving";
        if (lastScore < prevScore - 0.5) return "Declining";
        return "Stable";
    }

    // Morning, Afternoon, Evening, Night
    public String getWritingTimePattern() {
        Map<String, Long> timeCounts = new HashMap<>();
        for (JournalEntry e : entries) {
            int hour = e.getTimestamp().getHour();
            String period;
            if (hour >= 5 && hour < 12) period = "Morning";
            else if (hour >= 12 && hour < 17) period = "Afternoon";
            else if (hour >= 17 && hour < 21) period = "Evening";
            else period = "Night";
            timeCounts.merge(period, 1L, Long::sum);
        }
        return timeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Evening");
    }

    // Average words per entry trend
    public String getWritingLengthTrend() {
        if (entries.size() < 4) return "Stable";
        int half = entries.size() / 2;
        double firstHalf = entries.subList(0, half).stream()
                .mapToInt(e -> e.getContent().split("\\s+").length)
                .average().orElse(0);
        double secondHalf = entries.subList(half, entries.size()).stream()
                .mapToInt(e -> e.getContent().split("\\s+").length)
                .average().orElse(0);
        if (secondHalf > firstHalf * 1.2) return "Growing";
        if (secondHalf < firstHalf * 0.8) return "Shrinking";
        return "Stable";
    }

    // Total entries this week
    public int getEntriesThisWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return (int) entries.stream()
                .filter(e -> e.getTimestamp().isAfter(weekAgo))
                .count();
    }

    // Weekly insight paragraph
    public String getWeeklyInsight() {
        String mood = getDominantMood();
        String velocity = getMoodVelocity();
        String time = getWritingTimePattern();
        String length = getWritingLengthTrend();
        int count = getEntriesThisWeek();

        StringBuilder sb = new StringBuilder();
        sb.append("This week you wrote ").append(count)
                .append(count == 1 ? " entry" : " entries").append(". ");

        sb.append("Your dominant mood was ").append(mood).append(". ");

        if (velocity.equals("Improving"))
            sb.append("Your mood has been improving compared to last week — keep going. ");
        else if (velocity.equals("Declining"))
            sb.append("Your mood has dipped compared to last week. Be gentle with yourself. ");
        else
            sb.append("Your mood has been steady this week. ");

        sb.append("You tend to write most in the ").append(time).append(". ");

        if (length.equals("Growing"))
            sb.append("Your entries are getting longer — you're opening up more.");
        else if (length.equals("Shrinking"))
            sb.append("Your entries are getting shorter. That's okay too.");
        else
            sb.append("Your writing length has been consistent.");

        return sb.toString();
    }

    private double averageMoodScore(List<JournalEntry> list) {
        if (list.isEmpty()) return 0;
        return list.stream().mapToInt(e -> moodToScore(e.getMood()))
                .average().orElse(0);
    }

    private int moodToScore(String mood) {
        switch (mood) {
            case "Happy": return 5;
            case "Calm": return 4;
            case "Neutral": return 3;
            case "Anxious": return 2;
            case "Sad": return 1;
            case "Angry": return 1;
            default: return 3;
        }
    }
}