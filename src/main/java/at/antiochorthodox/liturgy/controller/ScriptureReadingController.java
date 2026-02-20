package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalCalendarReadings;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.service.LiturgicalDayReadingsService;
import at.antiochorthodox.liturgy.service.ScriptureReadingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class ScriptureReadingController {

    private final ScriptureReadingService scriptureReadingService;
    private final LiturgicalDayReadingsService liturgicalDayReadingsService;

    public ScriptureReadingController(
            ScriptureReadingService scriptureReadingService,
            LiturgicalDayReadingsService liturgicalDayReadingsService
    ) {
        this.scriptureReadingService = scriptureReadingService;
        this.liturgicalDayReadingsService = liturgicalDayReadingsService; // ✅ مهم
    }

    @GetMapping("/by-date-and-type")
    public ResponseEntity<List<ScriptureReading>> getReadingsByDateAndType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "any") String type,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        List<ScriptureReading> readings = scriptureReadingService.getReadingsByDateAndType(date, type, lang);
        if (readings.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/reading-day")
    public ResponseEntity<LiturgicalCalendarReadings> getReadingDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        LiturgicalCalendarReadings readings =
                liturgicalDayReadingsService.buildGroupedReadingsForDate(date, lang);

        if (readings == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/by-liturgicalName")
    public ResponseEntity<List<ScriptureReading>> getReadingsByLiturgicalName(
            @RequestParam String liturgicalName,
            @RequestParam(defaultValue = "any") String type,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByLiturgicalName(liturgicalName, type, lang);

        if (readings.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(readings);
    }

    @PostMapping
    public ResponseEntity<ScriptureReading> saveScriptureReading(@RequestBody ScriptureReading reading) {
        ScriptureReading saved = scriptureReadingService.saveReading(reading);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ScriptureReading>> saveScriptureReadingsBatch(@RequestBody List<ScriptureReading> readings) {
        List<ScriptureReading> saved = scriptureReadingService.saveReadings(readings);
        return ResponseEntity.ok(saved);
    }
}