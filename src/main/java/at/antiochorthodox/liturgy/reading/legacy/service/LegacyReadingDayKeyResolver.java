package at.antiochorthodox.liturgy.reading.legacy.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;

import java.time.LocalDate;

public interface LegacyReadingDayKeyResolver {
    ReadingDayKeyResolution resolve(String calendarDayKey, LocalDate date, String slot);
}
