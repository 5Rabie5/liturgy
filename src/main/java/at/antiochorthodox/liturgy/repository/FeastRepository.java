package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.Feast;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FeastRepository extends MongoRepository<Feast, String> {

    List<Feast> findByLang(String lang);
    List<Feast> findByLangAndDate(String lang, String date);
    List<Feast> findByLangAndGroup(String lang, String group);
    List<Feast> findByLangAndType(String lang, String type);
    List<Feast> findByLangAndNameContainingIgnoreCase(String lang, String namePart);
}
