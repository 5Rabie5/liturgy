package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.Fasting;
import at.antiochorthodox.liturgy.service.FastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    public List<Fasting> getByLanguage(@PathVariable String lang) {
        return fastingService.getByLanguage(lang);
    }

    @GetMapping("/lang/{lang}/type/{type}")
    public List<Fasting> getByType(@PathVariable String lang, @PathVariable String type) {
        return fastingService.getByLanguageAndType(lang, type);
    }

    @GetMapping("/lang/{lang}/weekly")
    public List<Fasting> getWeekly(@PathVariable String lang) {
        return fastingService.getWeeklyFasting(lang);
    }

    @PostMapping
    public Fasting save(@RequestBody Fasting fasting) {
        return fastingService.save(fasting);
    }

    @PostMapping("/bulk")
    public List<Fasting> saveAll(@RequestBody List<Fasting> list) {
        return fastingService.saveAll(list);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        fastingService.deleteById(id);
    }
}
