package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;

import java.time.LocalDate;

public interface ReadingContextService {

    ReadingContext resolveForDate(LocalDate date, String lang, String tradition);

    ReadingContext resolveForDate(LocalDate date, String lang, String tradition, String slot);
}
