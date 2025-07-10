package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import at.antiochorthodox.liturgy.service.LiturgicalLabelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labels")
public class LiturgicalLabelController {

    private final LiturgicalLabelService liturgicalLabelService;

    // Constructor injection (no @Autowired needed here)
    public LiturgicalLabelController(LiturgicalLabelService liturgicalLabelService) {
        this.liturgicalLabelService = liturgicalLabelService;
    }

    @GetMapping
    public List<LiturgicalLabel> getAllLabels(@RequestParam(required = false) String lang) {
        if (lang != null) {
            return liturgicalLabelService.getLabelsByLang(lang);
        }
        return liturgicalLabelService.getAllLabels();
    }

    @GetMapping("/lookup")
    public LiturgicalLabel getLabelByKeyAndLang(@RequestParam String labelKey, @RequestParam String lang) {
        return liturgicalLabelService.getLabelByKeyAndLang(labelKey, lang);
    }

    @PostMapping
    public LiturgicalLabel saveLabel(@RequestBody LiturgicalLabel label) {
        return liturgicalLabelService.saveLabel(label);
    }

    @DeleteMapping("/{id}")
    public void deleteLabel(@PathVariable String id) {
        liturgicalLabelService.deleteLabel(id);
    }
}
