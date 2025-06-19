package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalScriptureReadingDay;
import at.antiochorthodox.liturgy.repository.LiturgicalScriptureReadingDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LiturgicalScriptureReadingDayServiceImpl implements LiturgicalScriptureReadingDayService {

    private final LiturgicalScriptureReadingDayRepository repository;

    @Autowired
    public LiturgicalScriptureReadingDayServiceImpl(LiturgicalScriptureReadingDayRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<LiturgicalScriptureReadingDay> findByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    @Override
    public List<LiturgicalScriptureReadingDay> findBySeason(String season) {
        return repository.findBySeason(season);
    }

    @Override
    public LiturgicalScriptureReadingDay save(LiturgicalScriptureReadingDay day) {
        return repository.save(day);
    }

    @Override
    public List<LiturgicalScriptureReadingDay> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
