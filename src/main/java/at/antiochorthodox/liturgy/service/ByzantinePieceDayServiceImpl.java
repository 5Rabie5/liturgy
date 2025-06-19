package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalByzantinePieceDay;
import at.antiochorthodox.liturgy.repository.ByzantinePieceDayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ByzantinePieceDayServiceImpl implements ByzantinePieceDayService {

    private final ByzantinePieceDayRepository repository;

    @Autowired
    public ByzantinePieceDayServiceImpl(ByzantinePieceDayRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<LiturgicalByzantinePieceDay> findByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    @Override
    public List<LiturgicalByzantinePieceDay> findBySeason(String season) {
        return repository.findBySeason(season);
    }

    @Override
    public LiturgicalByzantinePieceDay save(LiturgicalByzantinePieceDay day) {
        return repository.save(day);
    }

    @Override
    public List<LiturgicalByzantinePieceDay> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
