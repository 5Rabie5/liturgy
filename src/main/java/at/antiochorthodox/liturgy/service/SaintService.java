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

    List<Saint> getByLanguageAndFeastDate(String lang, int month, int day); // جديد: بحث يوم/شهر
    List<Saint> getByLanguageAndFeastDate(String lang, LocalDate date);     // بديل: بحث بـ LocalDate
    List<Saint> getByLanguageAndFeastMonth(String lang, int month);         // بحث حسب الشهر

    Saint save(Saint saint);
    List<Saint> saveAll(List<Saint> saints);
    void deleteById(String id);

    List<String> findNamesByLangAndDate(String lang, LocalDate date);
}
