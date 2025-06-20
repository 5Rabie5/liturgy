package at.antiochorthodox.liturgy.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
@Component
public class PaschaDateCalculator {

    // تاريخ الفصح الأرثوذكسي (Gregorian)
    public static LocalDate getPaschaDate(int year) {
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

        LocalDate julianEaster = LocalDate.of(year, month, day);
        int offset = (year / 100) - (year / 400) - 2;
        return julianEaster.plusDays(offset);
    }

    // بداية الصوم الكبير (الإثنين النظيف)
    public static LocalDate getGreatLentStart(int year) {
        return getPaschaDate(year).minusWeeks(7);
    }

    // نهاية الصوم الكبير (الجمعة العظيمة)
    public static LocalDate getGreatLentEnd(int year) {
        return getPaschaDate(year).minusDays(2);
    }

    // سبت التجديدات (أول سبت بعد الفصح)
    public static LocalDate getRenewalSaturday(int year) {
        return getPaschaDate(year).plusDays(6);
    }

    // أحد توما (الأحد الثاني بعد الفصح)
    public static LocalDate getThomasSunday(int year) {
        return getPaschaDate(year).plusDays(7);
    }

    // أحد العنصرة (اليوم الـ50 بعد الفصح)
    public static LocalDate getPentecostDate(int year) {
        return getPaschaDate(year).plusDays(49);
    }
}
