package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import at.antiochorthodox.liturgy.repository.LiturgicalLabelRepository;
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

    public String getLabelForDate(LocalDate date, LocalDate pascha, String lang) {
        DayOfWeek day = date.getDayOfWeek();
        LocalDate pentecostSunday = pascha.plusDays(49);
        LocalDate triodionStart = pascha.minusWeeks(10); // أحد الفريسي والعشار

        System.out.printf("📅 Checking date: %s | DayOfWeek: %s%n", date, day);
        System.out.printf("🕊️ Pascha: %s | Pentecost Sunday: %s | Triodion Start: %s%n", pascha, pentecostSunday, triodionStart);

        // 1. السبت قبل العنصرة
        if (date.equals(pentecostSunday.minusDays(1)) && day == DayOfWeek.SATURDAY) {
            return getLabelForDay("saturday", "pentecost", 0, lang);
        }

        // 2. أحد العنصرة
        if (date.equals(pentecostSunday) && day == DayOfWeek.SUNDAY) {
            return getLabelForSunday("pentecost", 0, lang);
        }

        // 3. التريودي
        if (!date.isBefore(triodionStart) && date.isBefore(pascha)) {
            String season = "triodion";
            int weekIndex = (int) ChronoUnit.WEEKS.between(triodionStart, date);

            if (day == DayOfWeek.SUNDAY) {
                System.out.printf("📘 Triodion Sunday: weekIndex=%d%n", weekIndex);
                return getLabelForSunday(season, weekIndex, lang);
            } else {
                System.out.printf("📘 Triodion Weekday: weekIndex=%d%n", weekIndex);
                return getLabelForDay(day.name().toLowerCase(), season, weekIndex, lang);
            }
        }

        // 4. الأحـاد الأخرى (خارج التريودي)
        if (day == DayOfWeek.SUNDAY) {
            String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);
            if (movableFeast != null && !movableFeast.isBlank()) {
                System.out.printf("📦 Movable Feast: %s%n", movableFeast);
                return movableFeast;
            }

            String season;
            int weekIndex;

            if (!date.isBefore(pascha) && date.isBefore(pentecostSunday)) {
                season = "pascha";
                weekIndex = (int) ChronoUnit.WEEKS.between(pascha, date);
            } else if (!date.isBefore(pentecostSunday)) {
                season = "pentecost";
                weekIndex = (int) ChronoUnit.WEEKS.between(pentecostSunday.plusDays(1), date);
            } else {
                season = "pentecost";
                weekIndex = (int) ChronoUnit.WEEKS.between(pascha.minusWeeks(40), date);
            }

            System.out.printf("📕 Sunday: season=%s, weekIndex=%d%n", season, weekIndex);
            String label = getLabelForSunday(season, weekIndex, lang);
            System.out.printf("🔎 Sunday label: %s%n", label);
            return label;
        }

        // 5. باقي الأيام
        String season;
        int weekIndex;

        if (date.isAfter(pascha) && date.isBefore(pentecostSunday)) {
            season = "pascha";
            long daysAfterPascha = ChronoUnit.DAYS.between(pascha, date);
            if (daysAfterPascha <= 6) {
                weekIndex = 0; // أسبوع التجديدات
            } else {
                weekIndex = (int) (daysAfterPascha / 7);
            }
        } else if (!date.isBefore(pentecostSunday)) {
            season = "pentecost";
            weekIndex = (int) ChronoUnit.WEEKS.between(pentecostSunday.plusDays(1), date);
        } else {
            season = "pentecost";
            weekIndex = (int) ChronoUnit.WEEKS.between(pascha.minusWeeks(40), date);
        }

        return getLabelForDay(day.name().toLowerCase(), season, weekIndex, lang);
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
