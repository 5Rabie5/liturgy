package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalScriptureReadingDay;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiturgicalScriptureReadingDayRepository extends MongoRepository<LiturgicalScriptureReadingDay, String> {

    Optional<LiturgicalScriptureReadingDay> findByDate(LocalDate date);

    List<LiturgicalScriptureReadingDay> findBySeason(String season);

    List<LiturgicalScriptureReadingDay> findByDayName(String dayName);

    // يمكنك إضافة دوال بحث مخصصة لاحقاً حسب الحاجة!
}
