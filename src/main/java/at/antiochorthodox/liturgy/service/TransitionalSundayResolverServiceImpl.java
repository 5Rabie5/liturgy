package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.TransitionalSundayResolution;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.TransitionalSundayOverride;
import at.antiochorthodox.liturgy.repository.TransitionalSundayOverrideRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
public class TransitionalSundayResolverServiceImpl implements TransitionalSundayResolverService {

    private static final String TRADITION = "ANTIOCHIAN";
    private static final String DEFAULT_SLOT = "liturgy";

    private final TransitionalSundayOverrideRepository overrideRepository;
    private final PaschaDateCalculator paschaDateCalculator;

    public TransitionalSundayResolverServiceImpl(
            TransitionalSundayOverrideRepository overrideRepository,
            PaschaDateCalculator paschaDateCalculator
    ) {
        this.overrideRepository = overrideRepository;
        this.paschaDateCalculator = paschaDateCalculator;
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
        return "أحد انتقالي قبل التريودي";
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}