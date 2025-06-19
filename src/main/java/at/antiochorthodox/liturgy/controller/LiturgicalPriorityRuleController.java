package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalPriorityRule;
import at.antiochorthodox.liturgy.service.LiturgicalPriorityRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/priority-rules")
public class LiturgicalPriorityRuleController {

    private final LiturgicalPriorityRuleService ruleService;

    @Autowired
    public LiturgicalPriorityRuleController(LiturgicalPriorityRuleService ruleService) {
        this.ruleService = ruleService;
    }

    // جلب جميع القواعد
    @GetMapping
    public List<LiturgicalPriorityRule> getAll() {
        return ruleService.findAll();
    }

    // جلب قاعدة حسب ID
    @GetMapping("/{id}")
    public ResponseEntity<LiturgicalPriorityRule> getById(@PathVariable String id) {
        return ruleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // جلب القواعد حسب مناسبتين أو أكثر (تقاطع)
    @GetMapping("/by-occasions")
    public List<LiturgicalPriorityRule> getByOccasionTypes(
            @RequestParam List<String> occasionTypes) {
        return ruleService.findByOccasionTypes(occasionTypes);
    }

    // جلب كل القواعد التي تتعلق بمناسبة معينة
    @GetMapping("/by-occasion")
    public List<LiturgicalPriorityRule> getByOccasionType(
            @RequestParam String occasionType) {
        return ruleService.findByOccasionType(occasionType);
    }

    // البحث في الوصف
    @GetMapping("/search")
    public List<LiturgicalPriorityRule> searchByDescription(
            @RequestParam String text) {
        return ruleService.searchByDescription(text);
    }

    // إضافة أو تعديل قاعدة
    @PostMapping
    public LiturgicalPriorityRule save(@RequestBody LiturgicalPriorityRule rule) {
        return ruleService.save(rule);
    }

    // حذف قاعدة
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        ruleService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
