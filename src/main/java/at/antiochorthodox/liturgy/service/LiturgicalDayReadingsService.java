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
        ReadingGroup liturgicalGroup = null;

        if (context != null && context.getDayKey() != null && !context.getDayKey().isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByDayKey(context.getDayKey(), "any", normalizedLang);

            liturgicalGroup = ReadingGroup.builder()
                    .key(context.getDayKey())
                    .label(context.getDayLabel() != null ? context.getDayLabel() : "Liturgical Day Readings")
                    .desc(context.getDayLabel())
                    .readings(readings)
                    .build();
        }

        ReadingGroup fixedGroup = null;
        if (fixedFeast != null && !fixedFeast.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(fixedFeast, "any", normalizedLang);

            fixedGroup = ReadingGroup.builder()
                    .key(fixedFeast)
                    .label("Fixed Feast Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup movableGroup = null;
        if (movableFeast != null && !movableFeast.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(movableFeast, "any", normalizedLang);

            movableGroup = ReadingGroup.builder()
                    .key(movableFeast)
                    .label("Movable Feast Readings")
                    .readings(readings)
                    .build();
        }

        List<ReadingGroup> saintGroups = new ArrayList<>();
        if (saints != null) {
            for (String saint : saints) {
                if (saint == null || saint.isBlank()) {
                    continue;
                }

                String key = "عيد القديس " + saint;
                List<ScriptureReading> readings =
                        scriptureReadingService.getReadingsByLegacyName(key, "any", normalizedLang);

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
        }

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
        if (liturgicalName != null && !liturgicalName.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(liturgicalName, "any", normalizedLang);

            liturgicalGroup = ReadingGroup.builder()
                    .key(liturgicalName)
                    .label("Liturgical Day Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup fixedGroup = null;
        if (fixedFeast != null && !fixedFeast.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(fixedFeast, "any", normalizedLang);

            fixedGroup = ReadingGroup.builder()
                    .key(fixedFeast)
                    .label("Fixed Feast Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup movableGroup = null;
        if (movableFeast != null && !movableFeast.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(movableFeast, "any", normalizedLang);

            movableGroup = ReadingGroup.builder()
                    .key(movableFeast)
                    .label("Movable Feast Readings")
                    .readings(readings)
                    .build();
        }

        List<ReadingGroup> saintGroups = new ArrayList<>();
        if (saints != null) {
            for (String saint : saints) {
                if (saint == null || saint.isBlank()) {
                    continue;
                }

                String key = "عيد القديس " + saint;
                List<ScriptureReading> readings =
                        scriptureReadingService.getReadingsByLegacyName(key, "any", normalizedLang);

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
        }

        return LiturgicalCalendarReadings.builder()
                .liturgicalDay(liturgicalGroup)
                .fixedFeast(fixedGroup)
                .movableFeast(movableGroup)
                .saints(saintGroups)
                .build();
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
