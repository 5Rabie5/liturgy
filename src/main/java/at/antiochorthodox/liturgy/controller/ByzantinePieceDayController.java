package at.antiochorthodox.liturgy.controller;

import at.antiochorthodox.liturgy.model.LiturgicalByzantinePieceDay;
import at.antiochorthodox.liturgy.service.ByzantinePieceDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/byzantine-pieces")
public class ByzantinePieceDayController {

    private final ByzantinePieceDayService byzantinePieceDayService;

    @Autowired
    public ByzantinePieceDayController(ByzantinePieceDayService byzantinePieceDayService) {
        this.byzantinePieceDayService = byzantinePieceDayService;
    }

    // جلب كل الأيام
    @GetMapping
    public List<LiturgicalByzantinePieceDay> getAll() {
        return byzantinePieceDayService.findAll();
    }

    // جلب يوم محدد حسب التاريخ
    @GetMapping("/by-date")
    public ResponseEntity<LiturgicalByzantinePieceDay> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<LiturgicalByzantinePieceDay> result = byzantinePieceDayService.findByDate(date);
        return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // جلب أيام حسب الموسم
    @GetMapping("/by-season")
    public List<LiturgicalByzantinePieceDay> getBySeason(@RequestParam String season) {
        return byzantinePieceDayService.findBySeason(season);
    }

    // إضافة أو تعديل يوم
    @PostMapping
    public LiturgicalByzantinePieceDay save(@RequestBody LiturgicalByzantinePieceDay day) {
        return byzantinePieceDayService.save(day);
    }

    // حذف يوم
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        byzantinePieceDayService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
