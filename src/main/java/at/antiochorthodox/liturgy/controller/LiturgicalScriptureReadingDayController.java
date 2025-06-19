package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalScriptureReadingDay;
import at.antiochorthodox.liturgy.service.LiturgicalScriptureReadingDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scripture-readings")
public class LiturgicalScriptureReadingDayController {

    private final LiturgicalScriptureReadingDayService scriptureReadingDayService;

    @Autowired
    public LiturgicalScriptureReadingDayController(LiturgicalScriptureReadingDayService scriptureReadingDayService) {
        this.scriptureReadingDayService = scriptureReadingDayService;
    }

    // Get all readings
    @GetMapping
    public List<LiturgicalScriptureReadingDay> getAll() {
        return scriptureReadingDayService.findAll();
    }

    // Get readings by date
    @GetMapping("/by-date")
    public ResponseEntity<LiturgicalScriptureReadingDay> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<LiturgicalScriptureReadingDay> result = scriptureReadingDayService.findByDate(date);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Get readings by season
    @GetMapping("/by-season")
    public List<LiturgicalScriptureReadingDay> getBySeason(@RequestParam String season) {
        return scriptureReadingDayService.findBySeason(season);
    }

    // Save or update (admin only!)
    @PostMapping
    public LiturgicalScriptureReadingDay save(@RequestBody LiturgicalScriptureReadingDay day) {
        return scriptureReadingDayService.save(day);
    }

    // Delete by ID (admin only!)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        scriptureReadingDayService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
