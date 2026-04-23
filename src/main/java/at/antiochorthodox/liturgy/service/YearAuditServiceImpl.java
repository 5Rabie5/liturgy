package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.YearAuditDayItem;
import at.antiochorthodox.liturgy.dto.YearAuditReport;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.YearAuditIssueType;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class YearAuditServiceImpl {

    private static final String DEFAULT_LANG = "ar";
    private static final Set<String> SPECIAL_MOVABLE_DAY_KEYS = Set.of(
            LiturgicalLabelService.DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE,
            LiturgicalLabelService.DAY_KEY_SATURDAY_OF_SOULS_BEFORE_PENTECOST,
            LiturgicalLabelService.DAY_KEY_LAZARUS_SATURDAY,
            LiturgicalLabelService.DAY_KEY_PALM_SUNDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_MONDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_TUESDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_WEDNESDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_THURSDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_FRIDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_SATURDAY,
            LiturgicalLabelService.DAY_KEY_PASCHA_SUNDAY,
            LiturgicalLabelService.DAY_KEY_RENEWAL_SATURDAY,
            LiturgicalLabelService.DAY_KEY_THOMAS_SUNDAY,
            LiturgicalLabelService.DAY_KEY_MID_PENTECOST,
            LiturgicalLabelService.DAY_KEY_ASCENSION,
            LiturgicalLabelService.DAY_KEY_PENTECOST_SUNDAY,
            LiturgicalLabelService.DAY_KEY_MONDAY_OF_HOLY_SPIRIT,
            LiturgicalLabelService.DAY_KEY_ALL_SAINTS_SUNDAY,
            LiturgicalLabelService.DAY_KEY_FATHERS_1ST_ECUMENICAL_COUNCIL_SUNDAY,
            LiturgicalLabelService.DAY_KEY_FATHERS_4TH_ECUMENICAL_COUNCIL_SUNDAY,
            LiturgicalLabelService.DAY_KEY_FATHERS_7TH_ECUMENICAL_COUNCIL_SUNDAY,
            LiturgicalLabelService.DAY_KEY_FOREFATHERS_SUNDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_ANCESTORS_SUNDAY,
            LiturgicalLabelService.DAY_KEY_SUNDAY_BEFORE_NATIVITY,
            LiturgicalLabelService.DAY_KEY_SUNDAY_AFTER_NATIVITY,
            LiturgicalLabelService.DAY_KEY_SUNDAY_BEFORE_THEOPHANY,
            LiturgicalLabelService.DAY_KEY_SUNDAY_AFTER_THEOPHANY
    );

    private static final Set<String> INTENTIONAL_GOSPEL_ONLY_DAY_KEYS = Set.of(
            LiturgicalLabelService.DAY_KEY_HOLY_MONDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_TUESDAY,
            LiturgicalLabelService.DAY_KEY_HOLY_WEDNESDAY
    );

    private final LiturgicalCalendarDayBuilderService liturgicalCalendarDayBuilderService;
    private final LiturgicalLabelService liturgicalLabelService;
    private final PaschaDateCalculator paschaDateCalculator;

    public YearAuditReport auditYear(int year, String lang) {
        String effectiveLang = hasText(lang) ? lang : DEFAULT_LANG;
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);

        List<YearAuditDayItem> items = new ArrayList<>();
        Map<YearAuditIssueType, Long> counts = initializeCounts();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            YearAuditDayItem item = auditDay(date, effectiveLang);
            items.add(item);

            if (item.getIssueTypes() != null) {
                for (YearAuditIssueType type : item.getIssueTypes()) {
                    counts.merge(type, 1L, Long::sum);
                }
            }
        }

        long problemDays = items.stream()
                .filter(item -> item.getIssueTypes() != null)
                .filter(item -> !(item.getIssueTypes().size() == 1 && item.getIssueTypes().contains(YearAuditIssueType.OK)))
                .count();

        return YearAuditReport.builder()
                .year(year)
                .lang(effectiveLang)
                .totalDays(items.size())
                .problemDays(problemDays)
                .counts(counts)
                .items(items)
                .build();
    }

    public YearAuditDayItem auditDay(LocalDate date, String lang) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        String calendarDayKey = liturgicalLabelService.getDayKeyForDate(date, pascha, lang);
        LiturgicalCalendarDay result = fetchExistingDayResult(date, lang);

        List<YearAuditIssueType> issueTypes = classify(date, calendarDayKey, result);

        return YearAuditDayItem.builder()
                .date(date)
                .calendarDayKey(calendarDayKey)
                .resolvedDayKey(result != null ? result.getLiturgicalDayKey() : null)
                .liturgicalName(result != null ? result.getLiturgicalName() : null)
                .readingSlot(result != null ? result.getReadingSlot() : null)
                .readingSourceType(result != null ? result.getReadingSourceType() : null)
                .epistleKey(result != null ? result.getEpistleKey() : null)
                .gospelKey(result != null ? result.getGospelKey() : null)
                .fixedFeast(result != null ? result.getFixedFeast() : null)
                .movableFeast(result != null ? result.getMovableFeast() : null)
                .issueTypes(issueTypes)
                .note(buildNote(issueTypes, calendarDayKey, result))
                .build();
    }

    protected LiturgicalCalendarDay fetchExistingDayResult(LocalDate date, String lang) {
        return liturgicalCalendarDayBuilderService.buildLiturgicalDay(date, lang);
    }

    private List<YearAuditIssueType> classify(LocalDate date, String calendarDayKey, LiturgicalCalendarDay result) {
        Set<YearAuditIssueType> issues = new LinkedHashSet<>();

        boolean hasCalendarDayKey = hasText(calendarDayKey);
        boolean isSpecialMovable = isSpecialMovable(calendarDayKey);
        boolean hasResolvedDayKey = result != null && hasText(result.getLiturgicalDayKey());
        boolean hasEpistle = result != null && hasText(result.getEpistleKey());
        boolean hasGospel = result != null && hasText(result.getGospelKey());
        boolean hasAnyReading = hasEpistle || hasGospel;
        boolean hasFeast = result != null && (hasText(result.getFixedFeast()) || hasText(result.getMovableFeast()));
        String normalizedSlot = normalizeSlot(result != null ? result.getReadingSlot() : null);
        String effectiveDayKey = hasText(result != null ? result.getLiturgicalDayKey() : null)
                ? result.getLiturgicalDayKey()
                : calendarDayKey;

        if (!hasCalendarDayKey) {
            issues.add(YearAuditIssueType.MISSING_DAY_KEY);
        }

        if (isSpecialMovable && !hasResolvedDayKey) {
            issues.add(YearAuditIssueType.SPECIAL_MOVABLE_UNRESOLVED);
        }

        if (hasAnyReading && hasText(normalizedSlot)
                && !"liturgy".equals(normalizedSlot)
                && !"default".equals(normalizedSlot)) {
            issues.add(YearAuditIssueType.SLOT_MISMATCH);
        }

        if (isIntentionalGospelOnlyDayResult(effectiveDayKey, normalizedSlot, hasEpistle, hasGospel)) {
            if (issues.isEmpty()) {
                issues.add(YearAuditIssueType.OK);
            }
            return new ArrayList<>(issues);
        }

        if (hasEpistle ^ hasGospel) {
            issues.add(YearAuditIssueType.PARTIAL_READING_ONLY);
            if (!hasEpistle) {
                issues.add(YearAuditIssueType.MISSING_EPISTLE);
            }
            if (!hasGospel) {
                issues.add(YearAuditIssueType.MISSING_GOSPEL);
            }
        }

        if (!hasAnyReading) {
            if (hasFeast) {
                issues.add(YearAuditIssueType.FEAST_PRESENT_BUT_NOT_BOUND);
            } else if (shouldBeExpectedNoLiturgy(date, calendarDayKey, result)) {
                issues.add(YearAuditIssueType.EXPECTED_NO_LITURGY);
            } else if (hasCalendarDayKey || hasResolvedDayKey) {
                issues.add(YearAuditIssueType.TRUE_MISSING_ASSIGNMENT);
            }
        }

        if (issues.isEmpty()) {
            issues.add(YearAuditIssueType.OK);
        }

        return new ArrayList<>(issues);
    }

    private boolean isIntentionalGospelOnlyDayResult(
            String dayKey,
            String slot,
            boolean hasEpistle,
            boolean hasGospel
    ) {
        if (!hasText(dayKey)) {
            return false;
        }

        if (hasEpistle || !hasGospel) {
            return false;
        }

        if (!INTENTIONAL_GOSPEL_ONLY_DAY_KEYS.contains(dayKey)) {
            return false;
        }

        return "default".equals(slot) || "liturgy".equals(slot) || !hasText(slot);
    }

    private boolean shouldBeExpectedNoLiturgy(LocalDate date, String calendarDayKey, LiturgicalCalendarDay result) {
        if (date == null) {
            return false;
        }

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        if (isSpecialMovable(calendarDayKey)) {
            return false;
        }

        if (result != null && (hasText(result.getFixedFeast()) || hasText(result.getMovableFeast()))) {
            return false;
        }

        return result == null || !hasText(result.getLiturgicalDayKey());
    }

    private String buildNote(List<YearAuditIssueType> issueTypes, String calendarDayKey, LiturgicalCalendarDay result) {
        if (issueTypes == null || issueTypes.isEmpty()) {
            return null;
        }

        String effectiveDayKey = hasText(result != null ? result.getLiturgicalDayKey() : null)
                ? result.getLiturgicalDayKey()
                : calendarDayKey;

        boolean hasEpistle = result != null && hasText(result.getEpistleKey());
        boolean hasGospel = result != null && hasText(result.getGospelKey());
        String normalizedSlot = normalizeSlot(result != null ? result.getReadingSlot() : null);

        if (isIntentionalGospelOnlyDayResult(effectiveDayKey, normalizedSlot, hasEpistle, hasGospel)) {
            return "Intentional gospel-only Holy Week day-result.";
        }

        if (issueTypes.size() == 1 && issueTypes.contains(YearAuditIssueType.OK)) {
            return "Resolved with both epistle and gospel.";
        }

        if (issueTypes.contains(YearAuditIssueType.SPECIAL_MOVABLE_UNRESOLVED)) {
            return "Special movable day key was resolved by labels but not bound to a day result.";
        }

        if (issueTypes.contains(YearAuditIssueType.FEAST_PRESENT_BUT_NOT_BOUND)) {
            return "A fixed or movable feast is present, but no epistle/gospel binding was returned.";
        }

        if (issueTypes.contains(YearAuditIssueType.PARTIAL_READING_ONLY)) {
            return "Only one side of the liturgy pair is present for the resolved day.";
        }

        if (issueTypes.contains(YearAuditIssueType.SLOT_MISMATCH)) {
            return "Returned readings came from a non-liturgy slot in the daily summary surface.";
        }

        if (issueTypes.contains(YearAuditIssueType.TRUE_MISSING_ASSIGNMENT)) {
            return "A calendar day key exists, but no reading assignment was returned by the current day-result flow.";
        }

        if (issueTypes.contains(YearAuditIssueType.EXPECTED_NO_LITURGY)) {
            return "Administrative heuristic: non-Sunday/non-feast day with no resolved liturgy result.";
        }

        if (issueTypes.contains(YearAuditIssueType.MISSING_DAY_KEY)) {
            return hasText(calendarDayKey)
                    ? "The day label resolved unexpectedly but the daily result stayed empty."
                    : "No calendar day key was produced for this date.";
        }

        if (issueTypes.contains(YearAuditIssueType.MISSING_EPISTLE)) {
            return "Gospel exists but epistle is missing.";
        }

        if (issueTypes.contains(YearAuditIssueType.MISSING_GOSPEL)) {
            return "Epistle exists but gospel is missing.";
        }

        return result != null && hasText(result.getLiturgicalDayKey())
                ? "Resolved day needs review."
                : "Unresolved day needs review.";
    }

    private Map<YearAuditIssueType, Long> initializeCounts() {
        Map<YearAuditIssueType, Long> counts = new LinkedHashMap<>();
        Arrays.stream(YearAuditIssueType.values()).forEach(type -> counts.put(type, 0L));
        return counts;
    }

    private boolean isSpecialMovable(String dayKey) {
        return hasText(dayKey) && SPECIAL_MOVABLE_DAY_KEYS.contains(dayKey);
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}