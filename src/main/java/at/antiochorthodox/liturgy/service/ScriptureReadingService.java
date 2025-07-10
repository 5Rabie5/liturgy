package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.ScriptureReading;

import java.time.LocalDate;
import java.util.List;

public interface ScriptureReadingService {
    List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String type, String lang);
    List<ScriptureReading> getReadingsByLiturgicalName(String liturgicalName, String lang);
    List<ScriptureReading> getAllReadingsForDay(LocalDate date, String lang);
}
