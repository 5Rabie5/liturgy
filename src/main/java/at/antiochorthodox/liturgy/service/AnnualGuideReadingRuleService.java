package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Small, data-driven fallback for weekday Gospel readings published in the
 * Antiochian annual guide before Triodion.
 *
 * <p>This intentionally does not replace {@code liturgical_reading_assignments}.
 * It only completes a day that already has a weekday epistle assignment but is
 * missing its Gospel assignment. Sundays remain handled by
 * {@link TransitionalSundayResolverServiceImpl} and annual overrides.</p>
 */
@Service
public class AnnualGuideReadingRuleService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";
    private static final String LITURGY_SLOT = "liturgy";
    private static final String DEFAULT_SLOT = "default";

    private static final String READING_TYPE_GOSPEL = "gospel";
    private static final String SOURCE_TYPE_DAY = "day";
    private static final String USAGE_MAIN = "main";

    /*
     * Verified from the Antiochian annual guide pages for the pre-Triodion
     * weekday Gospel cycle. These are not display labels and contain no Arabic
     * language dependency.
     */
    private static final List<GuideGospelRule> GUIDE_GOSPEL_RULES = List.of(
            new GuideGospelRule(
                    2024,
                    LocalDate.of(2024, 1, 7),
                    LocalDate.of(2024, 2, 24),
                    10,
                    GospelCycle.MATTHEW,
                    "Guide 2024 p.15"
            ),
            new GuideGospelRule(
                    2025,
                    LocalDate.of(2025, 1, 5),
                    LocalDate.of(2025, 2, 8),
                    12,
                    GospelCycle.MATTHEW,
                    "Guide 2025 p.15"
            ),
            new GuideGospelRule(
                    2026,
                    LocalDate.of(2026, 1, 11),
                    LocalDate.of(2026, 2, 14),
                    11,
                    GospelCycle.MATTHEW,
                    "Guide 2026 p.15"
            )
    );

    public Optional<LiturgicalReadingAssignment> buildWeekdayGospelFallback(
            LocalDate date,
            String tradition,
            String dayKey,
            String slot
    ) {
        if (date == null || !hasText(dayKey)) {
            return Optional.empty();
        }

        if (!dayKey.startsWith("PENTECOST_")) {
            return Optional.empty();
        }

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return Optional.empty();
        }

        return GUIDE_GOSPEL_RULES.stream()
                .filter(rule -> rule.matches(date))
                .findFirst()
                .map(rule -> buildAssignment(date, tradition, dayKey, slot, rule));
    }

    private LiturgicalReadingAssignment buildAssignment(
            LocalDate date,
            String tradition,
            String dayKey,
            String slot,
            GuideGospelRule rule
    ) {
        String effectiveTradition = hasText(tradition) ? tradition : DEFAULT_TRADITION;
        String effectiveSlot = normalizeSlot(slot);
        if (!hasText(effectiveSlot)) {
            effectiveSlot = LITURGY_SLOT;
        }

        return LiturgicalReadingAssignment.builder()
                .tradition(effectiveTradition)
                .dayKey(dayKey)
                .calendarDayKey(dayKey)
                .slot(effectiveSlot)
                .serviceKey(buildServiceKey(dayKey, effectiveSlot))
                .sourceType(SOURCE_TYPE_DAY)
                .readingType(READING_TYPE_GOSPEL)
                .readingKey(rule.readingKeyFor(date))
                .sequence(2)
                .groupKey(LITURGY_SLOT.equals(effectiveSlot) ? "LITURGY_MAIN" : "SERVICE_MAIN")
                .usage(USAGE_MAIN)
                .notes("Annual Antiochian guide weekday Gospel fallback: " + rule.sourceReference())
                .primaryAssignment(true)
                .build();
    }

    private String buildServiceKey(String dayKey, String slot) {
        String normalizedSlot = normalizeSlot(slot);
        String suffix = DEFAULT_SLOT.equals(normalizedSlot) ? "DEFAULT" : "LITURGY";
        return dayKey + "_" + suffix;
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase(Locale.ROOT) : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private enum GospelCycle {
        MATTHEW
    }

    private static class GuideGospelRule {
        private final int year;
        private final LocalDate startSunday;
        private final LocalDate endDate;
        private final int startWeek;
        private final GospelCycle cycle;
        private final String sourceReference;

        private GuideGospelRule(
                int year,
                LocalDate startSunday,
                LocalDate endDate,
                int startWeek,
                GospelCycle cycle,
                String sourceReference
        ) {
            this.year = year;
            this.startSunday = startSunday;
            this.endDate = endDate;
            this.startWeek = startWeek;
            this.cycle = cycle;
            this.sourceReference = sourceReference;
        }

        private boolean matches(LocalDate date) {
            return date != null
                    && date.getYear() == year
                    && !date.isBefore(startSunday)
                    && !date.isAfter(endDate);
        }

        private String sourceReference() {
            return sourceReference;
        }

        private String readingKeyFor(LocalDate date) {
            int week = startWeek + (int) ChronoUnit.WEEKS.between(
                    startSunday,
                    date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            );

            if (cycle == GospelCycle.MATTHEW) {
                return buildMatthewReadingKey(week, date.getDayOfWeek());
            }

            throw new IllegalStateException("Unsupported annual guide Gospel cycle: " + cycle);
        }

        private String buildMatthewReadingKey(int week, DayOfWeek dayOfWeek) {
            String weekKey = String.format(Locale.ROOT, "W%02d", week);

            if (dayOfWeek == DayOfWeek.SATURDAY) {
                return "MATTHEW_" + weekKey + "_SATURDAY__SATURDAY";
            }

            return "MARK_MATTHEW_" + weekKey + "_" + dayOfWeek.name();
        }
    }
}
