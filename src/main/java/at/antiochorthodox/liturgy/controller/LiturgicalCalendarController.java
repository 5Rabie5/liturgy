package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.service.LiturgicalCalendarDayBuilderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class LiturgicalCalendarController {

    private final LiturgicalCalendarDayBuilderService builderService;

    @Autowired
    public LiturgicalCalendarController(LiturgicalCalendarDayBuilderService builderService) {
        this.builderService = builderService;
    }

    @GetMapping("/day")
    public LiturgicalCalendarDay getLiturgicalDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        return builderService.buildLiturgicalDay(date, lang);
    }
    @GetMapping("/days")
    public List<LiturgicalCalendarDay> getLiturgicalDays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        return start.datesUntil(end.plusDays(1))
                .map(date -> builderService.buildLiturgicalDay(date, lang))
                .toList();
    }
    @GetMapping("/year")
    public List<LiturgicalCalendarDay> getLiturgicalYear(
            @RequestParam int year,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        return start.datesUntil(end.plusDays(1))
                .map(date -> builderService.buildLiturgicalDay(date, lang))
                .toList();
    }

}
