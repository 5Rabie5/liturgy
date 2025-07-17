package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.model.ScriptureReadingOption;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
public class ScriptureReadingResolverService {

    private final ScriptureReadingService scriptureReadingService;
    private final SaintService saintService;
    private final FeastService feastService;
    private final LiturgicalWeekService liturgicalWeekService;
    private final PaschaDateCalculator paschaDateCalculator;

    public ScriptureReadingResolverService(
            ScriptureReadingService scriptureReadingService,
            SaintService saintService,
            FeastService feastService,
            LiturgicalWeekService liturgicalWeekService,
            PaschaDateCalculator paschaDateCalculator
    ) {
        this.scriptureReadingService = scriptureReadingService;
        this.saintService = saintService;
        this.feastService = feastService;
        this.liturgicalWeekService = liturgicalWeekService;
        this.paschaDateCalculator = paschaDateCalculator;
    }

    public List<ScriptureReadingOption> getAllReadingOptionsForDay(LocalDate date, String lang) {
        List<ScriptureReadingOption> options = new ArrayList<>();

        // 1. Liturgical Name
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        LocalDate nextPhariseePublican = paschaDateCalculator.getNextPhariseePublicanSunday(pascha);
        String liturgicalName = liturgicalWeekService.findLiturgicalNameForDate(date, pascha, nextPhariseePublican, lang);
        if (liturgicalName != null) {
            List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(liturgicalName, lang, "liturgicalName", liturgicalName);
            options.add(toScriptureReadingOption("Sunday/Liturgical Readings", "liturgicalName", liturgicalName, readings, lang, true));
        }

        // 2. Fixed Feast
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        if (fixedFeast != null) {
            List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(fixedFeast, lang, "fixedFeast", fixedFeast);
            options.add(toScriptureReadingOption("Fixed Feast Readings", "fixedFeast", fixedFeast, readings, lang, false));
        }

        // 3. Movable Feast
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
        if (movableFeast != null) {
            List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(movableFeast, lang, "movableFeast", movableFeast);
            options.add(toScriptureReadingOption("Movable Feast Readings", "movableFeast", movableFeast, readings, lang, false));
        }

        // 4. Saints
        List<String> saints = saintService.findNamesByLangAndDate(lang, date);
        if (saints != null) {
            for (String saint : saints) {
                String key = "عيد القديس " + saint;
                List<ScriptureReading> readings = scriptureReadingService.getReadingsByLiturgicalName(key, lang, "saint", saint);
                if (!readings.isEmpty()) {
                    options.add(toScriptureReadingOption("Saint Readings", "saint", saint, readings, lang, false));
                }
            }
        }
        return options;
    }

    private ScriptureReadingOption toScriptureReadingOption(String label, String reason, String reasonDetail, List<ScriptureReading> readings, String lang, boolean preferred) {
        // Pick first epistle/gospel, rest as alternative
        ScriptureReading epistle = readings.stream().filter(r -> "epistle".equalsIgnoreCase(r.getType())).findFirst().orElse(null);
        ScriptureReading gospel = readings.stream().filter(r -> "gospel".equalsIgnoreCase(r.getType())).findFirst().orElse(null);
        List<ScriptureReading> alternative = readings.stream()
                .filter(r -> !"epistle".equalsIgnoreCase(r.getType()) && !"gospel".equalsIgnoreCase(r.getType()))
                .toList();

        return ScriptureReadingOption.builder()
                .label(label)
                .reason(reason)
                .reasonDetail(reasonDetail)
                .preferred(preferred)
                .epistle(epistle)
                .gospel(gospel)
                .alternativeReadings(alternative)
                .lang(lang)
                .build();
    }
}
