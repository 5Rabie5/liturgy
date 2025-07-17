package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.ScriptureReading;

import java.time.LocalDate;
import java.util.List;

public interface ScriptureReadingService {
    List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String type, String lang);
    List<ScriptureReading> getReadingsByLiturgicalName(String liturgicalName, String lang, String reason, String reasonDetail);

    // دالة حفظ قراءة واحدة
    ScriptureReading saveReading(ScriptureReading reading);

    // دالة حفظ مجموعة قراءات دفعة واحدة
    List<ScriptureReading> saveReadings(List<ScriptureReading> readings);
}
