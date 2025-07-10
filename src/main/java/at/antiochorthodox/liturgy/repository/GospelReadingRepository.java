package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.GospelReading;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GospelReadingRepository extends MongoRepository<GospelReading, String> {
    List<GospelReading> findByLiturgicalNameAndLang(String liturgicalName, String lang);
}
