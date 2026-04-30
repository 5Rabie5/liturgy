package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.TransitionalSundayResolution;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.model.TransitionalSundayOverride;
import at.antiochorthodox.liturgy.repository.LiturgicalReadingAssignmentRepository;
import at.antiochorthodox.liturgy.repository.TransitionalSundayOverrideRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransitionalSundayResolverServiceImpl implements TransitionalSundayResolverService {

    private static final String TRADITION = "ANTIOCHIAN";
    private static final String DEFAULT_SLOT = "liturgy";

    private static final String READING_TYPE_EPISTLE = "epistle";
    private static final String READING_TYPE_GOSPEL = "gospel";

    private static final String THEOPHANY_AFTER_SUNDAY_DAY_KEY = "THEOPHANY_AFTER_SUNDAY";

    private final TransitionalSundayOverrideRepository overrideRepository;
    private final PaschaDateCalculator paschaDateCalculator;
    private final LiturgicalReadingAssignmentRepository assignmentRepository;

    public TransitionalSundayResolverServiceImpl(
            TransitionalSundayOverrideRepository overrideRepository,
            PaschaDateCalculator paschaDateCalculator,
            LiturgicalReadingAssignmentRepository assignmentRepository
    ) {
        this.overrideRepository = overrideRepository;
        this.paschaDateCalculator = paschaDateCalculator;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public boolean isInWindow(LocalDate date) {
        if (date == null || date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return false;
        }

        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        LocalDate triodionStart = pascha.minusWeeks(10);
        LocalDate lowerBound = LocalDate.of(date.getYear(), 1, 7);

        return !date.isBefore(lowerBound) && date.isBefore(triodionStart);
    }

    @Override
    public TransitionalSundayResolution resolve(LocalDate date, String lang, LiturgicalCalendarDay baseDay) {
        if (!isInWindow(date)) {
            return TransitionalSundayResolution.builder()
                    .inWindow(false)
                    .overrideApplied(false)
                    .effectiveDayKey(baseDay != null ? baseDay.getLiturgicalDayKey() : null)
                    .effectiveReadingDayKey(baseDay != null ? baseDay.getLiturgicalDayKey() : null)
                    .effectiveLiturgicalName(baseDay != null ? baseDay.getLiturgicalName() : null)
                    .effectiveEpistleKey(baseDay != null ? baseDay.getEpistleKey() : null)
                    .effectiveGospelKey(baseDay != null ? baseDay.getGospelKey() : null)
                    .decisionBasis(null)
                    .sourceReference(null)
                    .note(null)
                    .build();
        }

        String baseDayKey = baseDay != null ? baseDay.getLiturgicalDayKey() : null;
        String baseLiturgicalName = baseDay != null ? baseDay.getLiturgicalName() : null;
        String baseEpistleKey = baseDay != null ? baseDay.getEpistleKey() : null;
        String baseGospelKey = baseDay != null ? baseDay.getGospelKey() : null;

        if (isSundayAfterTheophany(date)) {
            String epistleKey = firstNonBlank(
                    findAssignmentReadingKey(THEOPHANY_AFTER_SUNDAY_DAY_KEY, READING_TYPE_EPISTLE),
                    baseEpistleKey
            );

            String gospelKey = firstNonBlank(
                    findAssignmentReadingKey(THEOPHANY_AFTER_SUNDAY_DAY_KEY, READING_TYPE_GOSPEL),
                    baseGospelKey
            );

            return TransitionalSundayResolution.builder()
                    .inWindow(true)
                    .overrideApplied(false)
                    .effectiveDayKey(THEOPHANY_AFTER_SUNDAY_DAY_KEY)
                    .effectiveReadingDayKey(THEOPHANY_AFTER_SUNDAY_DAY_KEY)
                    .effectiveLiturgicalName(firstNonBlank(baseLiturgicalName, THEOPHANY_AFTER_SUNDAY_DAY_KEY))
                    .effectiveEpistleKey(epistleKey)
                    .effectiveGospelKey(gospelKey)
                    .decisionBasis("SPECIAL_MOVABLE_FEAST")
                    .sourceReference(THEOPHANY_AFTER_SUNDAY_DAY_KEY)
                    .note("Sunday after Theophany resolved before transitional Sunday fallback.")
                    .build();
        }

        Optional<TransitionalSundayOverride> overrideOpt = findOverride(date);

        if (overrideOpt.isPresent()) {
            TransitionalSundayOverride override = overrideOpt.get();

            return TransitionalSundayResolution.builder()
                    .inWindow(true)
                    .overrideApplied(true)
                    .effectiveDayKey(buildVirtualDayKey(date))
                    .effectiveReadingDayKey(firstNonBlank(override.getSourceDayKey(), baseDayKey))
                    .effectiveLiturgicalName(firstNonBlank(baseLiturgicalName, buildVirtualLiturgicalName(date)))
                    .effectiveEpistleKey(firstNonBlank(override.getSelectedEpistleKey(), baseEpistleKey))
                    .effectiveGospelKey(firstNonBlank(override.getSelectedGospelKey(), baseGospelKey))
                    .decisionBasis(firstNonBlank(override.getDecisionBasis(), "YEAR_OVERRIDE"))
                    .sourceReference(override.getSourceReference())
                    .note(firstNonBlank(override.getNotes(), "Transitional Sunday annual override applied."))
                    .build();
        }

        if (isAlreadyFullyResolvedSunday(baseDay)) {
            return TransitionalSundayResolution.builder()
                    .inWindow(false)
                    .overrideApplied(false)
                    .effectiveDayKey(baseDayKey)
                    .effectiveReadingDayKey(baseDayKey)
                    .effectiveLiturgicalName(baseLiturgicalName)
                    .effectiveEpistleKey(baseEpistleKey)
                    .effectiveGospelKey(baseGospelKey)
                    .decisionBasis(null)
                    .sourceReference(null)
                    .note(null)
                    .build();
        }

        return TransitionalSundayResolution.builder()
                .inWindow(true)
                .overrideApplied(false)
                .effectiveDayKey(firstNonBlank(baseDayKey, buildVirtualDayKey(date)))
                .effectiveReadingDayKey(baseDayKey)
                .effectiveLiturgicalName(firstNonBlank(baseLiturgicalName, buildVirtualLiturgicalName(date)))
                .effectiveEpistleKey(baseEpistleKey)
                .effectiveGospelKey(baseGospelKey)
                .decisionBasis("BASE_RULE")
                .sourceReference(null)
                .note("Transitional Sunday resolved by base carry-over rule.")
                .build();
    }

    private Optional<TransitionalSundayOverride> findOverride(LocalDate date) {
        return overrideRepository.findByTraditionAndEnabledTrueOrderByDateAsc(TRADITION).stream()
                .filter(Objects::nonNull)
                .filter(override -> override.getDate() != null && override.getDate().equals(date))
                .filter(override -> matchesSlot(override.getSlot()))
                .findFirst();
    }

    private String findAssignmentReadingKey(String dayKey, String readingType) {
        if (!hasText(dayKey) || !hasText(readingType)) {
            return null;
        }

        List<LiturgicalReadingAssignment> assignments =
                assignmentRepository.findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(
                        TRADITION,
                        dayKey,
                        DEFAULT_SLOT
                );

        return assignments.stream()
                .filter(Objects::nonNull)
                .filter(assignment -> readingType.equalsIgnoreCase(assignment.getReadingType()))
                .map(LiturgicalReadingAssignment::getReadingKey)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private boolean isSundayAfterTheophany(LocalDate date) {
        if (date == null || date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return false;
        }

        LocalDate theophany = LocalDate.of(date.getYear(), 1, 6);
        LocalDate sundayAfterTheophany = theophany.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

        return date.equals(sundayAfterTheophany);
    }

    private boolean matchesSlot(String slot) {
        return !hasText(slot) || DEFAULT_SLOT.equalsIgnoreCase(slot.trim());
    }

    private boolean isAlreadyFullyResolvedSunday(LiturgicalCalendarDay baseDay) {
        return baseDay != null
                && hasText(baseDay.getLiturgicalDayKey())
                && hasText(baseDay.getEpistleKey())
                && hasText(baseDay.getGospelKey());
    }

    private String buildVirtualDayKey(LocalDate date) {
        return "TRANSITIONAL_SUNDAY_" + date;
    }

    private String buildVirtualLiturgicalName(LocalDate date) {
        return buildVirtualDayKey(date);
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}