import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SimpleAfterPentecostCounter {

    public static class SimpleSunday {
        public LocalDate date;
        public String label;
        public Integer weekAfterPentecost;

        public SimpleSunday(LocalDate date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return date + ": " + label + (weekAfterPentecost != null ? " [الأحد " + weekAfterPentecost + " بعد العنصرة]" : "");
        }
    }

    public static List<SimpleSunday> buildOrderedSundaysExtended(int year, LocalDate pentecost, LocalDate nextPhariseePublican) {
        List<SimpleSunday> sundays = new ArrayList<>();

        // 1. اجمع كل الآحاد من أول أحد بعد العنصرة حتى أول أحد تهيئة السنة التالية (شاملًا)
        LocalDate current = pentecost.plusWeeks(1);
        int week = 1;
        while (current.isBefore(nextPhariseePublican)) {
            SimpleSunday s = new SimpleSunday(current);
            s.label = "AFTER_PENTECOST " + week;
            s.weekAfterPentecost = week;
            sundays.add(s);
            week++;
            current = current.plusWeeks(1);
        }

        // 2. اجمع آحاد السنة من يناير حتى أحد العنصرة (للتكامل/التوثيق فقط)
        List<SimpleSunday> beforePentecost = new ArrayList<>();
        current = LocalDate.of(year, 1, 1);
        while (current.getDayOfWeek() != DayOfWeek.SUNDAY) current = current.plusDays(1);
        while (!current.isAfter(pentecost)) {
            SimpleSunday s = new SimpleSunday(current);
            s.label = "BEFORE_PENTECOST";
            s.weekAfterPentecost = null;
            beforePentecost.add(s);
            current = current.plusWeeks(1);
        }

        // 3. رتب القائمة: بعد العنصرة (حتى أحد التهيئة التالي) ثم آحاد بداية السنة
        sundays.addAll(beforePentecost);

        return sundays;
    }

    // تجربة (عين تواريخ العنصرة وأحد الفريسي والعشار للسنة التالية يدويًا أو باستخدام دوالك)
    public static void main(String[] args) {
        int year = 2022;
        LocalDate pentecost = LocalDate.of(2022, 6, 12);
        // أحد الفريسي والعشار في 2023 حسب التقويم الأرثوذكسي (مثلاً 5 شباط 2023)
        LocalDate nextPhariseePublican = LocalDate.of(2023, 2, 5);

        List<SimpleSunday> sundays = buildOrderedSundaysExtended(year, pentecost, nextPhariseePublican);

        System.out.println("=== العد بعد العنصرة حتى أول أحد تهيئة في السنة التالية ===");
        int i = 1;
        for (SimpleSunday s : sundays) {
            System.out.println(i + ". " + s);
            i++;
        }
    }
}
