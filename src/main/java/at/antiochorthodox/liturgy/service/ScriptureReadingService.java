package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.ScriptureReading;

import java.time.LocalDate;
import java.util.List;

/**
 * Legacy text/day-key reading service.
 *
 * <p>This service is still needed for older grouped calendar responses and
 * name-based lookups. New assignment-driven service selection should happen
 * through the v2 reading services.</p>
 */
public interface ScriptureReadingService {
    List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String type, String lang);
    List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String slot, String type, String lang);
    List<ScriptureReading> getReadingsByDayKey(String dayKey, String type, String lang);
    List<ScriptureReading> getReadingsByDayKey(String dayKey, String slot, String type, String lang);
    List<ScriptureReading> getReadingsByLiturgicalName(String liturgicalName, String type, String lang);

    /**
     * Direct lookup by readingKey.
     * Used when a transitional override explicitly selects epistle/gospel keys.
     */
    ScriptureReading getReadingByKey(String readingKey, String lang);

    ScriptureReading saveReading(ScriptureReading reading);
    List<ScriptureReading> saveReadings(List<ScriptureReading> readings);

    /**
     * Explicitly named legacy-name lookup used by fixed feasts, movable feasts,
     * and saint reading groups until those are also modeled with assignments.
     */
    List<ScriptureReading> getReadingsByLegacyName(String liturgicalName, String type, String lang);
}