package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;
import at.antiochorthodox.liturgy.reading.v2.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.reading.v2.repository.LiturgicalReadingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LiturgicalReadingAssignmentServiceImpl implements LiturgicalReadingAssignmentService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final LiturgicalReadingAssignmentRepository assignmentRepository;

    @Override
    public List<LiturgicalReadingAssignment> findAssignmentsForDay(ReadingContext context) {
        String tradition = resolveTradition(context);
        String dayKey = resolveReadingDayKey(context);

        return assignmentRepository.findByTraditionAndDayKeyOrderByServiceKeyAscSequenceAsc(
                tradition,
                dayKey
        );
    }

    @Override
    public List<LiturgicalReadingAssignment> findAssignmentsForDayAndSlot(ReadingContext context, String slot) {
        String tradition = resolveTradition(context);
        String dayKey = resolveReadingDayKey(context);

        return assignmentRepository.findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(
                tradition,
                dayKey,
                slot
        );
    }

    @Override
    public List<LiturgicalReadingAssignment> findAssignmentsForService(ReadingContext context, String serviceKey) {
        String tradition = resolveTradition(context);
        String readingDayKey = resolveReadingDayKey(context);
        String calendarDayKey = context != null ? context.getCalendarDayKey() : null;

        List<LiturgicalReadingAssignment> exact =
                assignmentRepository.findByTraditionAndDayKeyAndServiceKeyOrderBySequenceAsc(
                        tradition,
                        readingDayKey,
                        serviceKey
                );

        if (!exact.isEmpty()) {
            return exact;
        }

        if (hasText(calendarDayKey) && !calendarDayKey.equals(readingDayKey)) {
            exact = assignmentRepository.findByTraditionAndDayKeyAndServiceKeyOrderBySequenceAsc(
                    tradition,
                    calendarDayKey,
                    serviceKey
            );

            if (!exact.isEmpty()) {
                return exact;
            }
        }

        return findByAnyDayKey(tradition, serviceKey, readingDayKey, calendarDayKey);
    }

    private List<LiturgicalReadingAssignment> findByAnyDayKey(String tradition, String... dayKeys) {
        java.util.LinkedHashSet<String> keys = new java.util.LinkedHashSet<>();

        for (String key : dayKeys) {
            if (hasText(key)) {
                keys.add(key);
            }
        }

        for (String key : keys) {
            List<LiturgicalReadingAssignment> found =
                    assignmentRepository.findByTraditionAndDayKeyOrderByServiceKeyAscSequenceAsc(
                            tradition,
                            key
                    );
            if (!found.isEmpty()) {
                return found;
            }
        }

        return List.of();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveTradition(ReadingContext context) {
        if (context == null || context.getTradition() == null || context.getTradition().isBlank()) {
            return DEFAULT_TRADITION;
        }
        return context.getTradition();
    }

    private String resolveReadingDayKey(ReadingContext context) {
        if (context == null) {
            throw new IllegalArgumentException("ReadingContext must not be null");
        }

        if (context.getReadingDayKey() != null && !context.getReadingDayKey().isBlank()) {
            return context.getReadingDayKey();
        }

        if (context.getDayKey() != null && !context.getDayKey().isBlank()) {
            return context.getDayKey();
        }

        throw new IllegalStateException("No readingDayKey/dayKey found in ReadingContext");
    }
}
