package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.*;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LiturgicalDayReadingsService {

    private final ScriptureReadingService scriptureReadingService;
    private final SaintService saintService;
    private final FeastService feastService;
    private final LiturgicalLabelService liturgicalLabelService;
    private final PaschaDateCalculator paschaDateCalculator;

    public LiturgicalDayReadingsService(
            ScriptureReadingService scriptureReadingService,
            SaintService saintService,
            FeastService feastService,
            LiturgicalLabelService liturgicalLabelService,
            PaschaDateCalculator paschaDateCalculator
    ) {
        this.scriptureReadingService = scriptureReadingService;
        this.saintService = saintService;
        this.feastService = feastService;
        this.liturgicalLabelService = liturgicalLabelService;
        this.paschaDateCalculator = paschaDateCalculator;
    }

    // ✅ الجديد: يبني قراءات اليوم مباشرة من date/lang
    public LiturgicalCalendarReadings buildGroupedReadingsForDate(LocalDate date, String lang) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());

        String liturgicalName = liturgicalLabelService.getLabelForDate(date, pascha, lang);
        List<String> saints = saintService.findNamesByLangAndDate(lang, date);
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);

        return buildGroupedReadings(liturgicalName, fixedFeast, movableFeast, saints, lang);
    }

    // ✅ هذا نفس كودك الحالي (كما هو)
    public LiturgicalCalendarReadings buildGroupedReadings(
            String liturgicalName,
            String fixedFeast,
            String movableFeast,
            List<String> saints,
            String lang
    ) {
        ReadingGroup liturgicalGroup = null;
        if (liturgicalName != null && !liturgicalName.isBlank()) {
            List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(liturgicalName, "any", lang);
            liturgicalGroup = ReadingGroup.builder()
                    .key(liturgicalName)
                    .label("Liturgical Day Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup fixedGroup = null;
        if (fixedFeast != null && !fixedFeast.isBlank()) {
            List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(fixedFeast, "any", lang);
            fixedGroup = ReadingGroup.builder()
                    .key(fixedFeast)
                    .label("Fixed Feast Readings")
                    .readings(readings)
                    .build();
        }

        ReadingGroup movableGroup = null;
        if (movableFeast != null && !movableFeast.isBlank()) {
            List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(movableFeast, "any", lang);
            movableGroup = ReadingGroup.builder()
                    .key(movableFeast)
                    .label("Movable Feast Readings")
                    .readings(readings)
                    .build();
        }

        List<ReadingGroup> saintGroups = new ArrayList<>();
        if (saints != null) {
            for (String saint : saints) {
                if (saint == null || saint.isBlank()) continue;

                String key = "عيد القديس " + saint;
                List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(key, "any", lang);
                if (readings == null || readings.isEmpty()) continue;

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