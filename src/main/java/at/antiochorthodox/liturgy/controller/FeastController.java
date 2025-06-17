package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.Feast;
import at.antiochorthodox.liturgy.service.FeastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/feasts")
@RequiredArgsConstructor
public class FeastController {

    private final FeastService feastService;

    @GetMapping("/{id}")
    public Optional<Feast> getById(@PathVariable String id) {
        return feastService.getById(id);
    }

    @GetMapping("/lang/{lang}")
    public List<Feast> getByLanguage(@PathVariable String lang) {
        return feastService.getByLanguage(lang);
    }

    @GetMapping("/lang/{lang}/date/{date}")
    public List<Feast> getByDate(@PathVariable String lang, @PathVariable String date) {
        return feastService.getByLanguageAndDate(lang, date);
    }

    @GetMapping("/lang/{lang}/group/{group}")
    public List<Feast> getByGroup(@PathVariable String lang, @PathVariable String group) {
        return feastService.getByLanguageAndGroup(lang, group);
    }

    @GetMapping("/lang/{lang}/type/{type}")
    public List<Feast> getByType(@PathVariable String lang, @PathVariable String type) {
        return feastService.getByLanguageAndType(lang, type);
    }

    @GetMapping("/lang/{lang}/search")
    public List<Feast> searchByName(@PathVariable String lang, @RequestParam String q) {
        return feastService.getByLanguageAndName(lang, q);
    }

    @PostMapping
    public Feast save(@RequestBody Feast feast) {
        return feastService.save(feast);
    }

    @PostMapping("/bulk")
    public List<Feast> saveAll(@RequestBody List<Feast> feasts) {
        return feastService.saveAll(feasts);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        feastService.deleteById(id);
    }
}
