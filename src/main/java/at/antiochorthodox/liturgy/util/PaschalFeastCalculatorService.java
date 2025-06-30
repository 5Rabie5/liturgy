package at.antiochorthodox.liturgy.util;

import at.antiochorthodox.liturgy.model.Feast;
import at.antiochorthodox.liturgy.repository.FeastRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaschalFeastCalculatorService {

    private final FeastRepository feastRepository;
    private final PaschaDateCalculator paschaCalculator; // هذا يجب أن يحسب تاريخ الفصح

    /**
     * يرجع قائمة بالأعياد مع تواريخها في السنة المطلوبة.
     */
    public List<FeastWithDate> getPaschalFeastsWithDates(int year, String lang) {
        List<Feast> feasts = feastRepository.findByTypeAndLang("paschal", lang);
        LocalDate paschaDate = paschaCalculator.getPaschaDate(year);

        List<FeastWithDate> result = new ArrayList<>();
        for (Feast feast : feasts) {
            LocalDate feastDate = calculateFeastDate(feast, year, paschaDate);
            if (feastDate != null) {
                result.add(new FeastWithDate(feast, feastDate));
            }
        }
        return result;
    }

    /**
     * يحسب تاريخ العيد المتنقل بناء على قاعدة calculationRule.
     */
    public LocalDate calculateFeastDate(Feast feast, int year, LocalDate paschaDate) {
        String rule = feast.getCalculationRule();
        if (rule == null || rule.isEmpty()) {
            // إذا كان هناك offsetFromPascha معرف مباشرة
            if (feast.getOffsetFromPascha() != null) {
                return paschaDate.plusDays(feast.getOffsetFromPascha());
            }
            return null;
        }

        if (rule.startsWith("offsetFromPascha:")) {
            int offset = Integer.parseInt(rule.replace("offsetFromPascha:", "").trim());
            return paschaDate.plusDays(offset);
        }

        // قواعد خاصة (الأحد بين أيام معينة من شهر معين)
        if (rule.startsWith("between_")) {
            // مثال: between_10_11_17 يعني: الأحد بين 11-17 من شهر 10
            String[] parts = rule.split("_");
            int month = Integer.parseInt(parts[1]);
            int dayStart = Integer.parseInt(parts[2]);
            int dayEnd = Integer.parseInt(parts[3]);
            return findSundayBetween(year, month, dayStart, dayEnd);
        }

        // يمكنك إضافة قواعد أكثر هنا حسب الحاجة.
        return null;
    }

    private LocalDate findSundayBetween(int year, int month, int dayStart, int dayEnd) {
        for (int day = dayStart; day <= dayEnd; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (date.getDayOfWeek().getValue() == 7) { // Sunday = 7
                return date;
            }
        }
        return null;
    }

    // DTO بسيط لعرض العيد مع التاريخ المحسوب
    @Data
    @AllArgsConstructor
    public static class FeastWithDate {
        private Feast feast;
        private LocalDate date;
    }
}
