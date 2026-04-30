package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.YearAuditDayItem;
import at.antiochorthodox.liturgy.dto.YearAuditReport;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.model.YearAuditIssueType;
import at.antiochorthodox.liturgy.repository.LiturgicalReadingAssignmentRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.TemporalAdjusters;
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
    private static final String TRADITION = "ANTIOCHIAN";
    private static final String LITURGY_SLOT = "liturgy";

    private static final String READING_TYPE_EPISTLE = "epistle";
    private static final String READING_TYPE_GOSPEL = "gospel";

    private static final String CROSS_AFTER_SUNDAY_DAY_KEY = "CROSS_AFTER_SUNDAY";
    private static final String ANNUNCIATION_DAY_KEY = "ANNUNCIATION";

    private static final MonthDay CROSS_FEAST_DATE = MonthDay.of(9, 14);
    private static final MonthDay FATHERS_7TH_COUNCIL_WINDOW_START = MonthDay.of(10, 11);
    private static final MonthDay FATHERS_7TH_COUNCIL_WINDOW_END = MonthDay.of(10, 17);
    private static final MonthDay FOREFATHERS_WINDOW_START = MonthDay.of(12, 11);
    private static final MonthDay FOREFATHERS_WINDOW_END = MonthDay.of(12, 17);
    private static final MonthDay SUNDAY_BEFORE_NATIVITY_WINDOW_START = MonthDay.of(12, 18);
    private static final MonthDay SUNDAY_BEFORE_NATIVITY_WINDOW_END = MonthDay.of(12, 24);

    private static final Map<MonthDay, String> FIXED_DATE_FEAST_DAY_KEYS = Map.of(
            MonthDay.of(3, 25), ANNUNCIATION_DAY_KEY
    );

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
    private final LiturgicalReadingAssignmentRepository assignmentRepository;

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
                .filter(item -> !(item.getIssueTypes().size() == 1
                        && item.getIssueTypes().contains(YearAuditIssueType.OK)))
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

        LiturgicalCalendarDay rawResult = fetchExistingDayResult(date, lang);
        AuditDayResult result = resolveAuditBinding(date, rawResult);

        List<YearAuditIssueType> issueTypes = classify(date, calendarDayKey, result);

        return YearAuditDayItem.builder()
                .date(date)
                .calendarDayKey(calendarDayKey)
                .resolvedDayKey(result != null ? result.liturgicalDayKey : null)
                .liturgicalName(result != null ? result.liturgicalName : null)
                .readingSlot(result != null ? result.readingSlot : null)
                .readingSourceType(result != null ? result.readingSourceType : null)
                .epistleKey(result != null ? result.epistleKey : null)
                .gospelKey(result != null ? result.gospelKey : null)
                .fixedFeast(result != null ? result.fixedFeast : null)
                .movableFeast(result != null ? result.movableFeast : null)
                .issueTypes(issueTypes)
                .note(buildNote(issueTypes, calendarDayKey, result))
                .build();
    }

    protected LiturgicalCalendarDay fetchExistingDayResult(LocalDate date, String lang) {
        return liturgicalCalendarDayBuilderService.buildLiturgicalDay(date, lang);
    }

    private AuditDayResult resolveAuditBinding(LocalDate date, LiturgicalCalendarDay rawResult) {
        AuditDayResult result = AuditDayResult.from(rawResult);
        if (result == null) {
            return null;
        }

        if (hasText(result.epistleKey) && hasText(result.gospelKey)) {
            return result;
        }

        String specialFeastDayKey = resolveSpecialFeastDayKey(date);
        if (!hasText(specialFeastDayKey)) {
            return result;
        }

        List<LiturgicalReadingAssignment> assignments =
                assignmentRepository.findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(
                        TRADITION,
                        specialFeastDayKey,
                        LITURGY_SLOT
                );

        if (assignments == null || assignments.isEmpty()) {
            return result;
        }

        String epistleKey = firstNonBlank(
                findReadingKey(assignments, READING_TYPE_EPISTLE),
                result.epistleKey
        );

        String gospelKey = firstNonBlank(
                findReadingKey(assignments, READING_TYPE_GOSPEL),
                result.gospelKey
        );

        String sourceType = firstNonBlank(
                findFirstSourceType(assignments),
                result.readingSourceType
        );

        String liturgicalName = firstNonBlank(
                result.liturgicalName,
                firstNonBlank(result.movableFeast, result.fixedFeast)
        );

        return new AuditDayResult(
                specialFeastDayKey,
                liturgicalName,
                LITURGY_SLOT,
                sourceType,
                epistleKey,
                gospelKey,
                result.fixedFeast,
                result.movableFeast
        );
    }

    private String resolveSpecialFeastDayKey(LocalDate date) {
        if (date == null) {
            return null;
        }

        String fixedDateDayKey = FIXED_DATE_FEAST_DAY_KEYS.get(MonthDay.from(date));
        if (hasText(fixedDateDayKey)) {
            return fixedDateDayKey;
        }

        if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return null;
        }

        if (isSundayAfterCross(date)) {
            return CROSS_AFTER_SUNDAY_DAY_KEY;
        }

        if (isFathersSeventhEcumenicalCouncilSunday(date)) {
            return LiturgicalLabelService.DAY_KEY_FATHERS_7TH_ECUMENICAL_COUNCIL_SUNDAY;
        }

        if (isForefathersSunday(date)) {
            return LiturgicalLabelService.DAY_KEY_FOREFATHERS_SUNDAY;
        }

        if (isSundayBeforeNativity(date)) {
            return LiturgicalLabelService.DAY_KEY_SUNDAY_BEFORE_NATIVITY;
        }

        return null;
    }

    private boolean isSundayAfterCross(LocalDate date) {
        LocalDate crossFeast = CROSS_FEAST_DATE.atYear(date.getYear());
        LocalDate sundayAfterCross = crossFeast.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        return date.equals(sundayAfterCross);
    }

    private boolean isFathersSeventhEcumenicalCouncilSunday(LocalDate date) {
        return isSundayInWindow(date, FATHERS_7TH_COUNCIL_WINDOW_START, FATHERS_7TH_COUNCIL_WINDOW_END);
    }

    private boolean isForefathersSunday(LocalDate date) {
        return isSundayInWindow(date, FOREFATHERS_WINDOW_START, FOREFATHERS_WINDOW_END);
    }

    private boolean isSundayBeforeNativity(LocalDate date) {
        return isSundayInWindow(date, SUNDAY_BEFORE_NATIVITY_WINDOW_START, SUNDAY_BEFORE_NATIVITY_WINDOW_END);
    }

    private boolean isSundayInWindow(LocalDate date, MonthDay start, MonthDay end) {
        if (date == null || date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return false;
        }

        MonthDay current = MonthDay.from(date);
        return !current.isBefore(start) && !current.isAfter(end);
    }

    private String findReadingKey(List<LiturgicalReadingAssignment> assignments, String readingType) {
        if (assignments == null || assignments.isEmpty() || !hasText(readingType)) {
            return null;
        }

        return assignments.stream()
                .filter(a -> a != null && readingType.equalsIgnoreCase(a.getReadingType()))
                .map(LiturgicalReadingAssignment::getReadingKey)
                .filter(this::hasText)
                .findFirst()
                .orElse(null);
    }

    private String findFirstSourceType(List<LiturgicalReadingAssignment> assignments) {
        if (assignments == null || assignments.isEmpty()) {
            return null;
        }

        return assignments.stream()
                .filter(a -> a != null && hasText(a.getSourceType()))
                .map(LiturgicalReadingAssignment::getSourceType)
                .findFirst()
                .orElse(null);
    }

    private List<YearAuditIssueType> classify(LocalDate date, String calendarDayKey, AuditDayResult result) {
        Set<YearAuditIssueType> issues = new LinkedHashSet<>();

        boolean hasCalendarDayKey = hasText(calendarDayKey);
        boolean isSpecialMovable = isSpecialMovable(calendarDayKey);
        boolean hasResolvedDayKey = result != null && hasText(result.liturgicalDayKey);
        boolean hasEpistle = result != null && hasText(result.epistleKey);
        boolean hasGospel = result != null && hasText(result.gospelKey);
        boolean hasAnyReading = hasEpistle || hasGospel;
        boolean hasFeast = result != null && (hasText(result.fixedFeast) || hasText(result.movableFeast));
        String normalizedSlot = normalizeSlot(result != null ? result.readingSlot : null);
        String effectiveDayKey = hasText(result != null ? result.liturgicalDayKey : null)
                ? result.liturgicalDayKey
                : calendarDayKey;

        if (!hasCalendarDayKey && !hasResolvedDayKey) {
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

    private boolean shouldBeExpectedNoLiturgy(LocalDate date, String calendarDayKey, AuditDayResult result) {
        if (date == null) {
            return false;
        }

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        if (isSpecialMovable(calendarDayKey)) {
            return false;
        }

        if (isKnownLiturgicalDayThatMustNotBeExpectedNoLiturgy(date, calendarDayKey, result)) {
            return false;
        }

        if (result != null && (hasText(result.fixedFeast) || hasText(result.movableFeast))) {
            return false;
        }

        return result == null || !hasText(result.liturgicalDayKey);
    }

    private boolean isKnownLiturgicalDayThatMustNotBeExpectedNoLiturgy(
            LocalDate date,
            String calendarDayKey,
            AuditDayResult result
    ) {
        if (result != null && (hasText(result.fixedFeast) || hasText(result.movableFeast))) {
            return true;
        }

        if (!hasText(calendarDayKey)) {
            return false;
        }

        if (calendarDayKey.startsWith("PASCHA_")) {
            return true;
        }

        return calendarDayKey.startsWith("TRIODION_")
                && date != null
                && date.getDayOfWeek() == DayOfWeek.SATURDAY;
    }

    private String buildNote(List<YearAuditIssueType> issueTypes, String calendarDayKey, AuditDayResult result) {
        if (issueTypes == null || issueTypes.isEmpty()) {
            return null;
        }

        String effectiveDayKey = hasText(result != null ? result.liturgicalDayKey : null)
                ? result.liturgicalDayKey
                : calendarDayKey;

        boolean hasEpistle = result != null && hasText(result.epistleKey);
        boolean hasGospel = result != null && hasText(result.gospelKey);
        String normalizedSlot = normalizeSlot(result != null ? result.readingSlot : null);

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

        return result != null && hasText(result.liturgicalDayKey)
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

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static class AuditDayResult {
        private final String liturgicalDayKey;
        private final String liturgicalName;
        private final String readingSlot;
        private final String readingSourceType;
        private final String epistleKey;
        private final String gospelKey;
        private final String fixedFeast;
        private final String movableFeast;

        private AuditDayResult(
                String liturgicalDayKey,
                String liturgicalName,
                String readingSlot,
                String readingSourceType,
                String epistleKey,
                String gospelKey,
                String fixedFeast,
                String movableFeast
        ) {
            this.liturgicalDayKey = liturgicalDayKey;
            this.liturgicalName = liturgicalName;
            this.readingSlot = readingSlot;
            this.readingSourceType = readingSourceType;
            this.epistleKey = epistleKey;
            this.gospelKey = gospelKey;
            this.fixedFeast = fixedFeast;
            this.movableFeast = movableFeast;
        }

        private static AuditDayResult from(LiturgicalCalendarDay day) {
            if (day == null) {
                return null;
            }

            return new AuditDayResult(
                    day.getLiturgicalDayKey(),
                    day.getLiturgicalName(),
                    day.getReadingSlot(),
                    day.getReadingSourceType(),
                    day.getEpistleKey(),
                    day.getGospelKey(),
                    day.getFixedFeast(),
                    day.getMovableFeast()
            );
        }
    }
}