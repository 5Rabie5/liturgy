package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Fasting;
import at.antiochorthodox.liturgy.repository.FastingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FastingServiceImpl implements FastingService {

    private final FastingRepository repository;

    @Override
    public Optional<Fasting> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Fasting> getByLanguage(String lang) {
        return repository.findByLang(lang);
    }

    @Override
    public List<Fasting> getByLanguageAndType(String lang, String type) {
        return repository.findByLangAndType(lang, type);
    }

    @Override
    public List<Fasting> getWeeklyFasting(String lang) {
        return repository.findByLangAndRepeatWeeklyTrue(lang);
    }

    @Override
    public Fasting save(Fasting fasting) {
        return repository.save(fasting);
    }

    @Override
    public List<Fasting> saveAll(List<Fasting> fastingList) {
        return repository.saveAll(fastingList);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}