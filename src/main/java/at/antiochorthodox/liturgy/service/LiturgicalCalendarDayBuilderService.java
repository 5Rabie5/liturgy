package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
@Service
public class LiturgicalCalendarDayBuilderService {

    private final ScriptureReadingResolverService scriptureReadingResolverService;
    private final SaintService saintService;
    private final FeastService feastService;
    private final FastingService fastingService;
    private final LiturgicalLabelService liturgicalLabelService;
    private final PaschaDateCalculator paschaDateCalculator;
    private final MarriageAllowedService marriageAllowedService;

    public LiturgicalCalendarDayBuilderService(
            ScriptureReadingResolverService scriptureReadingResolverService,
            SaintService saintService,
            FeastService feastService,
            FastingService fastingService,
            LiturgicalLabelService liturgicalLabelService,
            PaschaDateCalculator paschaDateCalculator,
            MarriageAllowedService marriageAllowedService // ✅ Injection هنا
    ) {
        this.scriptureReadingResolverService = scriptureReadingResolverService;
        this.saintService = saintService;
        this.feastService = feastService;
        this.fastingService = fastingService;
        this.liturgicalLabelService = liturgicalLabelService;
        this.paschaDateCalculator = paschaDateCalculator;
        this.marriageAllowedService = marriageAllowedService;
    }

    public LiturgicalCalendarDay buildLiturgicalDay(LocalDate date, String lang) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        LocalDate previousPascha = paschaDateCalculator.getPaschaDate(date.getYear() - 1);
        LocalDate pentecost = pascha.plusDays(49);
        LocalDate nextPhariseePublican = paschaDateCalculator.getNextPhariseePublicanSunday(pascha);

        String liturgicalName = liturgicalLabelService.getLabelForDate(date, pascha, lang);

        List<ScriptureReading> readings = scriptureReadingResolverService.getAllReadingsForDay(date, lang);
        String gospelReading = readings.stream()
                .filter(r -> r.getType().equalsIgnoreCase("gospel"))
                .map(ScriptureReading::getReference)
                .findFirst()
                .orElse(null);

        String epistleReading = readings.stream()
                .filter(r -> r.getType().equalsIgnoreCase("epistle"))
                .map(ScriptureReading::getReference)
                .findFirst()
                .orElse(null);

        List<String> alternativeReadings = readings.stream()
                .filter(r -> !r.getType().equalsIgnoreCase("epistle") && !r.getType().equalsIgnoreCase("gospel"))
                .map(ScriptureReading::getReference)
                .toList();

        List<String> saints = saintService.findNamesByLangAndDate(lang, date);
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
        String fastingLevel = fastingService.getFastingEvelByLangAndDate(lang, date);

        // ✅ إضافة معلومة الزواج
        MarriageAllowedResponse marriageInfo = marriageAllowedService.isMarriageAllowed(date, lang);

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
                .marriageAllowed(marriageInfo.isAllowed())
                .marriageNote(marriageInfo.getMessage())
                .build();
    }
}


