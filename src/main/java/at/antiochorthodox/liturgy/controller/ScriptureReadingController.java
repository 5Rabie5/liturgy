package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.model.ScriptureReadingOption;
import at.antiochorthodox.liturgy.service.ScriptureReadingResolverService;
import at.antiochorthodox.liturgy.service.ScriptureReadingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/scripture-readings")
public class ScriptureReadingController {

    private final ScriptureReadingService scriptureReadingService;
    private final ScriptureReadingResolverService scriptureReadingResolverService;

    public ScriptureReadingController(
            ScriptureReadingService scriptureReadingService,
            ScriptureReadingResolverService scriptureReadingResolverService
    ) {
        this.scriptureReadingService = scriptureReadingService;
        this.scriptureReadingResolverService = scriptureReadingResolverService;
    }

    @GetMapping("/by-date-and-type")
    public ResponseEntity<List<ScriptureReading>> getReadingsByDateAndType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String type,
            @RequestParam(defaultValue = "ar") String lang) {
        List<ScriptureReading> readings = scriptureReadingService.getReadingsByDateAndType(date, type, lang);
        if (readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/grouped")
    public ResponseEntity<List<ScriptureReadingOption>> getGroupedScriptureReadings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ar") String lang) {
        List<ScriptureReadingOption> options = scriptureReadingResolverService.getAllReadingOptionsForDay(date, lang);
        if (options.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(options);
    }
    @GetMapping("/by-liturgical-name")
    public ResponseEntity<List<ScriptureReading>> getReadingsByLiturgicalName(
            @RequestParam String liturgicalName,
            @RequestParam(defaultValue = "gospel") String type,
            @RequestParam(defaultValue = "ar") String lang,
             @RequestParam String reasonDetail ) {
        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByLiturgicalName(liturgicalName, type, lang, reasonDetail);
        if (readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }
    // لحفظ قراءة واحدة
    @PostMapping
    public ResponseEntity<ScriptureReading> saveScriptureReading(
            @RequestBody ScriptureReading reading
    ) {
        ScriptureReading saved = scriptureReadingService.saveReading(reading);
        return ResponseEntity.ok(saved);
    }

    // لحفظ مجموعة من القراءات دفعة واحدة
    @PostMapping("/batch")
    public ResponseEntity<List<ScriptureReading>> saveScriptureReadingsBatch(
            @RequestBody List<ScriptureReading> readings
    ) {
        List<ScriptureReading> saved = scriptureReadingService.saveReadings(readings);
        return ResponseEntity.ok(saved);
    }
}
