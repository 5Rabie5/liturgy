package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.Fasting;
import at.antiochorthodox.liturgy.service.FastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fasting")
@RequiredArgsConstructor
public class FastingController {

    private final FastingService fastingService;

    @GetMapping("/{id}")
    public Optional<Fasting> getById(@PathVariable String id) {
        return fastingService.getById(id);
    }

    @GetMapping("/lang/{lang}")
    public List<Fasting> getByLang(@PathVariable String lang) {
        return fastingService.getByLanguage(lang);
    }

    @GetMapping("/lang/{lang}/type/{type}")
    public List<Fasting> getByLangAndType(@PathVariable String lang, @PathVariable String type) {
        return fastingService.getByLanguageAndType(lang, type);
    }

    @GetMapping("/lang/{lang}/weekly")
    public List<Fasting> getWeekly(@PathVariable String lang) {
        return fastingService.getWeeklyFasting(lang);
    }

    @GetMapping("/lang/{lang}/year/{year}")
    public List<Fasting> getByLangAndYear(@PathVariable String lang, @PathVariable int year) {
        return fastingService.getAllFastingForYear(lang, year);
    }

    @PostMapping
    public Fasting save(@RequestBody Fasting fasting) {
        return fastingService.save(fasting);
    }

    @PostMapping("/bulk")
    public List<Fasting> saveAll(@RequestBody List<Fasting> fastingList) {
        return fastingService.saveAll(fastingList);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        fastingService.deleteById(id);
    }
    @GetMapping("/lang/{lang}/date/{date}")
    public Optional<Fasting> getFastingForDate(
            @PathVariable String lang,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return fastingService.getFastingForDate(lang, date);
    }
}
