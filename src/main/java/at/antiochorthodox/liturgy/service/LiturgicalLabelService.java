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
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            // أولاً: افحص إذا كان هناك عيد متنقل خاص بهذا الأحد (مثل أحد الفريسي والعشار، أحد الصوم...)
            String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
            if (movableFeast != null && !movableFeast.isBlank()) {
                return movableFeast;
            }

            // أحد عادي بعد الفصح أو بعد العنصرة
            String season;
            int weekIndex;

            if (!date.isBefore(pascha) && date.isBefore(nextPhariseePublican)) {
                season = "pascha";
                weekIndex = (int) (date.toEpochDay() - pascha.toEpochDay()) / 7;
            } else if (!date.isBefore(nextPhariseePublican)) {
                season = "pentecost";
                weekIndex = (int) (date.toEpochDay() - nextPhariseePublican.toEpochDay()) / 7 + 2;
            } else {
                // افتراض أن كل أحد قبل الفصح هو أحد بعد العنصرة من السنة السابقة
                season = "pentecost";
                weekIndex = (int) (date.toEpochDay() - pascha.minusWeeks(40).toEpochDay()) / 7 + 2;
            }

            return getLabelForSunday(season, weekIndex, lang);
        } else {
            // أيام الأسبوع غير الأحد
            String season;
            int weekIndex;
            LocalDate pentecost = pascha.plusDays(49);

            if (date.isAfter(pascha) && date.isBefore(pentecost)) {
                season = "pascha";
                weekIndex = countSundaysBetween(pascha.plusDays(1), date);
            } else if (!date.isBefore(pentecost)) {
                season = "pentecost";
                weekIndex = countSundaysBetween(pentecost.plusDays(1), date);
            } else {
                season = "pentecost";
                weekIndex = countSundaysBetween(pascha.minusWeeks(40), date);
            }

            return getLabelForDay(date.getDayOfWeek().name().toLowerCase(), season, weekIndex + 1, lang);
        }
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
