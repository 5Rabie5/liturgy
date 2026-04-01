package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import at.antiochorthodox.liturgy.reading.v2.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.reading.v2.repository.LiturgicalReadingAssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AssignmentReadingDayKeyResolverImpl implements AssignmentReadingDayKeyResolver {

    private static final String TRADITION = "ANTIOCHIAN";

    private static final Map<String, String> EXPLICIT_ALIASES = Map.of(
            "TRIODION_W08_SUNDAY", "GREAT_LENT_W05_SUNDAY"
    );

    private final LiturgicalReadingAssignmentRepository assignmentRepository;

    public AssignmentReadingDayKeyResolverImpl(LiturgicalReadingAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public ReadingDayKeyResolution resolve(String calendarDayKey, LocalDate date, String slot) {
        List<String> tried = new ArrayList<>();

        if (!hasText(calendarDayKey)) {
            return ReadingDayKeyResolution.builder()
                    .calendarDayKey(calendarDayKey)
                    .resolvedDayKey(null)
                    .reason("blank")
                    .triedKeys(tried)
                    .build();
        }

        String normalizedSlot = normalizeSlot(slot);

        tried.add(calendarDayKey);
        if (existsInAssignmentsByDayKey(calendarDayKey, normalizedSlot)) {
            return ReadingDayKeyResolution.builder()
                    .calendarDayKey(calendarDayKey)
                    .resolvedDayKey(calendarDayKey)
                    .reason("direct_assignment_day_key")
                    .triedKeys(tried)
                    .build();
        }

        String mappedByCalendarKey = resolveByCalendarDayKeyFromAssignments(calendarDayKey, normalizedSlot);
        if (hasText(mappedByCalendarKey)) {
            if (!tried.contains(mappedByCalendarKey)) {
                tried.add(mappedByCalendarKey);
            }
            return ReadingDayKeyResolution.builder()
                    .calendarDayKey(calendarDayKey)
                    .resolvedDayKey(mappedByCalendarKey)
                    .reason("assignment_calendar_day_key")
                    .triedKeys(tried)
                    .build();
        }

        String alias = EXPLICIT_ALIASES.get(calendarDayKey);
        if (hasText(alias)) {
            tried.add(alias);
            if (existsInAssignmentsByDayKey(alias, normalizedSlot)) {
                return ReadingDayKeyResolution.builder()
                        .calendarDayKey(calendarDayKey)
                        .resolvedDayKey(alias)
                        .reason("explicit_alias_assignment")
                        .triedKeys(tried)
                        .build();
            }

            String aliasMappedByCalendarKey = resolveByCalendarDayKeyFromAssignments(alias, normalizedSlot);
            if (hasText(aliasMappedByCalendarKey)) {
                if (!tried.contains(aliasMappedByCalendarKey)) {
                    tried.add(aliasMappedByCalendarKey);
                }
                return ReadingDayKeyResolution.builder()
                        .calendarDayKey(calendarDayKey)
                        .resolvedDayKey(aliasMappedByCalendarKey)
                        .reason("explicit_alias_assignment_calendar_day_key")
                        .triedKeys(tried)
                        .build();
            }
        }

        String ruleBased = normalizeByRule(calendarDayKey);
        if (hasText(ruleBased) && !ruleBased.equals(calendarDayKey) && !tried.contains(ruleBased)) {
            tried.add(ruleBased);
            if (existsInAssignmentsByDayKey(ruleBased, normalizedSlot)) {
                return ReadingDayKeyResolution.builder()
                        .calendarDayKey(calendarDayKey)
                        .resolvedDayKey(ruleBased)
                        .reason("rule_based_assignment")
                        .triedKeys(tried)
                        .build();
            }

            String ruleMappedByCalendarKey = resolveByCalendarDayKeyFromAssignments(ruleBased, normalizedSlot);
            if (hasText(ruleMappedByCalendarKey)) {
                if (!tried.contains(ruleMappedByCalendarKey)) {
                    tried.add(ruleMappedByCalendarKey);
                }
                return ReadingDayKeyResolution.builder()
                        .calendarDayKey(calendarDayKey)
                        .resolvedDayKey(ruleMappedByCalendarKey)
                        .reason("rule_based_assignment_calendar_day_key")
                        .triedKeys(tried)
                        .build();
            }
        }

        return ReadingDayKeyResolution.builder()
                .calendarDayKey(calendarDayKey)
                .resolvedDayKey(calendarDayKey)
                .reason("unresolved_assignment_fallback")
                .triedKeys(tried)
                .build();
    }

    private String normalizeByRule(String key) {
        return key;
    }

    private boolean existsInAssignmentsByDayKey(String dayKey, String slot) {
        if (!hasText(dayKey)) {
            return false;
        }

        if (hasText(slot)) {
            return !assignmentRepository
                    .findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(TRADITION, dayKey, slot)
                    .isEmpty();
        }

        return !assignmentRepository
                .findByTraditionAndDayKeyOrderByServiceKeyAscSequenceAsc(TRADITION, dayKey)
                .isEmpty();
    }

    private String resolveByCalendarDayKeyFromAssignments(String calendarDayKey, String slot) {
        if (!hasText(calendarDayKey)) {
            return null;
        }

        List<LiturgicalReadingAssignment> assignments =
                assignmentRepository.findByTraditionAndCalendarDayKeyOrderByServiceKeyAscSequenceAsc(
                        TRADITION,
                        calendarDayKey
                );

        for (LiturgicalReadingAssignment assignment : assignments) {
            if (!hasText(assignment.getDayKey())) {
                continue;
            }
            if (!hasText(slot) || slot.equalsIgnoreCase(assignment.getSlot())) {
                return assignment.getDayKey();
            }
        }

        return null;
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
