package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarReadings;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LiturgicalCalendarDayBuilderService {

    private final SaintService saintService;
    private final FeastService feastService;
    private final FastingService fastingService;
    private final MarriageAllowedService marriageAllowedService;
    private final LiturgicalDayReadingsService liturgicalDayReadingsService;
    private final LiturgicalDayContextService liturgicalDayContextService;

    public LiturgicalCalendarDayBuilderService(
            SaintService saintService,
            FeastService feastService,
            FastingService fastingService,
            MarriageAllowedService marriageAllowedService,
            LiturgicalDayReadingsService liturgicalDayReadingsService,
            LiturgicalDayContextService liturgicalDayContextService
    ) {
        this.saintService = saintService;
        this.feastService = feastService;
        this.fastingService = fastingService;
        this.marriageAllowedService = marriageAllowedService;
        this.liturgicalDayReadingsService = liturgicalDayReadingsService;
        this.liturgicalDayContextService = liturgicalDayContextService;
    }

    public LiturgicalCalendarDay buildLiturgicalDay(LocalDate date, String lang) {
        LiturgicalDayContext context = liturgicalDayContextService.resolveForDate(date, lang);

        List<String> saints = saintService.findNamesByLangAndDate(lang, date);
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
        String fastingLevel = fastingService.getFastingEvelByLangAndDate(lang, date);
        MarriageAllowedResponse marriageInfo = marriageAllowedService.isMarriageAllowed(date, lang);

        LiturgicalCalendarReadings grouped =
                liturgicalDayReadingsService.buildGroupedReadings(context, fixedFeast, movableFeast, saints, lang);

        return LiturgicalCalendarDay.builder()
                .date(date)
                .liturgicalName(context != null ? context.getDayLabel() : null)
                .liturgicalDayKey(context != null ? context.getDayKey() : null)
                .epistleKey(context != null ? context.getEpistleKey() : null)
                .gospelKey(context != null ? context.getGospelKey() : null)
                .saints(saints)
                .fixedFeast(fixedFeast)
                .movableFeast(movableFeast)
                .fastingLevel(fastingLevel)
                .lang(lang)
                .marriageAllowed(marriageInfo.isAllowed())
                .marriageNote(marriageInfo.getMessage())
                .readings(grouped)
                .build();
    }
}
