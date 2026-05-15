package com.psychlog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {
    private List<JournalEntry> entries;
    private Analyzer analyzer;

    public ReportGenerator(List<JournalEntry> entries) {
        this.entries = entries;
        this.analyzer = new Analyzer();
    }

    public String generateMonthlyReport(int month, int year) {
        List<JournalEntry> monthEntries = entries.stream()
                .filter(e -> e.getTimestamp().getMonthValue() == month
                        && e.getTimestamp().getYear() == year)
                .collect(Collectors.toList());

        if (monthEntries.isEmpty()) {
            return "No entries found for this month.";
        }

        StringBuilder report = new StringBuilder();
        String monthName = LocalDate.of(year, month, 1)
                .format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        report.append("=== PsychLog Monthly Report ===\n");
        report.append("Month: ").append(monthName).append("\n");
        report.append("Total Entries: ")
                .append(monthEntries.size()).append("\n\n");

        Map<String, Long> moodDist = analyzer
                .getMoodDistribution(monthEntries);
        report.append("--- Mood Distribution ---\n");
        moodDist.forEach((mood, count) ->
                report.append(ThemeManager.getMoodEmoji(mood))
                        .append(" ").append(mood).append(": ")
                        .append(count).append(" entries\n"));

        String dominantMood = moodDist.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
        report.append("\nDominant Mood: ")
                .append(dominantMood).append("\n\n");

        report.append("--- Journal Entries ---\n");
        monthEntries.forEach(e -> {
            report.append("\n📅 ")
                    .append(e.getFormattedTimestamp()).append("\n");
            report.append("Mood: ")
                    .append(ThemeManager.getMoodEmoji(e.getMood()))
                    .append(" ").append(e.getMood()).append("\n");
            report.append(e.getContent()).append("\n");
            report.append("------------------------\n");
        });

        return report.toString();
    }

    public String generateFullReport() {
        if (entries.isEmpty()) return "No entries found.";

        StringBuilder report = new StringBuilder();
        report.append("=== PsychLog Full Report ===\n");
        report.append("Total Entries: ")
                .append(entries.size()).append("\n\n");

        Map<String, Long> moodDist = analyzer
                .getMoodDistribution(entries);
        report.append("--- Overall Mood Distribution ---\n");
        moodDist.forEach((mood, count) ->
                report.append(ThemeManager.getMoodEmoji(mood))
                        .append(" ").append(mood).append(": ")
                        .append(count).append(" entries\n"));

        return report.toString();
    }
}