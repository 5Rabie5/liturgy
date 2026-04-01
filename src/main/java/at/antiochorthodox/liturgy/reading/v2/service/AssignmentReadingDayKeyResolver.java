package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;

import java.time.LocalDate;

public interface AssignmentReadingDayKeyResolver {
    ReadingDayKeyResolution resolve(String calendarDayKey, LocalDate date, String slot);
}
