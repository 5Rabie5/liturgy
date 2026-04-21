package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;

import java.time.LocalDate;
import java.util.List;

public interface ReadingDayKeyResolver {
    ReadingDayKeyResolution resolve(String calendarDayKey, LocalDate date, String slot);

    List<String> resolveLookupDayKeys(String calendarDayKey, LocalDate date, String slot);
}
