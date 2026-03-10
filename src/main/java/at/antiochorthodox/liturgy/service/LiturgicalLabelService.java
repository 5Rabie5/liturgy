package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import at.antiochorthodox.liturgy.repository.LiturgicalLabelRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class LiturgicalLabelService {

    public static final String DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE = "SATURDAY_OF_SOULS_BEFORE_MEATFARE";
    public static final String DAY_KEY_SATURDAY_OF_SOULS_BEFORE_PENTECOST = "SATURDAY_OF_SOULS_BEFORE_PENTECOST";

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

    /**
     * Legacy API kept for compatibility. Returns the display label for the resolved day.
     */
    public String getLabelForDate(LocalDate date, LocalDate pascha, String lang) {
        return resolveLabelEntityForDate(date, pascha, lang)
                .map(LiturgicalLabel::getText)
                .orElse(null);
    }

    public String getDayKeyForDate(LocalDate date, LocalDate pascha, String lang) {
        return resolveLabelEntityForDate(date, pascha, lang)
                .map(this::ensureDayKey)
                .orElse(null);
    }

    public String getLabelForDayKey(String dayKey, String lang) {
        if (dayKey == null || dayKey.isBlank()) {
            return null;
        }

        Optional<LiturgicalLabel> byStoredDayKey = labelRepository.findByDayKeyAndLang(dayKey, lang);
        if (byStoredDayKey.isPresent()) {
            return byStoredDayKey.get().getText();
        }

        DayKeyParts parts = parseDayKey(dayKey);
        if (parts == null) {
            return null;
        }

        Optional<LiturgicalLabel> labelOpt = parts.isSunday()
                ? findSundayLabel(parts.season(), parts.weekIndex(), lang)
                : findDayLabel(parts.season(), parts.weekIndex(), parts.dayOfWeek(), lang);

        return labelOpt.map(LiturgicalLabel::getText).orElse(null);
    }

    public Optional<LiturgicalLabel> resolveLabelEntityForDate(LocalDate date, LocalDate pascha, String lang) {
        DayOfWeek day = date.getDayOfWeek();
        LocalDate previousPascha = paschaDateCalculator.getPaschaDate(date.getYear() - 1);
        LocalDate pentecostSunday = pascha.plusDays(49);
        LocalDate triodionStart = pascha.minusWeeks(10);

        boolean isInTriodion = !date.isBefore(triodionStart) && date.isBefore(pascha);
        boolean isInPascha = !date.isBefore(pascha) && date.isBefore(pentecostSunday);

        Optional<LiturgicalLabel> specialLabel = findSpecialLabelForDate(date, pascha, pentecostSunday, lang);
        if (specialLabel.isPresent()) {
            return specialLabel;
        }

        if (date.equals(pentecostSunday) && day == DayOfWeek.SUNDAY) {
            return findSundayLabel("pentecost", 0, lang);
        }

        // Legacy fallback only. In the Antiochian workflow this day should normally be handled
        // by SATURDAY_OF_SOULS_BEFORE_PENTECOST above.
        if (date.equals(pentecostSunday.minusDays(1)) && day == DayOfWeek.SATURDAY) {
            return findDayLabel("pentecost", 0, "saturday", lang);
        }

        if (isInTriodion) {
            int weekIndex = (int) ChronoUnit.WEEKS.between(triodionStart, date);
            return day == DayOfWeek.SUNDAY
                    ? findSundayLabel("triodion", weekIndex, lang)
                    : findDayLabel("triodion", weekIndex, day.name().toLowerCase(Locale.ROOT), lang);
        }

        if (isInPascha) {
            int weekIndex = (int) ChronoUnit.WEEKS.between(pascha, date);
            return day == DayOfWeek.SUNDAY
                    ? findSundayLabel("pascha", weekIndex, lang)
                    : findDayLabel("pascha", weekIndex, day.name().toLowerCase(Locale.ROOT), lang);
        }

        // Intentionally do not replace the liturgical day with a movable feast here.
        // Movable feasts are handled separately by FeastService.

        if (!date.isBefore(pentecostSunday.plusDays(1))) {
            int weekIndex = countSundaysBetween(pentecostSunday, date);
            return day == DayOfWeek.SUNDAY
                    ? findSundayLabel("pentecost", weekIndex, lang)
                    : findDayLabel("pentecost", weekIndex, day.name().toLowerCase(Locale.ROOT), lang);
        }

        int weekIndex = countSundaysBetween(previousPascha.plusDays(49), date);
        return day == DayOfWeek.SUNDAY
                ? findSundayLabel("pentecost", weekIndex, lang)
                : findDayLabel("pentecost", weekIndex, day.name().toLowerCase(Locale.ROOT), lang);
    }

    public List<LiturgicalLabel> getLabelsByLang(String lang) {
        return labelRepository.findByLang(lang);
    }

    public LiturgicalLabel getLabelByKeyAndLang(String key, String lang) {
        return labelRepository.findByLabelKeyAndLang(key, lang).orElse(null);
    }

    public LiturgicalLabel getLabelByDayKeyAndLang(String dayKey, String lang) {
        Optional<LiturgicalLabel> stored = labelRepository.findByDayKeyAndLang(dayKey, lang);
        if (stored.isPresent()) {
            return stored.get();
        }

        String text = getLabelForDayKey(dayKey, lang);
        if (text == null) {
            return null;
        }

        DayKeyParts parts = parseDayKey(dayKey);
        if (parts == null) {
            return null;
        }

        return LiturgicalLabel.builder()
                .dayKey(dayKey)
                .labelKey(buildLegacyLabelKey(parts))
                .lang(lang)
                .text(text)
                .type(parts.isSunday() ? "sunday" : "weekday")
                .season(parts.season())
                .weekIndex(parts.weekIndex())
                .dayOfWeek(!parts.isSunday() ? parts.dayOfWeek() : null)
                .build();
    }

    public List<LiturgicalLabel> getAllLabels() {
        return labelRepository.findAll();
    }

    public LiturgicalLabel saveLabel(LiturgicalLabel label) {
        if (label.getDayKey() == null || label.getDayKey().isBlank()) {
            label.setDayKey(deriveDayKey(label));
        }
        return labelRepository.save(label);
    }

    public void deleteLabel(String id) {
        labelRepository.deleteById(id);
    }

    private Optional<LiturgicalLabel> findSpecialLabelForDate(
            LocalDate date,
            LocalDate pascha,
            LocalDate pentecostSunday,
            String lang
    ) {
        if (date.getDayOfWeek() != DayOfWeek.SATURDAY) {
            return Optional.empty();
        }

        // Saturday of Souls before Meatfare Sunday.
        // Meatfare Sunday = 8 weeks before Pascha, so the preceding Saturday = Pascha - 57 days.
        LocalDate meatfareSoulSaturday = pascha.minusDays(57);
        if (date.equals(meatfareSoulSaturday)) {
            return labelRepository.findByDayKeyAndLang(DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE, lang)
                    .or(() -> labelRepository.findByLabelKeyAndLang(DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE, lang));
        }

        // Saturday of Souls before Pentecost.
        LocalDate pentecostSoulSaturday = pentecostSunday.minusDays(1);
        if (date.equals(pentecostSoulSaturday)) {
            return labelRepository.findByDayKeyAndLang(DAY_KEY_SATURDAY_OF_SOULS_BEFORE_PENTECOST, lang)
                    .or(() -> labelRepository.findByLabelKeyAndLang(DAY_KEY_SATURDAY_OF_SOULS_BEFORE_PENTECOST, lang));
        }

        return Optional.empty();
    }

    private Optional<LiturgicalLabel> findDayLabel(String season, int weekIndex, String dayOfWeek, String lang) {
        return labelRepository.findByTypeAndSeasonAndWeekIndexAndDayOfWeekAndLang(
                "weekday", season, weekIndex, dayOfWeek.toLowerCase(Locale.ROOT), lang
        );
    }

    private Optional<LiturgicalLabel> findSundayLabel(String season, int weekIndex, String lang) {
        Optional<LiturgicalLabel> label = labelRepository.findByTypeAndSeasonAndWeekIndexAndDayOfWeekIsNullAndLang(
                "sunday", season, weekIndex, lang
        );
        return label.isPresent()
                ? label
                : labelRepository.findByTypeAndSeasonAndWeekIndexAndDayOfWeekAndLang("sunday", season, weekIndex, "", lang);
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

    public String deriveDayKey(LiturgicalLabel label) {
        if (label == null) {
            return null;
        }
        if (label.getDayKey() != null && !label.getDayKey().isBlank()) {
            return label.getDayKey();
        }
        if ("special".equalsIgnoreCase(label.getType()) && label.getLabelKey() != null && !label.getLabelKey().isBlank()) {
            return label.getLabelKey();
        }
        if (label.getSeason() == null || label.getWeekIndex() == null) {
            return null;
        }

        String season = label.getSeason().toUpperCase(Locale.ROOT);
        if ("sunday".equalsIgnoreCase(label.getType())) {
            return String.format("%s_W%02d_SUNDAY", season, label.getWeekIndex());
        }

        String day = label.getDayOfWeek() == null ? "DAY" : label.getDayOfWeek().toUpperCase(Locale.ROOT);
        return String.format("%s_W%02d_%s", season, label.getWeekIndex(), day);
    }

    private String ensureDayKey(LiturgicalLabel label) {
        String dayKey = deriveDayKey(label);
        if (label.getDayKey() == null || label.getDayKey().isBlank()) {
            label.setDayKey(dayKey);
        }
        return dayKey;
    }

    private DayKeyParts parseDayKey(String dayKey) {
        if (dayKey == null || dayKey.isBlank()) {
            return null;
        }

        String[] parts = dayKey.split("_");
        if (parts.length != 3) {
            return null;
        }

        try {
            String season = parts[0].toLowerCase(Locale.ROOT);
            String weekToken = parts[1].toUpperCase(Locale.ROOT);
            if (!weekToken.startsWith("W")) {
                return null;
            }
            int weekIndex = Integer.parseInt(weekToken.substring(1));
            String day = parts[2].toLowerCase(Locale.ROOT);
            return new DayKeyParts(season, day, weekIndex);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildLegacyLabelKey(DayKeyParts parts) {
        if (parts == null) {
            return null;
        }
        String season = parts.season().toUpperCase(Locale.ROOT);
        if (parts.isSunday()) {
            return season + "_SUNDAY_" + parts.weekIndex();
        }
        return season + "_WEEKDAY_" + parts.weekIndex();
    }

    private record DayKeyParts(String season, String dayOfWeek, int weekIndex) {
        boolean isSunday() {
            return "sunday".equals(dayOfWeek);
        }
    }
}