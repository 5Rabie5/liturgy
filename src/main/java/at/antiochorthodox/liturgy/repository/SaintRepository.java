package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.Saint;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
public interface SaintRepository extends MongoRepository<Saint, String> {
    List<Saint> findByLang(String lang);
    List<Saint> findByLangAndTitle(String lang, String title);
    List<Saint> findByLangAndNameContainingIgnoreCase(String lang, String namePart);

    // بدل القديمة بهذه
    @Query("{ 'lang': ?0, 'feasts': { $elemMatch: { 'datesaintfeast': ?1 } } }")
    List<Saint> findByLangAndFeastDateElemMatch(String lang, String date);

    @Query("{ 'lang': ?0, 'feasts.datesaintfeast': { \"$regex\": ?1 } }")
    List<Saint> findByLangAndFeastDatePattern(String lang, String regex);
}

