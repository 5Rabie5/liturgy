package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LiturgicalCalendarDayBuilderService {

    private final ScriptureReadingResolverService scriptureReadingResolverService;
    private final SaintService saintService;
    private final FeastService feastService;
    private final FastingService fastingService;
    private final LiturgicalWeekService liturgicalWeekService;
    private final PaschaDateCalculator paschaDateCalculator;

    public LiturgicalCalendarDayBuilderService(
            ScriptureReadingResolverService scriptureReadingResolverService,
            SaintService saintService,
            FeastService feastService,
            FastingService fastingService,
            LiturgicalWeekService liturgicalWeekService,
            PaschaDateCalculator paschaDateCalculator
    ) {
        this.scriptureReadingResolverService = scriptureReadingResolverService;
        this.saintService = saintService;
        this.feastService = feastService;
        this.fastingService = fastingService;
        this.liturgicalWeekService = liturgicalWeekService;
        this.paschaDateCalculator = paschaDateCalculator;
    }

    public LiturgicalCalendarDay buildLiturgicalDay(LocalDate date, String lang) {
        // 1. حساب الاسم الليتورجي
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        LocalDate nextPhariseePublican = paschaDateCalculator.getNextPhariseePublicanSunday(pascha);
        String liturgicalName = liturgicalWeekService.findLiturgicalNameForDate(date, pascha, nextPhariseePublican, lang);

        // 2. جلب القراءات من الخدمة الجديدة (الخالية من الاعتمادية الدائرية)
        List<ScriptureReading> readings = scriptureReadingResolverService.getAllReadingsForDay(date, lang);

        String gospelReading = readings.stream()
                .filter(r -> r.getType().equalsIgnoreCase("gospel"))
                .map(ScriptureReading::getReference)
                .findFirst().orElse(null);

        String epistleReading = readings.stream()
                .filter(r -> r.getType().equalsIgnoreCase("epistle"))
                .map(ScriptureReading::getReference)
                .findFirst().orElse(null);

        List<String> alternativeReadings = readings.stream()
                .filter(r -> !r.getType().equalsIgnoreCase("epistle") && !r.getType().equalsIgnoreCase("gospel"))
                .map(ScriptureReading::getReference)
                .toList();

        // 3. القديسون
        List<String> saints = saintService.findNamesByLangAndDate(lang, date);

        // 4. الأعياد
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);

        // 5. الصوم
        String fastingLevel = fastingService.getFastingEvelByLangAndDate(lang, date);

        // 6. بناء كائن اليوم الليتورجي
        return LiturgicalCalendarDay.builder()
                .date(date)
                .liturgicalName(liturgicalName)
                .saints(saints)
                .gospelReading(gospelReading)
                .epistleReading(epistleReading)
                .alternativeReadings(alternativeReadings)
                .fixedFeast(fixedFeast)
                .movableFeast(movableFeast)
                .fastingLevel(fastingLevel)
                .lang(lang)
                .build();
    }
}
