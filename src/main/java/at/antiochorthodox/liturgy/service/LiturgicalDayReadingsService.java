package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarReadings;
import at.antiochorthodox.liturgy.model.ReadingGroup;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Legacy grouped-reading builder for the classic calendar response.
 *
 * <p>The new assignment flow returns {@code ServiceReadingsDto} from the v2
 * services. This class remains only for the older grouped response model used
 * by existing calendar endpoints.</p>
 */
@Service
public class LiturgicalDayReadingsService {

    private final ScriptureReadingService scriptureReadingService;
    private final SaintService saintService;
    private final FeastService feastService;
    private final LiturgicalDayContextService liturgicalDayContextService;

    public LiturgicalDayReadingsService(
            ScriptureReadingService scriptureReadingService,
            SaintService saintService,
            FeastService feastService,
            LiturgicalDayContextService liturgicalDayContextService
    ) {
        this.scriptureReadingService = scriptureReadingService;
        this.saintService = saintService;
        this.feastService = feastService;
        this.liturgicalDayContextService = liturgicalDayContextService;
    }

    public LiturgicalCalendarReadings buildGroupedReadingsForDate(LocalDate date, String lang) {
        String normalizedLang = normalizeLang(lang);

        LiturgicalDayContext context = liturgicalDayContextService.resolveForDate(date, normalizedLang);

        List<String> saints = saintService.findNamesByLangAndDate(normalizedLang, date);
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(normalizedLang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(normalizedLang, date);

        return buildGroupedReadings(context, fixedFeast, movableFeast, saints, normalizedLang);
    }

    public LiturgicalCalendarReadings buildGroupedReadings(
            LiturgicalDayContext context,
            String fixedFeast,
            String movableFeast,
            List<String> saints,
            String lang
    ) {
        String normalizedLang = normalizeLang(lang);
        ReadingGroup liturgicalGroup = buildLiturgicalDayGroupByDayKey(context, normalizedLang);
        ReadingGroup fixedGroup = buildFixedFeastGroup(fixedFeast, normalizedLang);
        ReadingGroup movableGroup = buildMovableFeastGroup(movableFeast, normalizedLang);
        List<ReadingGroup> saintGroups = buildSaintGroups(saints, normalizedLang);

        return LiturgicalCalendarReadings.builder()
                .liturgicalDay(liturgicalGroup)
                .fixedFeast(fixedGroup)
                .movableFeast(movableGroup)
                .saints(saintGroups)
                .build();
    }

    /**
     * Builds grouped readings using explicit epistle/gospel keys instead of loading
     * the liturgical-day group from dayKey.
     *
     * <p>This is used for transitional Sundays when the visible day identity is
     * virtual (for example TRANSITIONAL_SUNDAY_2024-02-11) but the actual pair of
     * readings is chosen explicitly by override.</p>
     */
    public LiturgicalCalendarReadings buildGroupedReadingsFromExplicitLiturgicalKeys(
            LiturgicalDayContext context,
            String fixedFeast,
            String movableFeast,
            List<String> saints,
            String lang
    ) {
        String normalizedLang = normalizeLang(lang);

        ReadingGroup liturgicalGroup = buildExplicitLiturgicalDayGroup(context, normalizedLang);
        ReadingGroup fixedGroup = buildFixedFeastGroup(fixedFeast, normalizedLang);
        ReadingGroup movableGroup = buildMovableFeastGroup(movableFeast, normalizedLang);
        List<ReadingGroup> saintGroups = buildSaintGroups(saints, normalizedLang);

        return LiturgicalCalendarReadings.builder()
                .liturgicalDay(liturgicalGroup)
                .fixedFeast(fixedGroup)
                .movableFeast(movableGroup)
                .saints(saintGroups)
                .build();
    }

    /**
     * Compatibility overload for callers that still pass the liturgical name
     * directly instead of a resolved context.
     */
    public LiturgicalCalendarReadings buildGroupedReadings(
            String liturgicalName,
            String fixedFeast,
            String movableFeast,
            List<String> saints,
            String lang
    ) {
        String normalizedLang = normalizeLang(lang);

        ReadingGroup liturgicalGroup = null;
        if (hasText(liturgicalName)) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(liturgicalName, "any", normalizedLang);

            liturgicalGroup = ReadingGroup.builder()
                    .key(liturgicalName)
                    .label("Liturgical Day Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup fixedGroup = buildFixedFeastGroup(fixedFeast, normalizedLang);
        ReadingGroup movableGroup = buildMovableFeastGroup(movableFeast, normalizedLang);
        List<ReadingGroup> saintGroups = buildSaintGroups(saints, normalizedLang);

        return LiturgicalCalendarReadings.builder()
                .liturgicalDay(liturgicalGroup)
                .fixedFeast(fixedGroup)
                .movableFeast(movableGroup)
                .saints(saintGroups)
                .build();
    }

    private ReadingGroup buildLiturgicalDayGroupByDayKey(LiturgicalDayContext context, String lang) {
        if (context == null || !hasText(context.getDayKey())) {
            return null;
        }

        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByDayKey(context.getDayKey(), "any", lang);

        return ReadingGroup.builder()
                .key(context.getDayKey())
                .label(hasText(context.getDayLabel()) ? context.getDayLabel() : "Liturgical Day Readings")
                .desc(context.getDayLabel())
                .readings(readings)
                .build();
    }

    private ReadingGroup buildExplicitLiturgicalDayGroup(LiturgicalDayContext context, String lang) {
        if (context == null) {
            return null;
        }

        List<ScriptureReading> readings = new ArrayList<>();

        if (hasText(context.getEpistleKey())) {
            ScriptureReading epistle = scriptureReadingService.getReadingByKey(context.getEpistleKey(), lang);
            if (epistle != null) {
                enrichReadingForContext(epistle, context, "epistle");
                readings.add(epistle);
            }
        }

        if (hasText(context.getGospelKey())) {
            ScriptureReading gospel = scriptureReadingService.getReadingByKey(context.getGospelKey(), lang);
            if (gospel != null) {
                enrichReadingForContext(gospel, context, "gospel");
                readings.add(gospel);
            }
        }

        if (readings.isEmpty()) {
            return null;
        }

        return ReadingGroup.builder()
                .key(firstNonBlank(context.getReadingDayKey(), context.getDayKey()))
                .label(hasText(context.getDayLabel()) ? context.getDayLabel() : "Liturgical Day Readings")
                .desc(context.getDayLabel())
                .readings(readings)
                .build();
    }

    private void enrichReadingForContext(
            ScriptureReading reading,
            LiturgicalDayContext context,
            String type
    ) {
        if (reading == null || context == null) {
            return;
        }

        reading.setDayKey(firstNonBlank(context.getReadingDayKey(), context.getDayKey()));
        reading.setSlot(context.getSlot());
        reading.setSourceType(context.getSourceType());
        reading.setType(type);
    }

    private ReadingGroup buildFixedFeastGroup(String fixedFeast, String lang) {
        if (!hasText(fixedFeast)) {
            return null;
        }

        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByLegacyName(fixedFeast, "any", lang);

        return ReadingGroup.builder()
                .key(fixedFeast)
                .label("Fixed Feast Readings")
                .readings(readings)
                .build();
    }

    private ReadingGroup buildMovableFeastGroup(String movableFeast, String lang) {
        if (!hasText(movableFeast)) {
            return null;
        }

        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByLegacyName(movableFeast, "any", lang);

        return ReadingGroup.builder()
                .key(movableFeast)
                .label("Movable Feast Readings")
                .readings(readings)
                .build();
    }

    private List<ReadingGroup> buildSaintGroups(List<String> saints, String lang) {
        List<ReadingGroup> saintGroups = new ArrayList<>();

        if (saints == null) {
            return saintGroups;
        }

        for (String saint : saints) {
            if (!hasText(saint)) {
                continue;
            }

            String key = "عيد القديس " + saint;
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(key, "any", lang);

            if (readings == null || readings.isEmpty()) {
                continue;
            }

            saintGroups.add(ReadingGroup.builder()
                    .key(key)
                    .label("Saint Readings")
                    .desc(saint)
                    .readings(readings)
                    .build());
        }

        return saintGroups;
    }

    private String firstNonBlank(String first, String second) {
        return hasText(first) ? first : second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
}