package at.antiochorthodox.liturgy.reading.v2.controller;

import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.reading.v2.service.ReadingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Canonical controller surface for the reading-assignment flow.
 *
 * <p>Use these endpoints for new development. Legacy v1 endpoints remain in
 * {@code ScriptureReadingController} for backward compatibility only.</p>
 */
@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class V2ReadingController {

    private static final String DEFAULT_LANG = "ar";
    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final ReadingQueryService readingQueryService;

    @GetMapping("/by-date-v2")
    public ResponseEntity<List<ServiceReadingsDto>> getByDateV2(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang,
            @RequestParam(defaultValue = DEFAULT_TRADITION) String tradition
    ) {
        List<ServiceReadingsDto> services =
                readingQueryService.getByDate(date, lang, tradition);

        if (services.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(services);
    }

    @GetMapping("/by-date-v2/slot")
    public ResponseEntity<List<ServiceReadingsDto>> getByDateV2AndSlot(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String slot,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang,
            @RequestParam(defaultValue = DEFAULT_TRADITION) String tradition
    ) {
        List<ServiceReadingsDto> services =
                readingQueryService.getByDateAndSlot(date, slot, lang, tradition);

        if (services.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(services);
    }

    @GetMapping("/service")
    public ResponseEntity<List<ServiceReadingsDto>> getService(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String serviceKey,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang,
            @RequestParam(defaultValue = DEFAULT_TRADITION) String tradition
    ) {
        List<ServiceReadingsDto> services =
                readingQueryService.getByDateAndService(date, serviceKey, lang, tradition);

        if (services.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(services);
    }
}
