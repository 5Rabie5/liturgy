package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Saint;
import at.antiochorthodox.liturgy.repository.SaintRepository;
//import lombok.RequiredArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SaintServiceImpl implements SaintService {

    private final SaintRepository repository;


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
    public Saint save(Saint saint) {
        return repository.save(saint);
    }

    @Override
    public List<Saint> saveAll(List<Saint> saints) {
        return repository.saveAll(saints);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<String> findNamesByLangAndDate(String lang, LocalDate date) {
        String mmdd = String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
        List<Saint> saints = repository.findByLang(lang);
        return saints.stream()
                .filter(saint ->
                        saint.getFeasts() != null &&
                                saint.getFeasts().stream().anyMatch(feast -> mmdd.equals(feast.getDatesaintfeast()))
                )
                .map(Saint::getName)
                .toList();
    }


    @Override
    public List<Saint> getByLanguageAndFeastMonth(String lang, int month) {
        String regex = String.format("^%02d", month);
        return repository.findByLangAndFeastDatePattern(lang, regex);
    }


    @Override
    public List<Saint> getByLanguageAndFeastDate(String lang, int month, int day) {
        String mmdd = String.format("%02d-%02d", month, day);
        return repository.findByLangAndFeastDateElemMatch(lang, mmdd);
    }

    @Override
    public List<Saint> getByLanguageAndFeastDate(String lang, LocalDate date) {
        String mmdd = String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
        return repository.findByLangAndFeastDateElemMatch(lang, mmdd);
    }

}

