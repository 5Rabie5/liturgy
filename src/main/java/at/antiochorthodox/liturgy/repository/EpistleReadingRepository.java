package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.EpistleReading;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EpistleReadingRepository extends MongoRepository<EpistleReading, String> {
    List<EpistleReading> findByLiturgicalNameAndLang(String liturgicalName, String lang);
    List<EpistleReading> findByReadingKeyAndLang(String readingKey, String lang);
    Optional<EpistleReading> findByReadingKey(String readingKey);
}
