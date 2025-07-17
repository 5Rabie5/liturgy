package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.model.MarriageBanReason;
import at.antiochorthodox.liturgy.repository.MarriageBanReasonRepository;
import at.antiochorthodox.liturgy.service.MarriageAllowedService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/marriage")
public class MarriageAllowedController {

    private final MarriageAllowedService marriageAllowedService;
    private final MarriageBanReasonRepository banReasonRepository;

    public MarriageAllowedController(
            MarriageAllowedService marriageAllowedService,
            MarriageBanReasonRepository banReasonRepository
    ) {
        this.marriageAllowedService = marriageAllowedService;
        this.banReasonRepository = banReasonRepository;
    }

    /**
     * GET: هل الزواج مسموح في تاريخ معين
     */
    @GetMapping("/allowed")
    public MarriageAllowedResponse isMarriageAllowed(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "ar") String lang
    ) {
        return marriageAllowedService.isMarriageAllowed(date, lang);
    }

    /**
     * POST: إدخال رسالة جديدة لمنع الزواج
     * يمكن إرسال واحد أو أكثر من الرسائل دفعة واحدة
     */
    @PostMapping("/reasons")
    public List<MarriageBanReason> addMarriageBanReasons(@RequestBody List<MarriageBanReason> reasons) {
        return banReasonRepository.saveAll(reasons);
    }
}
