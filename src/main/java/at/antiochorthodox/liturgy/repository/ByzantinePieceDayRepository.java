package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalByzantinePieceDay;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ByzantinePieceDayRepository extends MongoRepository<LiturgicalByzantinePieceDay, String> {

    Optional<LiturgicalByzantinePieceDay> findByDate(LocalDate date);

    List<LiturgicalByzantinePieceDay> findBySeason(String season);

    List<LiturgicalByzantinePieceDay> findByDayName(String dayName);

    // يمكنك إضافة دوال مخصصة أخرى للبحث أو الفلترة حسب الحاجة
}
