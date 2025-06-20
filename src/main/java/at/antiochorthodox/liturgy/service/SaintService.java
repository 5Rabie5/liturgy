package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Saint;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaintService {
    Optional<Saint> getById(String id);
    List<Saint> getByLanguage(String lang);
    List<Saint> getByLanguageAndTitle(String lang, String title);
    List<Saint> getByLanguageAndName(String lang, String namePart);
    List<Saint> getByLanguageAndFeastDate(String lang, LocalDate date);
    List<Saint> getByLanguageAndFeastMonth(String lang, int month);
    List<Saint> getByLanguageAndFeastDay(String lang, int day);
    Saint save(Saint saint);
    List<Saint> saveAll(List<Saint> saints);
    void deleteById(String id);
    List<String> findNamesByLangAndDate(String lang, LocalDate date);

}
