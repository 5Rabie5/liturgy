package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;
import at.antiochorthodox.liturgy.repository.LiturgicalReadingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LiturgicalReadingAssignmentQueryServiceImpl implements LiturgicalReadingAssignmentQueryService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final LiturgicalReadingAssignmentRepository assignmentRepository;

    @Override
    public List<LiturgicalReadingAssignment> findAssignmentsForDay(ReadingContext context) {
        String tradition = resolveTradition(context);
        for (String lookupDayKey : resolveLookupDayKeys(context)) {
            List<LiturgicalReadingAssignment> found =
                    assignmentRepository.findByTraditionAndDayKeyOrderByServiceKeyAscSequenceAsc(
                            tradition,
                            lookupDayKey
                    );
            if (!found.isEmpty()) {
                return found;
            }
        }
        return List.of();
    }

    @Override
    public List<LiturgicalReadingAssignment> findAssignmentsForDayAndSlot(ReadingContext context, String slot) {
        String tradition = resolveTradition(context);
        for (String lookupDayKey : resolveLookupDayKeys(context)) {
            List<LiturgicalReadingAssignment> found =
                    assignmentRepository.findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(
                            tradition,
                            lookupDayKey,
                            slot
                    );
            if (!found.isEmpty()) {
                return found;
            }
        }
        return List.of();
    }

    @Override
    public List<LiturgicalReadingAssignment> findAssignmentsForService(ReadingContext context, String serviceKey) {
        String tradition = resolveTradition(context);

        for (String lookupDayKey : resolveLookupDayKeys(context)) {
            List<LiturgicalReadingAssignment> exact =
                    assignmentRepository.findByTraditionAndDayKeyAndServiceKeyOrderBySequenceAsc(
                            tradition,
                            lookupDayKey,
                            serviceKey
                    );
            if (!exact.isEmpty()) {
                return exact;
            }
        }

        return List.of();
    }

    private String resolveTradition(ReadingContext context) {
        if (context == null || context.getTradition() == null || context.getTradition().isBlank()) {
            return DEFAULT_TRADITION;
        }
        return context.getTradition();
    }

    private List<String> resolveLookupDayKeys(ReadingContext context) {
        if (context == null) {
            throw new IllegalArgumentException("ReadingContext must not be null");
        }

        Set<String> keys = new LinkedHashSet<>();

        if (context.getLookupDayKeys() != null) {
            context.getLookupDayKeys().stream()
                    .filter(this::hasText)
                    .forEach(keys::add);
        }

        if (hasText(context.getReadingDayKey())) {
            keys.add(context.getReadingDayKey());
        }
        if (hasText(context.getDayKey())) {
            keys.add(context.getDayKey());
        }
        if (hasText(context.getCalendarDayKey())) {
            keys.add(context.getCalendarDayKey());
        }

        if (keys.isEmpty()) {
            throw new IllegalStateException("No readingDayKey/dayKey/calendarDayKey found in ReadingContext");
        }

        return new ArrayList<>(keys);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
