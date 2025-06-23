package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalSunday;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiturgicalSundays {

    public List<LiturgicalSunday> buildLiturgicalSundays(
            int year,
            LocalDate pentecost, // يوم أحد العنصرة
            LiturgicalSundayLabelService labelService,
            PaschaDateCalculator paschaDateCalculator,
            String lang
    ) {
        List<LiturgicalSunday> sundays = new ArrayList<>();

        // 1. اجمع جميع تواريخ الآحاد في السنة الميلادية
        List<LocalDate> allSundays = new ArrayList<>();
        LocalDate current = LocalDate.of(year, 1, 1);
        while (current.getDayOfWeek() != DayOfWeek.SUNDAY) current = current.plusDays(1);
        while (current.getYear() == year) {
            allSundays.add(current);
            current = current.plusWeeks(1);
        }

        // 2. جهز جدول تواريخ كل أحد له مفتاح خاص في ملف json
        LocalDate pascha = paschaDateCalculator.getPaschaDate(year);

        Map<LocalDate, String> specialKeys = new HashMap<>();
        specialKeys.put(pascha.minusWeeks(9),  "PHARISEE_PUBLICAN");
        specialKeys.put(pascha.minusWeeks(8),  "PRODIGAL_SON");
        specialKeys.put(pascha.minusWeeks(7),  "BRANCH_SUNDAY");
        specialKeys.put(pascha.minusWeeks(6),  "CHEESE_SUNDAY");
        specialKeys.put(pascha.minusWeeks(5),  "FIRST_LENT_SUNDAY_ORTHODOX");
        specialKeys.put(pascha.minusWeeks(4),  "SECOND_LENT_SUNDAY_PALAMAS");
        specialKeys.put(pascha.minusWeeks(3),  "THIRD_LENT_SUNDAY_CROSS_VENERATION");
        specialKeys.put(pascha.minusWeeks(2),  "FOURTH_LENT_SUNDAY_JOHN_CLIMACUS");
        specialKeys.put(pascha.minusWeeks(1),  "FIFTH_LENT_SUNDAY_MARY_EGYPT");
        specialKeys.put(pascha,                "PASCHA_SUNDAY");
        specialKeys.put(pascha.plusWeeks(1),   "THOMAS_SUNDAY");
        specialKeys.put(pascha.plusWeeks(2),   "MYRRH_BEARING_WOMEN_SUNDAY");
        specialKeys.put(pascha.plusWeeks(3),   "PARALYTIC_SUNDAY");
        specialKeys.put(pascha.plusWeeks(4),   "SAMARITAN_WOMAN_SUNDAY");
        specialKeys.put(pascha.plusWeeks(5),   "BLIND_MAN_SUNDAY");
        specialKeys.put(pascha.plusWeeks(6),   "FATHERS_OF_1ST_COUNCIL");
        specialKeys.put(pascha.plusWeeks(7),   "PENTECOST_SUNDAY");
        specialKeys.put(pascha.plusWeeks(8),   "ALL_SAINTS_SUNDAY");

        // يمكنك لاحقاً إضافة المزيد من المفاتيح الثابتة من ملف json هنا بنفس الأسلوب

        // 3. أول أحد بعد العنصرة (وليس العنصرة نفسه)
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
                // نوع الأحد حسب المفتاح (يمكنك توسيعه لاحقاً)
                if (key.endsWith("SUNDAY") && key.contains("LENT")) {
                    type = "lent";
                } else if (key.contains("PHARISEE") || key.contains("PRODIGAL") || key.contains("BRANCH") || key.contains("CHEESE")) {
                    type = "pre_lent";
                } else if (key.contains("PASCHA") || key.contains("THOMAS") || key.contains("MYRRH") || key.contains("PARALYTIC") || key.contains("SAMARITAN") || key.contains("BLIND") || key.contains("FATHERS") || key.contains("PENTECOST") || key.contains("ALL_SAINTS")) {
                    type = "paschal";
                } else if (key.contains("PALM")) {
                    type = "feast";
                } else {
                    type = "fixed";
                }
                weekNumber = null; // الآحاد الطقسية ليس لها عد بعد العنصرة
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
}
