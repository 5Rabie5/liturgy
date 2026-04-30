package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;
import at.antiochorthodox.liturgy.repository.LiturgicalReadingAssignmentRepository;
import at.antiochorthodox.liturgy.service.AnnualGuideReadingRuleService;
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
    private final AnnualGuideReadingRuleService annualGuideReadingRuleService;

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
                return mergeAnnualGuideFallbackAssignments(context, found, null);
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
                return mergeAnnualGuideFallbackAssignments(context, found, slot);
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
                return mergeAnnualGuideFallbackAssignments(context, exact, null);
            }
        }

        return List.of();
    }


    private List<LiturgicalReadingAssignment> mergeAnnualGuideFallbackAssignments(
            ReadingContext context,
            List<LiturgicalReadingAssignment> assignments,
            String requestedSlot
    ) {
        if (assignments == null || assignments.isEmpty()) {
            return assignments;
        }

        boolean hasEpistle = hasReadingType(assignments, "epistle");
        boolean hasGospel = hasReadingType(assignments, "gospel");
        if (!hasEpistle || hasGospel) {
            return assignments;
        }

        String dayKey = firstNonBlank(
                firstNonBlank(context.getReadingDayKey(), context.getDayKey()),
                context.getCalendarDayKey()
        );

        String effectiveSlot = assignments.stream()
                .map(LiturgicalReadingAssignment::getSlot)
                .filter(this::hasText)
                .findFirst()
                .orElse(requestedSlot);

        return annualGuideReadingRuleService
                .buildWeekdayGospelFallback(
                        context.getDate(),
                        resolveTradition(context),
                        dayKey,
                        effectiveSlot
                )
                .map(fallback -> appendAssignment(assignments, fallback))
                .orElse(assignments);
    }

    private boolean hasReadingType(List<LiturgicalReadingAssignment> assignments, String readingType) {
        if (assignments == null || assignments.isEmpty() || !hasText(readingType)) {
            return false;
        }

        return assignments.stream()
                .anyMatch(a -> a != null
                        && readingType.equalsIgnoreCase(a.getReadingType())
                        && hasText(a.getReadingKey()));
    }

    private List<LiturgicalReadingAssignment> appendAssignment(
            List<LiturgicalReadingAssignment> assignments,
            LiturgicalReadingAssignment fallback
    ) {
        List<LiturgicalReadingAssignment> merged = new ArrayList<>(assignments);
        merged.add(fallback);
        return merged;
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : second;
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
