package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalSunday;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class LiturgicalSundays2 {

    public List<LiturgicalSunday> buildLiturgicalSundays(
            int year,
            LocalDate pentecost, // يوم أحد العنصرة
            LiturgicalSundayLabelService labelService,
            PaschaDateCalculator paschaDateCalculator,
            String lang
    ) {
        List<LiturgicalSunday> sundays = new ArrayList<>();

        // جميع الآحاد في السنة الميلادية
        List<LocalDate> allSundays = new ArrayList<>();
        LocalDate current = LocalDate.of(year, 1, 1);
        while (current.getDayOfWeek() != DayOfWeek.SUNDAY) current = current.plusDays(1);
        while (current.getYear() == year) {
            allSundays.add(current);
            current = current.plusWeeks(1);
        }

        // الفصح
        LocalDate pascha = paschaDateCalculator.getPaschaDate(year);

        // الميلاد والظهور الإلهي (ثابتة)
        LocalDate nativity = LocalDate.of(year, 12, 25);
        LocalDate theophany = LocalDate.of(year, 1, 6);

        // أحد قبل وبعد الميلاد
        LocalDate sundayBeforeNativity = getSundayBefore(nativity);
        LocalDate sundayAfterNativity  = getSundayAfter(nativity);
        // أحد قبل وبعد الظهور الإلهي
        LocalDate sundayBeforeTheophany = getSundayBefore(theophany);
        LocalDate sundayAfterTheophany  = getSundayAfter(theophany);

        // أحد رفع الصليب (14 أيلول)، الأحد الذي بعد أو في هذا اليوم
        LocalDate crossFeast = LocalDate.of(year, 9, 14);
        LocalDate sundayBeforeExaltation = getSundayBefore(crossFeast);
        LocalDate sundayAfterExaltation  = getSundayAfter(crossFeast);

        // أحد الآباء القديسين (الأجداد) قبل الميلاد
        LocalDate forefathersSunday = sundayBeforeNativity.minusWeeks(1);

        // جدول مفاتيح جميع الآحاد الخاصة (من ملف json)
        Map<LocalDate, String> specialKeys = new HashMap<>();
        // متحركات (تهيئة وصوم وفصح وفصحية)
        specialKeys.put(pascha.minusWeeks(9),  "PHARISEE_PUBLICAN");
        specialKeys.put(pascha.minusWeeks(8),  "PRODIGAL_SON");
        specialKeys.put(pascha.minusWeeks(7),  "BRANCH_SUNDAY");
        specialKeys.put(pascha.minusWeeks(6),  "CHEESE_SUNDAY");
        specialKeys.put(pascha.minusWeeks(5),  "FIRST_LENT_SUNDAY_ORTHODOX");
        specialKeys.put(pascha.minusWeeks(4),  "SECOND_LENT_SUNDAY_PALAMAS");
        specialKeys.put(pascha.minusWeeks(3),  "THIRD_LENT_SUNDAY_CROSS_VENERATION");
        specialKeys.put(pascha.minusWeeks(2),  "FOURTH_LENT_SUNDAY_JOHN_CLIMACUS");
        specialKeys.put(pascha.minusWeeks(1),  "FIFTH_LENT_SUNDAY_MARY_EGYPT");
        specialKeys.put(pascha.minusDays(7),   "PALM_SUNDAY"); // أو pascha.minusWeeks(1)
        specialKeys.put(pascha,                "PASCHA_SUNDAY");
        specialKeys.put(pascha.plusWeeks(1),   "THOMAS_SUNDAY");
        specialKeys.put(pascha.plusWeeks(2),   "MYRRH_BEARING_WOMEN_SUNDAY");
        specialKeys.put(pascha.plusWeeks(3),   "PARALYTIC_SUNDAY");
        specialKeys.put(pascha.plusWeeks(4),   "SAMARITAN_WOMAN_SUNDAY");
        specialKeys.put(pascha.plusWeeks(5),   "BLIND_MAN_SUNDAY");
        specialKeys.put(pascha.plusWeeks(6),   "FATHERS_OF_1ST_COUNCIL");
        specialKeys.put(pascha.plusWeeks(7),   "PENTECOST_SUNDAY");
        specialKeys.put(pascha.plusWeeks(8),   "ALL_SAINTS_SUNDAY");
        // ثوابت
        specialKeys.put(sundayBeforeTheophany, "SUNDAY_BEFORE_THEOPHANY");
        specialKeys.put(sundayAfterTheophany,  "SUNDAY_AFTER_THEOPHANY");
        specialKeys.put(sundayBeforeNativity,  "SUNDAY_BEFORE_NATIVITY");
        specialKeys.put(sundayAfterNativity,   "SUNDAY_AFTER_NATIVITY");
        specialKeys.put(forefathersSunday,     "FOREFATHERS_SUNDAY");
        specialKeys.put(sundayBeforeExaltation,"SUNDAY_BEFORE_EXALTATION");
        specialKeys.put(sundayAfterExaltation, "SUNDAY_AFTER_EXALTATION");
        specialKeys.put(nativity,              "NATIVITY");
        specialKeys.put(theophany,             "THEOPHANY");
        // أعياد قديسين أو ثوابت أخرى أضف هنا...

        // ترتيب العد بعد العنصرة: أول أحد بعد العنصرة = 1
        int pentecostIndex = -1;
        for (int i = 0; i < allSundays.size(); i++) {
            if (allSundays.get(i).isAfter(pentecost)) {
                pentecostIndex = i;
                break;
            }
        }
        if (pentecostIndex == -1) pentecostIndex = 0;

        int totalSundays = allSundays.size();

        for (int i = 0; i < totalSundays; i++) {
            int weekAfterPentecost = ((i - pentecostIndex + totalSundays) % totalSundays) + 1;
            LocalDate date = allSundays.get(i);

            String label;
            String type;
            Integer weekNumber = weekAfterPentecost;

            if (specialKeys.containsKey(date)) {
                String key = specialKeys.get(date);
                label = labelService.getLabel(key, lang);
                // نوع الأحد حسب المفتاح (يمكنك تحسين هذا المنطق حسب الجدول!)
                if (key.endsWith("LENT_SUNDAY_ORTHODOX") || key.endsWith("PALAMAS") ||
                        key.endsWith("CROSS_VENERATION") || key.endsWith("JOHN_CLIMACUS") || key.endsWith("MARY_EGYPT")) {
                    type = "lent";
                } else if (key.endsWith("PUBLICAN") || key.endsWith("SON") || key.endsWith("BRANCH_SUNDAY") || key.endsWith("CHEESE_SUNDAY")) {
                    type = "pre_lent";
                } else if (key.endsWith("PASCHA_SUNDAY") || key.endsWith("THOMAS_SUNDAY") || key.endsWith("MYRRH_BEARING_WOMEN_SUNDAY") ||
                        key.endsWith("PARALYTIC_SUNDAY") || key.endsWith("SAMARITAN_WOMAN_SUNDAY") || key.endsWith("BLIND_MAN_SUNDAY") ||
                        key.endsWith("FATHERS_OF_1ST_COUNCIL") || key.endsWith("PENTECOST_SUNDAY") || key.endsWith("ALL_SAINTS_SUNDAY")) {
                    type = "paschal";
                } else if (key.endsWith("PALM_SUNDAY")) {
                    type = "feast";
                } else if (key.endsWith("NATIVITY") || key.endsWith("THEOPHANY") ||
                        key.endsWith("EXALTATION") || key.endsWith("FOREFATHERS_SUNDAY")) {
                    type = "fixed";
                } else {
                    type = "fixed";
                }
                weekNumber = null; // لا يحمل رقم بعد العنصرة
            } else {
                label = labelService.getLabel("AFTER_PENTECOST", lang) + " " + weekAfterPentecost;
                type = "after_pentecost";
            }

            sundays.add(LiturgicalSunday.builder()
                    .date(date)
                    .label(label)
                    .type(type)
                    .weekAfterPentecost(weekNumber)
                    .note("")
                    .lang(lang)
                    .build());
        }

        return sundays;
    }

    // الأحد الذي يسبق تاريخ معين
    private LocalDate getSundayBefore(LocalDate date) {
        LocalDate sunday = date.minusDays(1);
        while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) sunday = sunday.minusDays(1);
        return sunday;
    }

    // الأحد الذي يلي تاريخ معين
    private LocalDate getSundayAfter(LocalDate date) {
        LocalDate sunday = date.plusDays(1);
        while (sunday.getDayOfWeek() != DayOfWeek.SUNDAY) sunday = sunday.plusDays(1);
        return sunday;
    }
}
