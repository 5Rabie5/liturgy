package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.ScriptureReading;
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

    public List<ScriptureReading> getAllReadingsForDay(LocalDate date, String lang) {
        List<String> names = new ArrayList<>();

        // 1. الاسم الليتورجي
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        LocalDate nextPhariseePublican = paschaDateCalculator.getNextPhariseePublicanSunday(pascha);
        String liturgicalName = liturgicalWeekService.findLiturgicalNameForDate(date, pascha, nextPhariseePublican, lang);
        if (liturgicalName != null) names.add(liturgicalName);

        // 2. الأعياد
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
        if (fixedFeast != null) names.add(fixedFeast);
        if (movableFeast != null) names.add(movableFeast);

        // 3. القديسون
        List<String> saints = saintService.findNamesByLangAndDate(lang, date);
        if (saints != null) {
            names.addAll(saints.stream().map(name -> "عيد القديس " + name).toList());
        }

        // 4. جلب القراءات
        List<ScriptureReading> all = new ArrayList<>();
        for (String name : names) {
            all.addAll(scriptureReadingService.getReadingsByLiturgicalName(name, lang));
        }

        return all;
    }
}
