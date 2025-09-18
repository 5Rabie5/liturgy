package at.antiochorthodox.liturgy.util;

import at.antiochorthodox.liturgy.model.Feast;
import at.antiochorthodox.liturgy.repository.FeastRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaschalFeastCalculatorService {

    private final FeastRepository feastRepository;
    private final PaschaDateCalculator paschaCalculator; // يحسب تاريخ الفصح

    /**
     * يرجع قائمة بالأعياد المتنقلة (paschal) مع تواريخها في السنة المطلوبة.
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
     * يحسب تاريخ العيد المتنقّل بناءً على قاعدة calculationRule (أو offsetFromPascha).
     */
    public LocalDate calculateFeastDate(Feast feast, int year, LocalDate paschaDate) {
        String rule = feast.getCalculationRule();

        // بدون قاعدة: إن وُجد offsetFromPascha استخدمه
        if (rule == null || rule.isEmpty()) {
            if (feast.getOffsetFromPascha() != null) {
                return paschaDate.plusDays(feast.getOffsetFromPascha());
            }
            return null;
        }

        // قاعدة صريحة على شكل offsetFromPascha:N
        if (rule.startsWith("offsetFromPascha:")) {
            int offset = Integer.parseInt(rule.replace("offsetFromPascha:", "").trim());
            return paschaDate.plusDays(offset);
        }

        // قواعد الأحد ضمن نافذة محددة: between_MM_DD_DD
        // مثال: between_10_11_17 يعني: الأحد بين 11–17 من الشهر 10
        if (rule.startsWith("between_")) {
            String[] parts = rule.split("_");
            int month = Integer.parseInt(parts[1]);
            int dayStart = Integer.parseInt(parts[2]);
            int dayEnd = Integer.parseInt(parts[3]);
            return findSundayBetween(year, month, dayStart, dayEnd);
        }

        // ===== القواعد الخاصة (المعلّمة "add" في الجدول) =====

        // أحد قبل/بعد رفع الصليب (14 أيلول)
        if (rule.equals("sundayBeforeExaltation")) {
            // الأحد بين 7–13 أيلول
            return findSundayBetween(year, 9, 7, 13);
        }
        if (rule.equals("sundayAfterExaltation")) {
            // الأحد بين 15–20 أيلول
            return findSundayBetween(year, 9, 15, 20);
        }

        // أحد قبل/بعد الميلاد (25 كانون الأول)
        if (rule.equals("sundayBeforeNativity")) {
            // الأحد بين 18–24 كانون الأول
            return findSundayBetween(year, 12, 18, 24);
        }
        if (rule.equals("sundayAfterNativity")) {
            // الأحد بين 26–31 كانون الأول داخل نفس السنة
            // ملاحظة: إذا صادف الأحد التالي 1 كانون الثاني، سيُعاد null لأننا لا نعبر للسنة التالية هنا.
            return findSundayBetween(year, 12, 26, 31);
        }

        // أحد قبل/بعد الظهور الإلهي (6 كانون الثاني)
        if (rule.equals("sundayBeforeTheophany")) {
            // الأحد بين 2–5 كانون الثاني
            return findSundayBetween(year, 1, 2, 5);
        }
        if (rule.equals("sundayAfterTheophany")) {
            // الأحد بين 7–13 كانون الثاني
            return findSundayBetween(year, 1, 7, 13);
        }

        // أول سبت بعد الفصح
        if (rule.equals("first_saturday_after_pascha")) {
            return firstSaturdayAfter(paschaDate);
        }

        // الإثنين بعد أحد العنصرة (Pentecost Sunday + 1)
        if (rule.equals("pentecost_sunday_plus_1")) {
            // أحد العنصرة = فصح + 49 يوم ⇒ الإثنين التالي = +50
            return paschaDate.plusDays(50);
        }

        // أضف هنا قواعد إضافية إذا احتجت.
        return null;
    }

    /**
     * يبحث عن الأحد الواقع بين يوميْن (شموليًّا) ضمن شهر وسنة محددين.
     * Sunday في ISO-8601 = 7.
     */
    private LocalDate findSundayBetween(int year, int month, int dayStart, int dayEnd) {
        for (int day = dayStart; day <= dayEnd; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            if (date.getDayOfWeek().getValue() == 7) {
                return date;
            }
        }
        return null;
    }

    /**
     * أول سبت يلي مباشرة تاريخًا مُعطى (startExclusive نفسه غير مُحتسب).
     * Saturday في ISO-8601 = 6.
     */
    private LocalDate firstSaturdayAfter(LocalDate startExclusive) {
        LocalDate d = startExclusive.plusDays(1);
        for (int i = 0; i < 7; i++) {
            if (d.getDayOfWeek().getValue() == 6) {
                return d;
            }
            d = d.plusDays(1);
        }
        // احتياط (لن نصل له عمليًّا)
        return startExclusive.plusDays(6);
    }

    // DTO بسيط لعرض العيد مع التاريخ المحسوب
    @Data
    @AllArgsConstructor
    public static class FeastWithDate {
        private Feast feast;
        private LocalDate date;
    }
}
