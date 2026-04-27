package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.dto.TransitionalSundayResolution;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarReadings;
import at.antiochorthodox.liturgy.model.ReadingGroup;
import at.antiochorthodox.liturgy.reading.v2.service.ReadingQueryService;
import at.antiochorthodox.liturgy.reading.v2.summary.SundayReadingSummary;
import at.antiochorthodox.liturgy.reading.v2.summary.SundayReadingSummarySelector;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class LiturgicalCalendarDayBuilderService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";
    private static final String DEFAULT_SLOT = "liturgy";
    private static final String DEFAULT_SOURCE_TYPE = "day";

    private final SaintService saintService;
    private final FeastService feastService;
    private final FastingService fastingService;
    private final MarriageAllowedService marriageAllowedService;
    private final LiturgicalDayReadingsService liturgicalDayReadingsService;
    private final LiturgicalDayContextService liturgicalDayContextService;
    private final ReadingQueryService readingQueryService;
    private final SundayReadingSummarySelector sundayReadingSummarySelector;
    private final TransitionalSundayResolverService transitionalSundayResolverService;

    public LiturgicalCalendarDayBuilderService(
            SaintService saintService,
            FeastService feastService,
            FastingService fastingService,
            MarriageAllowedService marriageAllowedService,
            LiturgicalDayReadingsService liturgicalDayReadingsService,
            LiturgicalDayContextService liturgicalDayContextService,
            ReadingQueryService readingQueryService,
            SundayReadingSummarySelector sundayReadingSummarySelector,
            TransitionalSundayResolverService transitionalSundayResolverService
    ) {
        this.saintService = saintService;
        this.feastService = feastService;
        this.fastingService = fastingService;
        this.marriageAllowedService = marriageAllowedService;
        this.liturgicalDayReadingsService = liturgicalDayReadingsService;
        this.liturgicalDayContextService = liturgicalDayContextService;
        this.readingQueryService = readingQueryService;
        this.sundayReadingSummarySelector = sundayReadingSummarySelector;
        this.transitionalSundayResolverService = transitionalSundayResolverService;
    }

    public LiturgicalCalendarDay buildLiturgicalDay(LocalDate date, String lang) {
        String normalizedLang = normalizeLang(lang);

        LiturgicalDayContext context = liturgicalDayContextService.resolveForDate(date, normalizedLang);

        List<String> saints = saintService.findNamesByLangAndDate(normalizedLang, date);
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(normalizedLang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(normalizedLang, date);
        String fastingLevel = fastingService.getFastingEvelByLangAndDate(normalizedLang, date);
        MarriageAllowedResponse marriageInfo = marriageAllowedService.isMarriageAllowed(date, normalizedLang);

        LiturgicalCalendarReadings grouped = liturgicalDayReadingsService.buildGroupedReadings(
                context,
                fixedFeast,
                movableFeast,
                saints,
                normalizedLang
        );

        LiturgicalCalendarDay calendarDay = LiturgicalCalendarDay.builder()
                .date(date)
                .liturgicalName(context != null ? context.getDayLabel() : null)
                .liturgicalDayKey(context != null ? context.getDayKey() : null)
                .readingSlot(context != null ? context.getSlot() : null)
                .readingSourceType(context != null ? context.getSourceType() : null)
                .epistleKey(context != null ? context.getEpistleKey() : null)
                .gospelKey(context != null ? context.getGospelKey() : null)
                .saints(saints)
                .fixedFeast(fixedFeast)
                .movableFeast(movableFeast)
                .fastingLevel(fastingLevel)
                .lang(normalizedLang)
                .marriageAllowed(marriageInfo.isAllowed())
                .marriageNote(marriageInfo.getMessage())
                .readings(grouped)
                .build();

        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            applySundayReadingSummaryFromV2OrFallback(date, normalizedLang, calendarDay);
            applyTransitionalSundayResolution(date, normalizedLang, calendarDay);
        }

        return calendarDay;
    }

    private void applyTransitionalSundayResolution(
            LocalDate date,
            String lang,
            LiturgicalCalendarDay calendarDay
    ) {
        TransitionalSundayResolution transitional = transitionalSundayResolverService.resolve(date, lang, calendarDay);

        if (!transitional.isInWindow()) {
            return;
        }

        calendarDay.setEpistleKey(transitional.getEffectiveEpistleKey());
        calendarDay.setGospelKey(transitional.getEffectiveGospelKey());

        if (hasText(transitional.getEffectiveDayKey())) {
            calendarDay.setLiturgicalDayKey(transitional.getEffectiveDayKey());
        }
        if (hasText(transitional.getEffectiveLiturgicalName())) {
            calendarDay.setLiturgicalName(transitional.getEffectiveLiturgicalName());
        }

        if (transitional.isOverrideApplied() && !hasText(calendarDay.getReadingSlot())) {
            calendarDay.setReadingSlot(DEFAULT_SLOT);
        }
        if (transitional.isOverrideApplied() && !hasText(calendarDay.getReadingSourceType())) {
            calendarDay.setReadingSourceType(DEFAULT_SOURCE_TYPE);
        }

        calendarDay.setDecisionBasis(transitional.getDecisionBasis());
        calendarDay.setSourceReference(transitional.getSourceReference());

        if (hasText(transitional.getNote())) {
            calendarDay.setDesc(!hasText(calendarDay.getDesc())
                    ? transitional.getNote()
                    : calendarDay.getDesc() + " | " + transitional.getNote());
        }

        if (shouldRebuildGroupedReadingsForTransitional(calendarDay, transitional)) {
            LiturgicalDayContext transitionalContext = LiturgicalDayContext.builder()
                    .tradition(DEFAULT_TRADITION)
                    .dayKey(calendarDay.getLiturgicalDayKey())
                    .calendarDayKey(calendarDay.getLiturgicalDayKey())
                    .readingDayKey(calendarDay.getLiturgicalDayKey())
                    .dayLabel(calendarDay.getLiturgicalName())
                    .slot(hasText(calendarDay.getReadingSlot()) ? calendarDay.getReadingSlot() : DEFAULT_SLOT)
                    .sourceType(hasText(calendarDay.getReadingSourceType()) ? calendarDay.getReadingSourceType() : DEFAULT_SOURCE_TYPE)
                    .epistleKey(calendarDay.getEpistleKey())
                    .gospelKey(calendarDay.getGospelKey())
                    .build();

            LiturgicalCalendarReadings rebuilt =
                    liturgicalDayReadingsService.buildGroupedReadingsFromExplicitLiturgicalKeys(
                            transitionalContext,
                            calendarDay.getFixedFeast(),
                            calendarDay.getMovableFeast(),
                            calendarDay.getSaints(),
                            lang
                    );

            calendarDay.setReadings(rebuilt);
        }
    }

    private boolean shouldRebuildGroupedReadingsForTransitional(
            LiturgicalCalendarDay calendarDay,
            TransitionalSundayResolution transitional
    ) {
        if (calendarDay == null || transitional == null) {
            return false;
        }

        if (!transitional.isOverrideApplied()) {
            return false;
        }

        if (!hasText(calendarDay.getEpistleKey()) && !hasText(calendarDay.getGospelKey())) {
            return false;
        }

        return true;
    }

    private void applySundayReadingSummaryFromV2OrFallback(
            LocalDate date,
            String lang,
            LiturgicalCalendarDay calendarDay
    ) {
        SundayReadingSummary summary = null;

        try {
            List<ServiceReadingsDto> services = readingQueryService.getByDate(date, lang, DEFAULT_TRADITION);
            summary = sundayReadingSummarySelector.selectBestForSunday(services).orElse(null);
        } catch (RuntimeException ignored) {
            // Fall back to legacy grouped readings when v2 resolution is unavailable.
        }

        if (summary == null || summary.isEmpty()) {
            summary = fallbackSundaySummaryFromLegacyGroups(calendarDay.getReadings());
        }

        if (summary == null || summary.isEmpty()) {
            return;
        }

        applySummaryToCalendarDay(calendarDay, summary);
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return "ar";
        }

        String cleaned = lang.trim().toLowerCase().replaceAll("[^a-z_]", "");
        if (cleaned.isBlank()) {
            return "ar";
        }

        return cleaned;
    }

    private SundayReadingSummary fallbackSundaySummaryFromLegacyGroups(LiturgicalCalendarReadings readings) {
        if (readings == null) {
            return null;
        }

        SundayReadingSummary movableFeastSummary = fromLegacyGroup("movable_feast", readings.getMovableFeast());
        if (movableFeastSummary != null && !movableFeastSummary.isEmpty()) {
            return movableFeastSummary;
        }

        SundayReadingSummary fixedFeastSummary = fromLegacyGroup("fixed_feast", readings.getFixedFeast());
        if (fixedFeastSummary != null && !fixedFeastSummary.isEmpty()) {
            return fixedFeastSummary;
        }

        SundayReadingSummary liturgicalDaySummary = fromLegacyGroup("day", readings.getLiturgicalDay());
        if (liturgicalDaySummary != null && !liturgicalDaySummary.isEmpty()) {
            return liturgicalDaySummary;
        }

        return null;
    }

    private SundayReadingSummary fromLegacyGroup(String sourceType, ReadingGroup group) {
        if (group == null || group.getReadings() == null || group.getReadings().isEmpty()) {
            return null;
        }

        String epistleKey = group.getReadings().stream()
                .filter(Objects::nonNull)
                .filter(r -> "epistle".equalsIgnoreCase(r.getType()))
                .map(r -> r.getReadingKey())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        String gospelKey = group.getReadings().stream()
                .filter(Objects::nonNull)
                .filter(r -> "gospel".equalsIgnoreCase(r.getType()))
                .map(r -> r.getReadingKey())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (epistleKey == null && gospelKey == null) {
            return null;
        }

        return new SundayReadingSummary("liturgy", sourceType, epistleKey, gospelKey);
    }

    private void applySummaryToCalendarDay(LiturgicalCalendarDay calendarDay, SundayReadingSummary summary) {
        calendarDay.setReadingSlot(summary.slot());
        calendarDay.setReadingSourceType(summary.sourceType());
        calendarDay.setEpistleKey(summary.epistleKey());
        calendarDay.setGospelKey(summary.gospelKey());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}