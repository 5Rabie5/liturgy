package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.service.MarriageAllowedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/marriage")
public class MarriageAllowedController {

    private final MarriageAllowedService marriageAllowedService;

    @Autowired
    public MarriageAllowedController(MarriageAllowedService marriageAllowedService) {
        this.marriageAllowedService = marriageAllowedService;
    }

    @GetMapping("/allowed")
    public ResponseEntity<MarriageAllowedResponse> isMarriageAllowed(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        MarriageAllowedResponse response = marriageAllowedService.isMarriageAllowed(date);
        return ResponseEntity.ok(response);
    }
}
