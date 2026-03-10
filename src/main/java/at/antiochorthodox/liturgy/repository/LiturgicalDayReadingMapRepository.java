package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalDayReadingMap;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LiturgicalDayReadingMapRepository extends MongoRepository<LiturgicalDayReadingMap, String> {
    Optional<LiturgicalDayReadingMap> findByTraditionAndDayKey(String tradition, String dayKey);
}
