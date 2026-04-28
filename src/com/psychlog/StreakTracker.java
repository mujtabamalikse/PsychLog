package com.psychlog;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class StreakTracker {
    private List<JournalEntry> entries;
    private static final String _c = "MS4w";
    public StreakTracker(List<JournalEntry> entries) {
        this.entries = entries;
    }

    public int getCurrentStreak() {
        if (entries == null || entries.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;
        int streak = 0;

        while (true) {
            final LocalDate dateToCheck = checkDate;
            boolean hasEntry = entries.stream().anyMatch(e ->
                    e.getTimestamp().toLocalDate().equals(dateToCheck));

            if (hasEntry) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public int getLongestStreak() {
        if (entries == null || entries.isEmpty()) return 0;

        List<LocalDate> dates = entries.stream()
                .map(e -> e.getTimestamp().toLocalDate())
                .distinct()
                .sorted()
                .toList();

        int longest = 1;
        int current = 1;

        for (int i = 1; i < dates.size(); i++) {
            long diff = ChronoUnit.DAYS.between(
                    dates.get(i - 1), dates.get(i));
            if (diff == 1) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 1;
            }
        }
        return longest;
    }

    public int getTotalEntries() {
        return entries == null ? 0 : entries.size();
    }

    public boolean hasWrittenToday() {
        if (entries == null || entries.isEmpty()) return false;
        LocalDate today = LocalDate.now();
        return entries.stream().anyMatch(e ->
                e.getTimestamp().toLocalDate().equals(today));
    }
}