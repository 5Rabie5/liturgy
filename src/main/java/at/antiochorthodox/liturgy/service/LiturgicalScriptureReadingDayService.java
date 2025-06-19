package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalScriptureReadingDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LiturgicalScriptureReadingDayService {
    Optional<LiturgicalScriptureReadingDay> findByDate(LocalDate date);
    List<LiturgicalScriptureReadingDay> findBySeason(String season);
    LiturgicalScriptureReadingDay save(LiturgicalScriptureReadingDay day);
    List<LiturgicalScriptureReadingDay> findAll();
    void deleteById(String id);
}
