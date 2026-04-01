package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarReadings;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.service.LiturgicalDayContextService;
import at.antiochorthodox.liturgy.service.LiturgicalDayReadingsService;
import at.antiochorthodox.liturgy.service.LiturgicalReadingAssignmentService;
import at.antiochorthodox.liturgy.service.ScriptureReadingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class ScriptureReadingController {

    private static final String DEFAULT_LANG = "ar";
    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final ScriptureReadingService scriptureReadingService;
    private final LiturgicalDayReadingsService liturgicalDayReadingsService;
    private final LiturgicalDayContextService liturgicalDayContextService;
    private final LiturgicalReadingAssignmentService liturgicalReadingAssignmentService;

    public ScriptureReadingController(
            ScriptureReadingService scriptureReadingService,
            LiturgicalDayReadingsService liturgicalDayReadingsService,
            LiturgicalDayContextService liturgicalDayContextService,
            LiturgicalReadingAssignmentService liturgicalReadingAssignmentService
    ) {
        this.scriptureReadingService = scriptureReadingService;
        this.liturgicalDayReadingsService = liturgicalDayReadingsService;
        this.liturgicalDayContextService = liturgicalDayContextService;
        this.liturgicalReadingAssignmentService = liturgicalReadingAssignmentService;
    }

    // =========================================================
    // Legacy endpoints
    // =========================================================

    @GetMapping("/by-date-and-type")
    public ResponseEntity<List<ScriptureReading>> getReadingsByDateAndType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String slot,
            @RequestParam(defaultValue = "any") String type,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang
    ) {
        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByDateAndType(date, slot, type, lang);

        if (readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/by-day-key")
    public ResponseEntity<List<ScriptureReading>> getReadingsByDayKey(
            @RequestParam String dayKey,
            @RequestParam(required = false) String slot,
            @RequestParam(defaultValue = "any") String type,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang
    ) {
        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByDayKey(dayKey, slot, type, lang);

        if (readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/reading-day")
    public ResponseEntity<LiturgicalCalendarReadings> getReadingDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang
    ) {
        LiturgicalCalendarReadings readings =
                liturgicalDayReadingsService.buildGroupedReadingsForDate(date, lang);

        if (readings == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/by-liturgicalName")
    public ResponseEntity<List<ScriptureReading>> getReadingsByLiturgicalName(
            @RequestParam String liturgicalName,
            @RequestParam(defaultValue = "any") String type,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang
    ) {
        List<ScriptureReading> readings =
                scriptureReadingService.getReadingsByLiturgicalName(liturgicalName, type, lang);

        if (readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    // =========================================================
    // New v2 endpoints based on LiturgicalReadingAssignment
    // =========================================================

    /**
     * Returns all services/readings for the resolved reading day.
     *
     * Example:
     * GET /api/readings/by-date-v2?date=2026-04-09&lang=ar
     */
    @GetMapping("/by-date-v2")
    public ResponseEntity<List<ServiceReadingsDto>> getByDateV2(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang,
            @RequestParam(defaultValue = DEFAULT_TRADITION) String tradition
    ) {
        LiturgicalDayContext context = resolveContext(date, lang, tradition);
        List<ServiceReadingsDto> services =
                liturgicalReadingAssignmentService.getAssignmentsForDay(context);

        if (services.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(services);
    }

    /**
     * Returns all services for one specific slot of the resolved day.
     *
     * Example:
     * GET /api/readings/by-date-v2/slot?date=2026-04-09&slot=vespers&lang=ar
     */
    @GetMapping("/by-date-v2/slot")
    public ResponseEntity<List<ServiceReadingsDto>> getByDateV2AndSlot(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String slot,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang,
            @RequestParam(defaultValue = DEFAULT_TRADITION) String tradition
    ) {
        LiturgicalDayContext context = resolveContext(date, lang, tradition);
        List<ServiceReadingsDto> services =
                liturgicalReadingAssignmentService.getAssignmentsForDayAndSlot(context, slot);

        if (services.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(services);
    }

    /**
     * Returns one specific service of the resolved day.
     *
     * Example:
     * GET /api/readings/service?date=2026-04-09&serviceKey=HOLY_THURSDAY_VESPERS&lang=ar
     */
    @GetMapping("/service")
    public ResponseEntity<List<ServiceReadingsDto>> getService(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String serviceKey,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang,
            @RequestParam(defaultValue = DEFAULT_TRADITION) String tradition
    ) {
        LiturgicalDayContext context = resolveContext(date, lang, tradition);
        List<ServiceReadingsDto> services =
                liturgicalReadingAssignmentService.getAssignmentsForService(context, serviceKey);

        if (services.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(services);
    }

    // =========================================================
    // POST methods
    // =========================================================

    @PostMapping
    public ResponseEntity<ScriptureReading> saveScriptureReading(@RequestBody ScriptureReading reading) {
        ScriptureReading saved = scriptureReadingService.saveReading(reading);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ScriptureReading>> saveScriptureReadingsBatch(
            @RequestBody List<ScriptureReading> readings
    ) {
        List<ScriptureReading> saved = scriptureReadingService.saveReadings(readings);
        return ResponseEntity.ok(saved);
    }

    // =========================================================
    // Helper
    // =========================================================

    private LiturgicalDayContext resolveContext(LocalDate date, String lang, String tradition) {
        LiturgicalDayContext context = liturgicalDayContextService.resolve(date, lang, tradition);

        if (context == null) {
            throw new IllegalStateException("Could not resolve LiturgicalDayContext for date: " + date);
        }

        if (context.getTradition() == null || context.getTradition().isBlank()) {
            context.setTradition(tradition);
        }

        return context;
    }
}