package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LiturgicalLabelRepository extends MongoRepository<LiturgicalLabel, String> {
    List<LiturgicalLabel> findByLang(String lang);
    Optional<LiturgicalLabel> findByLabelKeyAndLang(String labelKey, String lang);

    Optional<LiturgicalLabel> findByTypeAndSeasonAndWeekIndexAndDayOfWeekIsNullAndLang(
            String type, String season, int weekIndex, String lang);

    Optional<LiturgicalLabel> findByTypeAndSeasonAndWeekIndexAndDayOfWeekAndLang(
            String type, String season, int weekIndex, String dayOfWeek, String lang);
}