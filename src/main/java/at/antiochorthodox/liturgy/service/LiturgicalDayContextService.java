package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.repository.LiturgicalReadingAssignmentRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Compatibility context resolver used by the legacy/v1 text-based reading flow.
 *
 * <p>Although the legacy endpoints still return a simplified single-context view,
 * they now hydrate that view from {@code liturgical_reading_assignments} rather
 * than from the old day-map collection. This keeps the legacy API surface alive
 * without making {@code liturgical_day_reading_maps} part of the runtime core.</p>
 */
@Service
public class LiturgicalDayContextService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";
    private static final String DEFAULT_SLOT = "default";
    private static final String LITURGY_SLOT = "liturgy";

    private final LiturgicalLabelService liturgicalLabelService;
    private final LiturgicalReadingAssignmentRepository assignmentRepository;
    private final PaschaDateCalculator paschaDateCalculator;
    private final ReadingDayKeyResolver readingDayKeyResolver;

    public LiturgicalDayContextService(
            LiturgicalLabelService liturgicalLabelService,
            LiturgicalReadingAssignmentRepository assignmentRepository,
            PaschaDateCalculator paschaDateCalculator,
            ReadingDayKeyResolver readingDayKeyResolver
    ) {
        this.liturgicalLabelService = liturgicalLabelService;
        this.assignmentRepository = assignmentRepository;
        this.paschaDateCalculator = paschaDateCalculator;
        this.readingDayKeyResolver = readingDayKeyResolver;
    }

    public LiturgicalDayContext resolveForDate(LocalDate date, String lang) {
        return resolveForDate(date, lang, null);
    }

    public LiturgicalDayContext resolveForDate(LocalDate date, String lang, String slot) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        String calendarDayKey = liturgicalLabelService.getDayKeyForDate(date, pascha, lang);

        if (!hasText(calendarDayKey)) {
            return null;
        }

        ReadingDayKeyResolution resolution = readingDayKeyResolver.resolve(calendarDayKey, date, slot);

        String resolvedDayKey = resolution != null ? resolution.getResolvedDayKey() : null;
        String canonicalDayKey = resolution != null && hasText(resolution.getCanonicalDayKey())
                ? resolution.getCanonicalDayKey()
                : resolvedDayKey;

        List<String> lookupDayKeys = resolution != null && resolution.getLookupDayKeys() != null
                ? resolution.getLookupDayKeys()
                : List.of();

        if (!hasText(canonicalDayKey) && !lookupDayKeys.isEmpty()) {
            canonicalDayKey = lookupDayKeys.get(0);
        }

        if (!hasText(canonicalDayKey)) {
            return null;
        }

        LiturgicalDayContext context = resolveByLookupDayKeys(canonicalDayKey, lookupDayKeys, lang, slot);
        if (context == null) {
            return null;
        }

        context.setCalendarDayKey(calendarDayKey);
        context.setReadingDayKey(canonicalDayKey);
        context.setDayKey(canonicalDayKey);
        context.setTradition(DEFAULT_TRADITION);

        String dayLabel = liturgicalLabelService.getLabelForDayKey(canonicalDayKey, lang);
        if (hasText(dayLabel)) {
            context.setDayLabel(dayLabel);
        }

        return context;
    }

    public LiturgicalDayContext resolveByDayKey(String dayKey, String lang) {
        return resolveByDayKey(dayKey, lang, null);
    }

    public LiturgicalDayContext resolveByDayKey(String dayKey, String lang, String slot) {
        if (!hasText(dayKey)) {
            return null;
        }

        List<String> lookupDayKeys = readingDayKeyResolver.resolveLookupDayKeys(dayKey, null, slot);
        return resolveByLookupDayKeys(dayKey, lookupDayKeys, lang, slot);
    }

    private LiturgicalDayContext resolveByLookupDayKeys(
            String canonicalDayKey,
            List<String> lookupDayKeys,
            String lang,
            String slot
    ) {
        String normalizedSlot = normalizeSlot(slot);

        List<String> effectiveLookupKeys = buildEffectiveLookupKeys(canonicalDayKey, lookupDayKeys);
        if (effectiveLookupKeys.isEmpty()) {
            return null;
        }

        List<LiturgicalReadingAssignment> assignments =
                loadMergedAssignments(effectiveLookupKeys, normalizedSlot);

        if (assignments.isEmpty() && hasText(normalizedSlot) && !LITURGY_SLOT.equals(normalizedSlot)) {
            assignments = loadMergedAssignments(effectiveLookupKeys, LITURGY_SLOT);
        }
        if (assignments.isEmpty() && hasText(normalizedSlot) && !DEFAULT_SLOT.equals(normalizedSlot)) {
            assignments = loadMergedAssignments(effectiveLookupKeys, DEFAULT_SLOT);
        }
        if (assignments.isEmpty()) {
            assignments = loadMergedAssignments(effectiveLookupKeys, null);
        }
        if (assignments.isEmpty()) {
            return null;
        }

        LiturgicalReadingAssignment first = assignments.get(0);

        String preferredSlot = assignments.stream()
                .map(LiturgicalReadingAssignment::getSlot)
                .filter(this::hasText)
                .findFirst()
                .orElse(normalizedSlot);

        String effectiveCalendarDayKey = firstNonBlank(first.getCalendarDayKey(), canonicalDayKey);

        return LiturgicalDayContext.builder()
                .tradition(DEFAULT_TRADITION)
                .dayKey(canonicalDayKey)
                .calendarDayKey(effectiveCalendarDayKey)
                .readingDayKey(canonicalDayKey)
                .dayLabel(resolveLabel(canonicalDayKey, lang))
                .slot(firstNonBlank(preferredSlot, normalizedSlot))
                .sourceType(first.getSourceType())
                .epistleKey(findReadingKey(assignments, "epistle"))
                .gospelKey(findReadingKey(assignments, "gospel"))
                .build();
    }

    private List<String> buildEffectiveLookupKeys(String canonicalDayKey, List<String> lookupDayKeys) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();

        if (hasText(canonicalDayKey)) {
            keys.add(canonicalDayKey);
        }

        if (lookupDayKeys != null) {
            lookupDayKeys.stream()
                    .filter(this::hasText)
                    .forEach(keys::add);
        }

        return new ArrayList<>(keys);
    }

    private List<LiturgicalReadingAssignment> loadMergedAssignments(List<String> dayKeys, String slot) {
        if (dayKeys == null || dayKeys.isEmpty()) {
            return List.of();
        }

        List<LiturgicalReadingAssignment> merged = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (String dayKey : dayKeys) {
            List<LiturgicalReadingAssignment> assignments = loadAssignments(dayKey, slot);
            for (LiturgicalReadingAssignment assignment : assignments) {
                String identity = buildAssignmentIdentity(assignment);
                if (seen.add(identity)) {
                    merged.add(assignment);
                }
            }
        }

        return merged;
    }

    private String buildAssignmentIdentity(LiturgicalReadingAssignment assignment) {
        if (assignment == null) {
            return "";
        }

        return String.join("|",
                nullSafe(assignment.getTradition()),
                nullSafe(assignment.getDayKey()),
                nullSafe(assignment.getSlot()),
                nullSafe(assignment.getServiceKey()),
                nullSafe(assignment.getSourceType()),
                nullSafe(assignment.getReadingType()),
                nullSafe(assignment.getReadingKey()),
                String.valueOf(assignment.getSequence())
        );
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    private List<LiturgicalReadingAssignment> loadAssignments(String dayKey, String slot) {
        if (!hasText(dayKey)) {
            return List.of();
        }

        List<LiturgicalReadingAssignment> assignments;
        if (hasText(slot)) {
            assignments = assignmentRepository
                    .findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(DEFAULT_TRADITION, dayKey, slot);
        } else {
            assignments = assignmentRepository
                    .findByTraditionAndDayKeyOrderByServiceKeyAscSequenceAsc(DEFAULT_TRADITION, dayKey);
        }

        return assignments.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing(LiturgicalReadingAssignment::getServiceKey, Comparator.nullsLast(String::compareTo))
                        .thenComparing(a -> a.getSequence() == null ? Integer.MAX_VALUE : a.getSequence()))
                .collect(Collectors.toList());
    }

    private String resolveLabel(String dayKey, String lang) {
        String label = liturgicalLabelService.getLabelForDayKey(dayKey, lang);
        return hasText(label) ? label : dayKey;
    }

    private String findReadingKey(List<LiturgicalReadingAssignment> assignments, String readingType) {
        return assignments.stream()
                .filter(Objects::nonNull)
                .filter(a -> readingType.equalsIgnoreCase(a.getReadingType()))
                .map(LiturgicalReadingAssignment::getReadingKey)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase() : null;
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}