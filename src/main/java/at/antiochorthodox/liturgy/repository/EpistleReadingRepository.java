package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.EpistleReading;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EpistleReadingRepository extends MongoRepository<EpistleReading, String> {
    List<EpistleReading> findByLiturgicalNameAndLang(String liturgicalName, String lang);
}