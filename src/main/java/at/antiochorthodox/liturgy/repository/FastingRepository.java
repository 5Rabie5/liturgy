package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.Fasting;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FastingRepository extends MongoRepository<Fasting, String> {

    List<Fasting> findByLang(String lang);

    List<Fasting> findByLangAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String lang, String end, String start);

    List<Fasting> findByLangAndType(String lang, String type);

    List<Fasting> findByLangAndRepeatWeeklyTrue(String lang);
}
