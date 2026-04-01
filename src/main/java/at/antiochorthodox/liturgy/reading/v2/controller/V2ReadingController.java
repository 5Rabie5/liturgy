package at.antiochorthodox.liturgy.reading.v2.controller;

import at.antiochorthodox.liturgy.reading.v2.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.reading.v2.service.V2ReadingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class V2ReadingController {

    private static final String DEFAULT_LANG = "ar";
    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final V2ReadingQueryService v2ReadingQueryService;

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
        List<ServiceReadingsDto> services =
                v2ReadingQueryService.getByDate(date, lang, tradition);

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
        List<ServiceReadingsDto> services =
                v2ReadingQueryService.getByDateAndSlot(date, slot, lang, tradition);

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
        List<ServiceReadingsDto> services =
                v2ReadingQueryService.getByDateAndService(date, serviceKey, lang, tradition);

        if (services.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(services);
    }
}
