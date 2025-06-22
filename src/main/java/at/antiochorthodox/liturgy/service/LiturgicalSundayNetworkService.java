package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalSunday;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Service
public class LiturgicalSundayNetworkService {

    private final PaschaDateCalculator paschaDateCalculator;
    private final LiturgicalSundayLabelService labelService;

    @Autowired
    public LiturgicalSundayNetworkService(PaschaDateCalculator paschaDateCalculator, LiturgicalSundayLabelService labelService) {
        this.paschaDateCalculator = paschaDateCalculator;
        this.labelService = labelService;
    }

    // مفاتيح التسمية
    private static final Map<Integer, String> PASCHAL_SUNDAY_KEYS = Map.of(
            0, "PASCHA",
            7, "THOMAS_SUNDAY",
            14, "MYRRH_BEARING_WOMEN",
            21, "PARALYTIC",
            28, "SAMARITAN_WOMAN",
            35, "BLIND_MAN",
            42, "FATHERS_OF_1ST_COUNCIL",
            49, "PENTECOST",
            56, "ALL_SAINTS"
    );

    public List<LiturgicalSunday> buildLiturgicalSundays(int year, String lang) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(year);

        // الميلاد والأحدان السابقان له (الأجداد والآباء)
        LocalDate nativity = LocalDate.of(year, 12, 25);
        LocalDate sundayBeforeNativity = getSundayBefore(nativity);
        LocalDate sundayOfForefathers = getSundayBefore(sundayBeforeNativity);

        // أحد رفع الصليب (14 أيلول، الأحد الأقرب بعد أو في 14 أيلول)
        LocalDate crossFeast = LocalDate.of(year, 9, 14);
        LocalDate sundayAfterCross = getSundayAfterOrSame(crossFeast);

        // أحد النبي إيليا (20 تموز، الأحد الأقرب بعد أو في 20 تموز)
        LocalDate eliasFeast = LocalDate.of(year, 7, 20);
        LocalDate sundayAfterElias = getSundayAfterOrSame(eliasFeast);

        // آحاد بعد العنصرة
        Map<LocalDate, Integer> sundaysAfterPentecost = getSundaysAfterPentecost(pascha.plusDays(49), year);

        List<LiturgicalSunday> result = new ArrayList<>();
        LocalDate current = LocalDate.of(year, 1, 1);
        while (current.getDayOfWeek().getValue() != 7) current = current.plusDays(1);

        while (current.getYear() == year) {
            String label = labelService.getLabel("ORDINARY_SUNDAY", lang);
            String type = "ordinary";
            String note = "";
            Integer weekAfterPentecost = null;

            // فصحي؟
            int daysFromPascha  = (int) (current.toEpochDay() - pascha.toEpochDay());

            if (PASCHAL_SUNDAY_KEYS.containsKey(daysFromPascha)) {
                String key = PASCHAL_SUNDAY_KEYS.get(daysFromPascha);
                label = labelService.getLabel(key, lang);
                type = "paschal";
            } else if (current.equals(sundayOfForefathers)) {
                label = labelService.getLabel("SUNDAY_OF_ANCESTORS", lang);
                type = "fixed";
            } else if (current.equals(sundayBeforeNativity)) {
                label = labelService.getLabel("SUNDAY_OF_FATHERS", lang);
                type = "fixed";
            } else if (current.equals(sundayAfterCross)) {
                label = labelService.getLabel("SUNDAY_AFTER_THE_CROSS", lang);
                type = "fixed";
            } else if (current.equals(sundayAfterElias)) {
                label = labelService.getLabel("SUNDAY_OF_ELIAS", lang);
                type = "fixed";
            } else if (current.equals(nativity)) {
                label = labelService.getLabel("NATIVITY", lang);
                type = "feast";
            } else if (sundaysAfterPentecost.containsKey(current)) {
                int week = sundaysAfterPentecost.get(current);
                // كل أحد بعد العنصرة يأخذ تسمية ديناميكية مثل "الأحد X بعد العنصرة"
                label = labelService.getLabel("AFTER_PENTECOST", lang) + " " + week;
                type = "after_pentecost";
                weekAfterPentecost = week;
            }

            result.add(LiturgicalSunday.builder()
                    .date(current)
                    .label(label)
                    .type(type)
                    .weekAfterPentecost(weekAfterPentecost)
                    .note(note)
                    .lang(lang)
                    .build());

            current = current.plusWeeks(1);
        }
        return result;
    }

    // حساب الأحد الذي يسبق تاريخ معين
    private LocalDate getSundayBefore(LocalDate fixedDate) {
        LocalDate sunday = fixedDate.minusDays(1);
        while (sunday.getDayOfWeek().getValue() != 7) sunday = sunday.minusDays(1);
        return sunday;
    }

    // حساب الأحد الذي يلي أو يصادف تاريخ معين
    private LocalDate getSundayAfterOrSame(LocalDate fixedDate) {
        LocalDate sunday = fixedDate;
        while (sunday.getDayOfWeek().getValue() != 7) sunday = sunday.plusDays(1);
        return sunday;
    }

//    // كل أحد بعد العنصرة مع رقمه
//    private Map<LocalDate, Integer> getSundaysAfterPentecost(LocalDate pentecost, int year) {
//        Map<LocalDate, Integer> map = new LinkedHashMap<>();
//        LocalDate nextSunday = pentecost.plusWeeks(1);
//        int count = 1;
//        while (nextSunday.getYear() == year) {
//            map.put(nextSunday, count);
//            nextSunday = nextSunday.plusWeeks(1);
//            count++;
//        }
//        return map;
//    }
private Map<LocalDate, Integer> getSundaysAfterPentecost(LocalDate pentecost, int year) {
    Map<LocalDate, Integer> map = new LinkedHashMap<>();
    LocalDate nextSunday = pentecost.plusWeeks(1); // الأحد الأول بعد العنصرة
    int count = 1;
    while (nextSunday.getYear() == year) {
        map.put(nextSunday, count);
        nextSunday = nextSunday.plusWeeks(1);
        count++;
    }
    return map;
}
}
