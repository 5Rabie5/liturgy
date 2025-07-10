package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.ScriptureReading;
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

    public ScriptureReadingController(ScriptureReadingService scriptureReadingService) {
        this.scriptureReadingService = scriptureReadingService;
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
}
