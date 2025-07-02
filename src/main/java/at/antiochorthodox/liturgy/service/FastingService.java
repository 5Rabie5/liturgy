package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Fasting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FastingService {
    Optional<Fasting> getById(String id);
    List<Fasting> getByLanguage(String lang);
    List<Fasting> getByLanguageAndType(String lang, String type);
    List<Fasting> getWeeklyFasting(String lang);
    Fasting save(Fasting fasting);
    List<Fasting> saveAll(List<Fasting> fastingList);
    void deleteById(String id);
    List<Fasting> getAllFastingForYear(String lang, int year);
    Optional<Fasting> getFastingForDate(String lang, LocalDate date);
    String getFastingTypeByLangAndDate(String lang, LocalDate date );

    String getFastingEvelByLangAndDate(String lang, LocalDate date);
}