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

    public String getLabelForDate(LocalDate date, LocalDate pascha, LocalDate nextPhariseePublican, String lang) {
        DayOfWeek day = date.getDayOfWeek();
        LocalDate pentecostSunday = pascha.plusDays(49);

        if (date.equals(pentecostSunday.minusDays(1)) && day == DayOfWeek.SATURDAY) {
            return getLabelForDay("saturday", "pentecost", 0, lang);
        }

        if (date.equals(pentecostSunday) && day == DayOfWeek.SUNDAY) {
            return getLabelForSunday("pentecost", 0, lang);
        }

        if (day == DayOfWeek.SUNDAY) {
            String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
            if (movableFeast != null && !movableFeast.isBlank()) {
                return movableFeast;
            }

            String season;
            int weekIndex;

            if (!date.isBefore(pascha) && date.isBefore(nextPhariseePublican)) {
                season = "pascha";
                weekIndex = (int) (date.toEpochDay() - pascha.toEpochDay()) / 7;
            } else if (!date.isBefore(nextPhariseePublican)) {
                season = "pentecost";
                weekIndex = (int) (date.toEpochDay() - nextPhariseePublican.toEpochDay()) / 7 + 2;
            } else {
                season = "pentecost";
                weekIndex = (int) (date.toEpochDay() - pascha.minusWeeks(40).toEpochDay()) / 7 + 2;
            }

            return getLabelForSunday(season, weekIndex, lang);
        }

        String season;
        int weekIndex;

        if (date.isAfter(pascha) && date.isBefore(pentecostSunday)) {
            season = "pascha";
            long daysAfterPascha = date.toEpochDay() - pascha.toEpochDay();
            if (daysAfterPascha <= 6) {
                weekIndex = 0; // Renewal week
            } else {
                weekIndex = (int) (daysAfterPascha / 7);
            }
        } else if (!date.isBefore(pentecostSunday)) {
            season = "pentecost";
            weekIndex = countSundaysBetween(pentecostSunday.plusDays(1), date);
        } else {
            season = "pentecost";
            weekIndex = countSundaysBetween(pascha.minusWeeks(40), date);
        }

        return getLabelForDay(day.name().toLowerCase(), season, weekIndex, lang);
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
