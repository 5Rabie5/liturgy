package at.antiochorthodox.liturgy.util;

import java.time.LocalDate;

public class PaschaDateCalculator {

    /**
     * يحسب تاريخ الفصح الأرثوذكسي (بالتقويم الميلادي) لسنة معينة.
     * @param year السنة الميلادية المطلوبة
     * @return تاريخ عيد الفصح الأرثوذكسي
     */
    public static LocalDate calculateOrthodoxEaster(int year) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int d = (19 * a + 15) % 30;
        int e = (2 * b + 4 * c + 6 * d + 6) % 7;

        int month, day;

        if ((d + e) < 10) {
            month = 3; // March
            day = 22 + d + e;
        } else {
            month = 4; // April
            day = d + e - 9;
        }

        // تاريخ الفصح بالتقويم اليولياني
        LocalDate julianEaster = LocalDate.of(year, month, day);

        // الفارق الديناميكي بين التقويم اليولياني والميلادي
        int offset = (year / 100) - (year / 400) - 2;

        // إضافة الفارق للتحويل إلى التاريخ الميلادي
        return julianEaster.plusDays(offset);
    }
}
