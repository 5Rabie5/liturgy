package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Feast;
import at.antiochorthodox.liturgy.repository.FeastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
class FeastServiceImpl implements FeastService {

    private final FeastRepository repository;

    @Override
    public Optional<Feast> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Feast> getByLanguage(String lang) {
        return repository.findByLang(lang);
    }

    @Override
    public List<Feast> getByLanguageAndDate(String lang, String feastdate) {
        return repository.findByLangAndFeastdate(lang, feastdate);
    }

    @Override
    public List<Feast> getByLanguageAndGroup(String lang, String group) {
        return repository.findByLangAndGroup(lang, group);
    }

    @Override
    public List<Feast> getByLanguageAndType(String lang, String type) {
        return repository.findByLangAndType(lang, type);
    }

    @Override
    public List<Feast> getByLanguageAndName(String lang, String namePart) {
        return repository.findByLangAndNameContainingIgnoreCase(lang, namePart);
    }

    @Override
    public Feast save(Feast feast) {
        return repository.save(feast);
    }

    @Override
    public List<Feast> saveAll(List<Feast> feasts) {
        return repository.saveAll(feasts);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public String findFixedFeastNameByLangAndDate(String lang, LocalDate date) {
        String feastdate = date.format(DateTimeFormatter.ofPattern("MM-dd"));
        List<Feast> feasts = repository.findByLangAndFeastdate(lang, feastdate);
        return feasts.stream()
                .filter(f -> "fixed".equalsIgnoreCase(f.getType()))
                .map(Feast::getName)
                .findFirst()
                .orElse(null);
    }

    @Override
    public String findMovableFeastNameByLangAndDate(String lang, LocalDate date) {
        String feastdate = date.format(DateTimeFormatter.ofPattern("MM-dd"));
        List<Feast> feasts = repository.findByLangAndFeastdate(lang, feastdate);
        return feasts.stream()
                .filter(f -> "paschal".equalsIgnoreCase(f.getType()))
                .map(Feast::getName)
                .findFirst()
                .orElse(null);
    }
}
