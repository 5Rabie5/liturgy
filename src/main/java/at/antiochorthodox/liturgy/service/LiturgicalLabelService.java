package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import at.antiochorthodox.liturgy.repository.LiturgicalLabelRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LiturgicalLabelService {

    private final LiturgicalLabelRepository labelRepository;
    private final FeastService feastService;
    private final PaschaDateCalculator paschaDateCalculator;

    public LiturgicalLabelService(
            LiturgicalLabelRepository labelRepository,
            FeastService feastService,
            PaschaDateCalculator paschaDateCalculator
    ) {
        this.labelRepository = labelRepository;
        this.feastService = feastService;
        this.paschaDateCalculator = paschaDateCalculator;
    }

    public String getLabelForDate(LocalDate date, LocalDate pascha, String lang) {
        DayOfWeek day = date.getDayOfWeek();
        LocalDate previousPascha = paschaDateCalculator.getPaschaDate(date.getYear() - 1);
        LocalDate pentecostSunday = pascha.plusDays(49);
        LocalDate triodionStart = pascha.minusWeeks(10); // Sunday of the Publican and Pharisee

        boolean isInTriodion = !date.isBefore(triodionStart) && date.isBefore(pascha);
        boolean isInPascha = !date.isBefore(pascha) && date.isBefore(pentecostSunday);

        // Debug Output
        System.out.println("----- Liturgical Date Calculation -----");
        System.out.println("Date: " + date);
        System.out.println("Pascha: " + pascha);
        System.out.println("Triodion Start: " + triodionStart);
        System.out.println("Pentecost Sunday: " + pentecostSunday);
        System.out.println("Is in Triodion? " + isInTriodion);
        System.out.println("Is in Pascha? " + isInPascha);
        System.out.println("Day: " + day);

        // Pentecost Sunday
        if (date.equals(pentecostSunday) && day == DayOfWeek.SUNDAY) {
            System.out.println("[PENTECOST SUNDAY]");
            return getLabelForSunday("pentecost", 0, lang);
        }

        // Pentecost Saturday
        if (date.equals(pentecostSunday.minusDays(1)) && day == DayOfWeek.SATURDAY) {
            System.out.println("[PENTECOST SATURDAY]");
            return getLabelForDay("saturday", "pentecost", 0, lang);
        }

        // Triodion Season
        if (isInTriodion) {
            int weekIndex = (int) ChronoUnit.WEEKS.between(triodionStart, date);
            System.out.println("[TRIODION] Week index: " + weekIndex);
            if (day == DayOfWeek.SUNDAY) {
                return getLabelForSunday("triodion", weekIndex, lang);
            } else {
                return getLabelForDay(day.name().toLowerCase(), "triodion", weekIndex, lang);
            }
        }

        // Pascha Season
        if (isInPascha) {
            int weekIndex = (int) ChronoUnit.WEEKS.between(pascha, date);
            System.out.println("[PASCHA] Week index: " + weekIndex);
            if (day == DayOfWeek.SUNDAY) {
                return getLabelForSunday("pascha", weekIndex, lang);
            } else {
                return getLabelForDay(day.name().toLowerCase(), "pascha", weekIndex, lang);
            }
        }

        // Movable Feast on Sunday or Saturday
        if (day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY) {
            String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
            if (movableFeast != null && !movableFeast.isBlank()) {
                System.out.println("[MOVABLE FEAST] Found: " + movableFeast);
                return movableFeast;
            }
        }

        // After Pentecost Season
        if (!date.isBefore(pentecostSunday.plusDays(1))) {
            int weekIndex = countSundaysBetween(pentecostSunday, date);
            System.out.println("[AFTER PENTECOST] Week index: " + weekIndex);
            if (day == DayOfWeek.SUNDAY) {
                return getLabelForSunday("pentecost", weekIndex, lang);
            } else {
                return getLabelForDay(day.name().toLowerCase(), "pentecost", weekIndex, lang);
            }
        }

        // Before Triodion Season (Post previous Pentecost)
        if (day == DayOfWeek.SUNDAY) {
            int weekIndex = countSundaysBetween(previousPascha.plusDays(49), date);
            System.out.println("[BEFORE TRIODION] Week index: " + weekIndex);
            return getLabelForSunday("pentecost", weekIndex, lang);
        }

        int weekIndex = countSundaysBetween(previousPascha.plusDays(49), date);
        System.out.println("[BEFORE TRIODION - weekday] Week index: " + weekIndex);
        return getLabelForDay(day.name().toLowerCase(), "pentecost", weekIndex, lang);
    }



    public String getLabelForDay(String dayOfWeek, String season, int weekIndex, String lang) {
        Optional<LiturgicalLabel> labelOpt = labelRepository
                .findByTypeAndSeasonAndWeekIndexAndDayOfWeekAndLang("weekday", season, weekIndex, dayOfWeek.toLowerCase(), lang);
        return labelOpt.map(LiturgicalLabel::getText).orElse(null);
    }

    public String getLabelForSunday(String season, int weekIndex, String lang) {
        Optional<LiturgicalLabel> labelOpt = labelRepository
                .findByTypeAndSeasonAndWeekIndexAndDayOfWeekIsNullAndLang("sunday", season, weekIndex, lang);
        return labelOpt.map(LiturgicalLabel::getText).orElse(null);
    }

    private int countSundaysBetween(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                count++;
            }
            current = current.plusDays(1);
        }
        return count;
    }

    public List<LiturgicalLabel> getLabelsByLang(String lang) {
        return labelRepository.findByLang(lang);
    }

    public LiturgicalLabel getLabelByKeyAndLang(String key, String lang) {
        return labelRepository.findByLabelKeyAndLang(key, lang).orElse(null);
    }

    public List<LiturgicalLabel> getAllLabels() {
        return labelRepository.findAll();
    }

    public LiturgicalLabel saveLabel(LiturgicalLabel label) {
        return labelRepository.save(label);
    }

    public void deleteLabel(String id) {
        labelRepository.deleteById(id);
    }

}
