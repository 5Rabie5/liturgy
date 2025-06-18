package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Feast;
import at.antiochorthodox.liturgy.repository.FeastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface FeastService {
    Optional<Feast> getById(String id);
    List<Feast> getByLanguage(String lang);
    List<Feast> getByLanguageAndDate(String lang, String date);
    List<Feast> getByLanguageAndGroup(String lang, String group);
    List<Feast> getByLanguageAndType(String lang, String type);
    List<Feast> getByLanguageAndName(String lang, String namePart);

    Feast save(Feast feast);
    List<Feast> saveAll(List<Feast> feasts);
    void deleteById(String id);
}