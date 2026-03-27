package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalDayReadingMap;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LiturgicalDayReadingMapRepository extends MongoRepository<LiturgicalDayReadingMap, String> {
    Optional<LiturgicalDayReadingMap> findByTraditionAndDayKeyAndSlot(String tradition, String dayKey, String slot);
    Optional<LiturgicalDayReadingMap> findFirstByTraditionAndDayKeyOrderBySlotAsc(String tradition, String dayKey);
    List<LiturgicalDayReadingMap> findByTraditionAndDayKeyOrderBySlotAsc(String tradition, String dayKey);
}
