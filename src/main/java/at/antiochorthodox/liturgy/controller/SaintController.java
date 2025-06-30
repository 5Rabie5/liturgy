package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.Saint;
import at.antiochorthodox.liturgy.service.SaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/saints")
@RequiredArgsConstructor
public class SaintController {

    private final SaintService saintService;

    @GetMapping("/{id}")
    public Optional<Saint> getById(@PathVariable String id) {
        return saintService.getById(id);
    }

    @GetMapping("/lang/{lang}")
    public List<Saint> getByLanguage(@PathVariable String lang) {
        return saintService.getByLanguage(lang);
    }

    @GetMapping("/lang/{lang}/title/{title}")
    public List<Saint> getByTitle(@PathVariable String lang, @PathVariable String title) {
        return saintService.getByLanguageAndTitle(lang, title);
    }

    @GetMapping("/lang/{lang}/search")
    public List<Saint> searchByName(@PathVariable String lang, @RequestParam String q) {
        return saintService.getByLanguageAndName(lang, q);
    }

    // بحث باليوم والشهر (مثال: /lang/ar/month/1/day/1)
    @GetMapping("/lang/{lang}/month/{month}/day/{day}")
    public List<Saint> getByMonthDay(@PathVariable String lang, @PathVariable int month, @PathVariable int day) {
        return saintService.getByLanguageAndFeastDate(lang, month, day);
    }

    // بحث بتاريخ (LocalDate) (مثال: /lang/ar/date/2025-07-01)
    @GetMapping("/lang/{lang}/date/{date}")
    public List<Saint> getByFeastDate(@PathVariable String lang,
                                      @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return saintService.getByLanguageAndFeastDate(lang, date);
    }

    // بحث بكل شهر (مثال: /lang/ar/month/1)
    @GetMapping("/lang/{lang}/month/{month}")
    public List<Saint> getByMonth(@PathVariable String lang, @PathVariable int month) {
        return saintService.getByLanguageAndFeastMonth(lang, month);
    }

    @PostMapping
    public Saint createSaint(@RequestBody Saint saint) {
        return saintService.save(saint);
    }

    @PostMapping("/bulk")
    public List<Saint> createSaints(@RequestBody List<Saint> saints) {
        return saintService.saveAll(saints);
    }

    @DeleteMapping("/{id}")
    public void deleteSaint(@PathVariable String id) {
        saintService.deleteById(id);
    }
    @GetMapping("/lang/{lang}/debug/all")
    public List<Saint> debugAll(@PathVariable String lang) {

        return saintService.getByLanguage(lang);
    }
}
