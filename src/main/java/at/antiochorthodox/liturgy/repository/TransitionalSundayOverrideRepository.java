package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.TransitionalSundayOverride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransitionalSundayOverrideRepository extends MongoRepository<TransitionalSundayOverride, String> {

    List<TransitionalSundayOverride> findByTraditionAndEnabledTrueOrderByDateAsc(String tradition);
}