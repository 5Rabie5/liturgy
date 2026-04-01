package at.antiochorthodox.liturgy.reading.legacy.service;

import at.antiochorthodox.liturgy.reading.legacy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.reading.legacy.model.LiturgicalCalendarReadings;
import at.antiochorthodox.liturgy.reading.legacy.model.ReadingGroup;
import at.antiochorthodox.liturgy.reading.legacy.model.ScriptureReading;
import org.springframework.stereotype.Service;
import at.antiochorthodox.liturgy.service.SaintService;
import at.antiochorthodox.liturgy.service.FeastService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * NEW:
     * يبني قراءات اليوم من التاريخ مباشرة باستخدام dayKey / readingKey
     * مع الإبقاء على الأعياد والقديسين على legacy text lookup مؤقتًا.
     */
    public LiturgicalCalendarReadings buildGroupedReadingsForDate(LocalDate date, String lang) {
        LiturgicalDayContext context = liturgicalDayContextService.resolveForDate(date, lang);

        List<String> saints = saintService.findNamesByLangAndDate(lang, date);
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);

        return buildGroupedReadings(context, fixedFeast, movableFeast, saints, lang);
    }

    /**
     * NEW:
     * البناء الأساسي الجديد المعتمد على LiturgicalDayContext.
     */
    public LiturgicalCalendarReadings buildGroupedReadings(
            LiturgicalDayContext context,
            String fixedFeast,
            String movableFeast,
            List<String> saints,
            String lang
    ) {
        ReadingGroup liturgicalGroup = null;

        if (context != null && context.getDayKey() != null && !context.getDayKey().isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByDayKey(context.getDayKey(), "any", lang);

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
                    scriptureReadingService.getReadingsByLegacyName(fixedFeast, "any", lang);

            fixedGroup = ReadingGroup.builder()
                    .key(fixedFeast)
                    .label("Fixed Feast Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup movableGroup = null;
        if (movableFeast != null && !movableFeast.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(movableFeast, "any", lang);

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
        }

        return LiturgicalCalendarReadings.builder()
                .liturgicalDay(liturgicalGroup)
                .fixedFeast(fixedGroup)
                .movableFeast(movableGroup)
                .saints(saintGroups)
                .build();
    }

    /**
     * LEGACY COMPATIBILITY:
     * هذا الميثود أبقيته حتى لا ينكسر أي كود قديم ما زال يمرر liturgicalName كنص.
     */
    public LiturgicalCalendarReadings buildGroupedReadings(
            String liturgicalName,
            String fixedFeast,
            String movableFeast,
            List<String> saints,
            String lang
    ) {
        ReadingGroup liturgicalGroup = null;
        if (liturgicalName != null && !liturgicalName.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(liturgicalName, "any", lang);

            liturgicalGroup = ReadingGroup.builder()
                    .key(liturgicalName)
                    .label("Liturgical Day Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup fixedGroup = null;
        if (fixedFeast != null && !fixedFeast.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(fixedFeast, "any", lang);

            fixedGroup = ReadingGroup.builder()
                    .key(fixedFeast)
                    .label("Fixed Feast Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup movableGroup = null;
        if (movableFeast != null && !movableFeast.isBlank()) {
            List<ScriptureReading> readings =
                    scriptureReadingService.getReadingsByLegacyName(movableFeast, "any", lang);

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
        }

        return LiturgicalCalendarReadings.builder()
                .liturgicalDay(liturgicalGroup)
                .fixedFeast(fixedGroup)
                .movableFeast(movableGroup)
                .saints(saintGroups)
                .build();
    }
}