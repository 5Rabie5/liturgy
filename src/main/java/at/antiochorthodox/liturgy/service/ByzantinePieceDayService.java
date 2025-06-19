package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalByzantinePieceDay;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ByzantinePieceDayService {

    Optional<LiturgicalByzantinePieceDay> findByDate(LocalDate date);

    List<LiturgicalByzantinePieceDay> findBySeason(String season);

    LiturgicalByzantinePieceDay save(LiturgicalByzantinePieceDay day);

    List<LiturgicalByzantinePieceDay> findAll();

    void deleteById(String id);
}
