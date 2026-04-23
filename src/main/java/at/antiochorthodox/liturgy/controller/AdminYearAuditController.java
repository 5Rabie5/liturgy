package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.dto.YearAuditReport;
import at.antiochorthodox.liturgy.service.YearAuditServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/readings")
@RequiredArgsConstructor
public class AdminYearAuditController {

    private static final String DEFAULT_LANG = "ar";

    private final YearAuditServiceImpl yearAuditService;

    @GetMapping("/year-audit")
    public ResponseEntity<YearAuditReport> auditYear(
            @RequestParam int year,
            @RequestParam(defaultValue = DEFAULT_LANG) String lang
    ) {
        return ResponseEntity.ok(yearAuditService.auditYear(year, lang));
    }
}
