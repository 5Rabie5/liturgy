package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import at.antiochorthodox.liturgy.repository.LiturgicalLabelRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LiturgicalLabelService {

    private final LiturgicalLabelRepository labelRepository;
    private final FeastService feastService;

    public LiturgicalLabelService(
            LiturgicalLabelRepository labelRepository,
            FeastService feastService
    ) {
        this.labelRepository = labelRepository;
        this.feastService = feastService;
    }

    public List<LiturgicalLabel> getAllLabels() {
        return labelRepository.findAll();
    }

    public List<LiturgicalLabel> getLabelsByLang(String lang) {
        return labelRepository.findByLang(lang);
    }

    public LiturgicalLabel getLabelByKeyAndLang(String key, String lang) {
        return labelRepository.findByLabelKeyAndLang(key, lang).orElse(null);
    }

    public LiturgicalLabel saveLabel(LiturgicalLabel label) {
        return labelRepository.save(label);
    }

    public void deleteLabel(String id) {
        labelRepository.deleteById(id);
    }

    /**
     * جلب التسمية حسب التاريخ الكامل.
     */
    public String getLabelForDate(LocalDate date, LocalDate pascha, LocalDate nextPhariseePublican, String lang) {
        DayOfWeek day = date.getDayOfWeek();
        LocalDate pentecostSunday = pascha.plusDays(49); // Pentecost = 50th day

        // --- 1. Special Case: Saturday before Pentecost (Saturday of the Departed) ---
        if (date.equals(pentecostSunday.minusDays(1)) && day == DayOfWeek.SATURDAY) {
            String label = getLabelForDay("saturday", "pentecost", 0, lang);
            if (label == null) {
                System.err.printf("⚠️ No label found for Saturday of the Departed (2025-06-07) in lang='%s'%n", lang);
            }
            return label;
        }

        // --- 2. Special Case: Pentecost Sunday itself ---
        if (date.equals(pentecostSunday) && day == DayOfWeek.SUNDAY) {
            String label = getLabelForSunday("pentecost", 0, lang); // weekIndex = 0
            if (label == null) {
                System.err.printf("⚠️ No label found for Pentecost Sunday (2025-06-08) in lang='%s'%n", lang);
            }
            return label;
        }

        // --- 3. Movable Feast? ---
        if (day == DayOfWeek.SUNDAY) {
            String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
            if (movableFeast != null && !movableFeast.isBlank()) {
                return movableFeast;
            }

            // --- 4. Regular Sunday Logic ---
            String season;
            int weekIndex;

            if (!date.isBefore(pascha) && date.isBefore(nextPhariseePublican)) {
                season = "pascha";
                weekIndex = (int) (date.toEpochDay() - pascha.toEpochDay()) / 7;
            } else if (!date.isBefore(nextPhariseePublican)) {
                season = "pentecost";
                weekIndex = (int) (date.toEpochDay() - nextPhariseePublican.toEpochDay()) / 7 + 2;
            } else {
                // Before Pascha: assume part of previous year's Pentecost season
                season = "pentecost";
                weekIndex = (int) (date.toEpochDay() - pascha.minusWeeks(40).toEpochDay()) / 7 + 2;
            }

            String label = getLabelForSunday(season, weekIndex, lang);
            if (label == null) {
                System.err.printf("⚠️ No Sunday label found for %s week %d (%s)%n", season, weekIndex, date);
            }
            return label;
        }

        // --- 5. Weekday Logic ---
        String season;
        int weekIndex;

        if (date.isAfter(pascha) && date.isBefore(pentecostSunday)) {
            season = "pascha";
            weekIndex = countSundaysBetween(pascha.plusDays(1), date);
        } else if (!date.isBefore(pentecostSunday)) {
            season = "pentecost";
            weekIndex = countSundaysBetween(pentecostSunday.plusDays(1), date);
        } else {
            season = "pentecost";
            weekIndex = countSundaysBetween(pascha.minusWeeks(40), date);
        }

        String label = getLabelForDay(day.name().toLowerCase(), season, weekIndex + 1, lang);
        if (label == null) {
            System.err.printf("⚠️ No weekday label found for %s week %d (%s)%n", season, weekIndex + 1, date);
        }
        return label;
    }

    private int countSundaysBetween(LocalDate start, LocalDate end) {
        int count = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.getDayOfWeek() == DayOfWeek.SUNDAY) count++;
            current = current.plusDays(1);
        }
        return count;
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
}
