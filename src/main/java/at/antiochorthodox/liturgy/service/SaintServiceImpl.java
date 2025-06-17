package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Saint;
import at.antiochorthodox.liturgy.repository.SaintRepository;
//import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SaintServiceImpl implements SaintService {

    private final SaintRepository repository;
//    public SaintServiceImpl(SaintRepository repository) {
//        this.repository = repository;
//    }

    @Override
    public Optional<Saint> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Saint> getByLanguage(String lang) {
        return repository.findByLang(lang);
    }

    @Override
    public List<Saint> getByLanguageAndTitle(String lang, String title) {
        return repository.findByLangAndTitle(lang, title);
    }

    @Override
    public List<Saint> getByLanguageAndName(String lang, String namePart) {
        return repository.findByLangAndNameContainingIgnoreCase(lang, namePart);
    }

    @Override
    public List<Saint> getByLanguageAndFeastDate(String lang, LocalDate date) {
        String formatted = date.format(DateTimeFormatter.ofPattern("MM-dd"));
        return repository.findByLangAndFeastsDate(lang, formatted);
    }

    @Override
    public List<Saint> getByLanguageAndFeastMonth(String lang, int month) {
        String pattern = String.format("^%02d", month);
        return repository.findByLangAndFeastDatePattern(lang, pattern);
    }

    @Override
    public List<Saint> getByLanguageAndFeastDay(String lang, int day) {
        String pattern = String.format("-%02d$", day);
        return repository.findByLangAndFeastDatePattern(lang, pattern);
    }
}

