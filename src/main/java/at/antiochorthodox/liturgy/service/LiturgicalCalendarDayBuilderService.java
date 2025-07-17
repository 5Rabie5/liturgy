package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.model.ScriptureReadingOption;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;
import at.antiochorthodox.liturgy.model.ScriptureReadingOption;

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
            MarriageAllowedService marriageAllowedService
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

        String liturgicalName = liturgicalLabelService.getLabelForDate(date, pascha, lang);
        List<String> saints = saintService.findNamesByLangAndDate(lang, date);
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
        String fastingLevel = fastingService.getFastingEvelByLangAndDate(lang, date);
        MarriageAllowedResponse marriageInfo = marriageAllowedService.isMarriageAllowed(date, lang);

        List<ScriptureReadingOption> readingOptions = scriptureReadingResolverService.getAllReadingOptionsForDay(date, lang);

        return LiturgicalCalendarDay.builder()
                .date(date)
                .liturgicalName(liturgicalName)
                .saints(saints)
                .fixedFeast(fixedFeast)
                .movableFeast(movableFeast)
                .fastingLevel(fastingLevel)
                .lang(lang)
                .marriageAllowed(marriageInfo.isAllowed())
                .marriageNote(marriageInfo.getMessage())
                .readingOptions(readingOptions)
                .build();
    }
}
