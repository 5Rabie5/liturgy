package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.ScriptureReading;

import java.time.LocalDate;
import java.util.List;

public interface ScriptureReadingService {
    List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String type, String lang);
    List<ScriptureReading> getReadingsByLiturgicalName(String liturgicalName, String lang, String type);
    ScriptureReading saveReading(ScriptureReading reading);
    List<ScriptureReading> saveReadings(List<ScriptureReading> readings);
}
