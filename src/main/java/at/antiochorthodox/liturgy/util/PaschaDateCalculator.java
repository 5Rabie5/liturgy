package at.antiochorthodox.liturgy.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Component
public class PaschaDateCalculator {

    /**
     * حساب تاريخ الفصح الأرثوذكسي حسب التقويم الشرقي (Gregorian output)
     */
    public LocalDate getPaschaDate(int year) {
        // Meeus algorithm for Julian Easter
        int a = year % 4;
        int b = year % 7;
        int c = year % 19;
        int d = (19 * c + 15) % 30;
        int e = (2 * a + 4 * b - d + 34) % 7;

        int month = (d + e + 114) / 31;
        int day = ((d + e + 114) % 31) + 1;

        // Julian date of Pascha
        LocalDate julianEaster = LocalDate.of(year, month, day);

        // Convert Julian to Gregorian by adding the proper offset
        return julianEaster.plusDays(getJulianToGregorianOffset(year));
    }

    /**
     * يحسب عدد الأيام التي تفصل بين التقويم اليولياني والميلادي حسب السنة.
     * الفرق يتغير مع الوقت: 10 أيام في عام 1582، 13 يومًا من 1900 إلى 2099، 14 يومًا بعد عام 2100.
     */
    private int getJulianToGregorianOffset(int year) {
        if (year <= 1582) return 0;

        int centuriesSince1600 = (year - 1600) / 100;
        int leapCorrection = (year - 1600) / 400;

        return 10 + centuriesSince1600 - leapCorrection;
    }

    public LocalDate getGreatLentStart(int year) {
        return getPaschaDate(year).minusWeeks(7).with(DayOfWeek.MONDAY);
    }

    public LocalDate getGreatLentEnd(int year) {
        return getPaschaDate(year).minusDays(2);
    }

    public LocalDate getHolyWeekStart(int year) {
        return getPaschaDate(year).minusWeeks(1);
    }

    public LocalDate getPalmSunday(int year) {
        return getHolyWeekStart(year);
    }

    public LocalDate getThomasSunday(int year) {
        return getPaschaDate(year).plusWeeks(1);
    }

    public LocalDate getRenewalSaturday(int year) {
        return getPaschaDate(year).plusDays(6);
    }

    public LocalDate getPentecostDate(int year) {
        return getPaschaDate(year).plusDays(49);
    }

    public boolean isPascha(LocalDate date) {
        return date.equals(getPaschaDate(date.getYear()));
    }

    public boolean isPentecost(LocalDate date) {
        return date.equals(getPentecostDate(date.getYear()));
    }

    public boolean isThomasSunday(LocalDate date) {
        return date.equals(getThomasSunday(date.getYear()));
    }

    public boolean isDuringGreatLent(LocalDate date) {
        return !date.isBefore(getGreatLentStart(date.getYear())) &&
                !date.isAfter(getGreatLentEnd(date.getYear()));
    }

    public boolean isInHolyWeek(LocalDate date) {
        return !date.isBefore(getHolyWeekStart(date.getYear())) &&
                date.isBefore(getPaschaDate(date.getYear()));
    }

    public LocalDate getNextPhariseePublicanSunday(LocalDate pascha) {
        return pascha.minusWeeks(10);
    }
}
