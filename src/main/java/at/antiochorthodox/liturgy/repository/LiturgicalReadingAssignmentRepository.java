package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LiturgicalReadingAssignmentRepository
        extends MongoRepository<LiturgicalReadingAssignment, String>,
        LiturgicalReadingAssignmentRepositoryCustom {

    List<LiturgicalReadingAssignment> findByTraditionAndDayKeyOrderByServiceKeyAscSequenceAsc(
            String tradition,
            String dayKey
    );

    List<LiturgicalReadingAssignment> findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(
            String tradition,
            String dayKey,
            String slot
    );

    List<LiturgicalReadingAssignment> findByTraditionAndDayKeyAndServiceKeyOrderBySequenceAsc(
            String tradition,
            String dayKey,
            String serviceKey
    );

    List<LiturgicalReadingAssignment> findByTraditionAndCalendarDayKeyOrderByServiceKeyAscSequenceAsc(
            String tradition,
            String calendarDayKey
    );

    List<LiturgicalReadingAssignment> findByTraditionAndDayKeyAndSourceTypeOrderByServiceKeyAscSequenceAsc(
            String tradition,
            String dayKey,
            String sourceType
    );

    Optional<LiturgicalReadingAssignment> findByTraditionAndDayKeyAndServiceKeyAndReadingTypeAndSequence(
            String tradition,
            String dayKey,
            String serviceKey,
            String readingType,
            Integer sequence
    );
}