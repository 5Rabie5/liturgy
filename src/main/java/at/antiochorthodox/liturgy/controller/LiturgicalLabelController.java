package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import at.antiochorthodox.liturgy.service.LiturgicalLabelService;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.format.annotation.DateTimeFormat;
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

    @PostMapping
    public LiturgicalLabel saveLabel(@RequestBody LiturgicalLabel label) {
        return liturgicalLabelService.saveLabel(label);
    }

    @DeleteMapping("/{id}")
    public void deleteLabel(@PathVariable String id) {
        liturgicalLabelService.deleteLabel(id);
    }

//    @GetMapping("/day-label")
//    public String getLabelForDate(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//            @RequestParam(defaultValue = "ar") String lang
//    ) {
//        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
//        LocalDate nextPhariseePublican = paschaDateCalculator.getNextPhariseePublicanSunday(pascha);
//        return liturgicalLabelService.getLabelForDate(date, pascha, nextPhariseePublican, lang);
//    }
    @GetMapping("/day-label")
    public String getLiturgicalLabelForDate(
            @RequestParam String date,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        LocalDate localDate = LocalDate.parse(date);
        LocalDate pascha = paschaDateCalculator.getPaschaDate(localDate.getYear());
        LocalDate nextPharisee = paschaDateCalculator.getNextPhariseePublicanSunday(pascha);
        return liturgicalLabelService.getLabelForDate(localDate, pascha, lang);
    }
}
