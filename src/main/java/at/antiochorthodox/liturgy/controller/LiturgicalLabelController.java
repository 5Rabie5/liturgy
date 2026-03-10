package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import at.antiochorthodox.liturgy.service.LiturgicalLabelService;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/labels")
public class LiturgicalLabelController {

    private final LiturgicalLabelService liturgicalLabelService;
    private final PaschaDateCalculator paschaDateCalculator;

    public LiturgicalLabelController(LiturgicalLabelService liturgicalLabelService,
                                     PaschaDateCalculator paschaDateCalculator) {
        this.liturgicalLabelService = liturgicalLabelService;
        this.paschaDateCalculator = paschaDateCalculator;
    }

    @GetMapping
    public List<LiturgicalLabel> getAllLabels(@RequestParam(required = false) String lang) {
        if (lang != null) {
            return liturgicalLabelService.getLabelsByLang(lang);
        }
        return liturgicalLabelService.getAllLabels();
    }

    @GetMapping("/lookup")
    public LiturgicalLabel getByKeyAndLang(@RequestParam String labelKey, @RequestParam String lang) {
        return liturgicalLabelService.getLabelByKeyAndLang(labelKey, lang);
    }

    @GetMapping("/lookup-day-key")
    public LiturgicalLabel getByDayKeyAndLang(@RequestParam String dayKey, @RequestParam String lang) {
        return liturgicalLabelService.getLabelByDayKeyAndLang(dayKey, lang);
    }

    @PostMapping
    public LiturgicalLabel saveLabel(@RequestBody LiturgicalLabel label) {
        return liturgicalLabelService.saveLabel(label);
    }

    @DeleteMapping("/{id}")
    public void deleteLabel(@PathVariable String id) {
        liturgicalLabelService.deleteLabel(id);
    }

    @GetMapping("/day-label")
    public String getLiturgicalLabelForDate(
            @RequestParam String date,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDate pascha = paschaDateCalculator.getPaschaDate(localDate.getYear());
        return liturgicalLabelService.getLabelForDate(localDate, pascha, lang);
    }

    @GetMapping("/day-key")
    public String getLiturgicalDayKeyForDate(
            @RequestParam String date,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDate pascha = paschaDateCalculator.getPaschaDate(localDate.getYear());
        return liturgicalLabelService.getDayKeyForDate(localDate, pascha, lang);
    }
}
