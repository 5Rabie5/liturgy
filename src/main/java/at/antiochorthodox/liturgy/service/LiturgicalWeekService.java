package at.antiochorthodox.liturgy.service;


import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
public class LiturgicalWeekService {

    public static class SimpleSunday {
        public LocalDate date;
        public String label;
        public Integer weekAfterPentecost;

        public SimpleSunday(LocalDate date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return date + ": " + label + (weekAfterPentecost != null ? " [" + label + "]" : "");
        }
    }

    public static List<SimpleSunday> buildOrderedSundaysExtended(int year, LocalDate pascha, LocalDate nextPhariseePublican, String lang) {
        List<SimpleSunday> sundays = new ArrayList<>();
        List<String> labels = predefinedLabels.getOrDefault(lang, predefinedLabels.get("en"));

        // الأحد الأول: القيامة
        LocalDate current = pascha;
        for (int i = 0; i < labels.size(); i++) {
            SimpleSunday s = new SimpleSunday(current);
            s.label = labels.get(i);
            s.weekAfterPentecost = (i <= 1) ? null : (i - 1);
            sundays.add(s);
            current = current.plusWeeks(1);
        }

        // الأسابيع بعد أحد جميع القديسين
        int week = 2;
        while (current.isBefore(nextPhariseePublican)) {
            SimpleSunday s = new SimpleSunday(current);
            s.weekAfterPentecost = week;
            s.label = getLocalizedOrdinal(lang, week);
            sundays.add(s);
            current = current.plusWeeks(1);
            week++;
        }

        return sundays;
    }

    private static String getLocalizedOrdinal(String lang, int number) {
        switch (lang) {
            case "ar":
                return "الأحد " + toArabicOrdinal(number) + " بعد العنصرة";
            case "en":
                return "Sunday after Pentecost #" + number;
            default:
                return "Week " + number;
        }
    }

    private static String toArabicOrdinal(int number) {
        return switch (number) {
            case 1 -> "الأول";
            case 2 -> "الثاني";
            case 3 -> "الثالث";
            case 4 -> "الرابع";
            case 5 -> "الخامس";
            case 6 -> "السادس";
            case 7 -> "السابع";
            case 8 -> "الثامن";
            case 9 -> "التاسع";
            case 10 -> "العاشر";
            case 11 -> "الحادي عشر";
            case 12 -> "الثاني عشر";
            case 13 -> "الثالث عشر";
            default -> "رقم " + number;
        };
    }

    private static final java.util.Map<String, List<String>> predefinedLabels = new java.util.HashMap<>();

    static {
        predefinedLabels.put("ar", List.of(
                "أحد القيامة",
                "أحد توما",
                "أحد حاملات الطيب",
                "أحد المخلع",
                "أحد السامرية",
                "أحد الأعمى",
                "أحد الآباء في نيقية",
                "عيد العنصرة",
                "أحد جميع القديسين"
        ));

        predefinedLabels.put("en", List.of(
                "Pascha Sunday",
                "Thomas Sunday",
                "Sunday of the Myrrh-bearing Women",
                "Sunday of the Paralytic",
                "Sunday of the Samaritan Woman",
                "Sunday of the Blind Man",
                "Sunday of the Fathers of Nicaea",
                "Pentecost",
                "All Saints Sunday"
        ));
    }
    public String findLiturgicalNameForDate(LocalDate date, LocalDate pascha, LocalDate nextPhariseePublican, String lang) {
        List<LiturgicalWeekService.SimpleSunday> sundays = buildOrderedSundaysExtended(date.getYear(), pascha, nextPhariseePublican, lang);
        return sundays.stream()
                .filter(s -> s.date.equals(date))
                .map(s -> s.label)
                .findFirst()
                .orElse(null);
    }

}
