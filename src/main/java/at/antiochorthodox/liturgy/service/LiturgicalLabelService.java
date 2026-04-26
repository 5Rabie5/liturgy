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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LiturgicalLabelService {

    public static final String DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE = "SATURDAY_OF_SOULS_BEFORE_MEATFARE";
    public static final String DAY_KEY_SATURDAY_OF_SOULS_BEFORE_PENTECOST = "SATURDAY_OF_SOULS_BEFORE_PENTECOST";

    public static final String DAY_KEY_LAZARUS_SATURDAY = "LAZARUS_SATURDAY";
    public static final String DAY_KEY_PALM_SUNDAY = "PALM_SUNDAY";
    public static final String DAY_KEY_HOLY_MONDAY = "HOLY_MONDAY";
    public static final String DAY_KEY_HOLY_TUESDAY = "HOLY_TUESDAY";
    public static final String DAY_KEY_HOLY_WEDNESDAY = "HOLY_WEDNESDAY";
    public static final String DAY_KEY_HOLY_THURSDAY = "HOLY_THURSDAY";
    public static final String DAY_KEY_HOLY_FRIDAY = "HOLY_FRIDAY";
    public static final String DAY_KEY_HOLY_SATURDAY = "HOLY_SATURDAY";
    public static final String DAY_KEY_PASCHA_SUNDAY = "PASCHA_SUNDAY";
    public static final String DAY_KEY_RENEWAL_SATURDAY = "RENEWAL_SATURDAY";
    public static final String DAY_KEY_THOMAS_SUNDAY = "THOMAS_SUNDAY";
    public static final String DAY_KEY_MID_PENTECOST = "MID_PENTECOST";
    public static final String DAY_KEY_ASCENSION = "ASCENSION";
    public static final String DAY_KEY_PENTECOST_SUNDAY = "PENTECOST_SUNDAY";
    public static final String DAY_KEY_MONDAY_OF_HOLY_SPIRIT = "MONDAY_OF_HOLY_SPIRIT";
    public static final String DAY_KEY_ALL_SAINTS_SUNDAY = "ALL_SAINTS_SUNDAY";

    public static final String DAY_KEY_FATHERS_1ST_ECUMENICAL_COUNCIL_SUNDAY = "FATHERS_1ST_ECUMENICAL_COUNCIL_SUNDAY";
    public static final String DAY_KEY_FATHERS_4TH_ECUMENICAL_COUNCIL_SUNDAY = "FATHERS_4TH_ECUMENICAL_COUNCIL_SUNDAY";
    public static final String DAY_KEY_FATHERS_7TH_ECUMENICAL_COUNCIL_SUNDAY = "FATHERS_7TH_ECUMENICAL_COUNCIL_SUNDAY";

    public static final String DAY_KEY_FOREFATHERS_SUNDAY = "FOREFATHERS_SUNDAY";
    public static final String DAY_KEY_HOLY_ANCESTORS_SUNDAY = "HOLY_ANCESTORS_SUNDAY";
    public static final String DAY_KEY_SUNDAY_BEFORE_NATIVITY = "SUNDAY_BEFORE_NATIVITY";
    public static final String DAY_KEY_SUNDAY_AFTER_NATIVITY = "SUNDAY_AFTER_NATIVITY";
    public static final String DAY_KEY_SUNDAY_BEFORE_THEOPHANY = "SUNDAY_BEFORE_THEOPHANY";
    public static final String DAY_KEY_SUNDAY_AFTER_THEOPHANY = "SUNDAY_AFTER_THEOPHANY";

    private static final String DEFAULT_LABEL_LANG = "ar";
    private static final String SECONDARY_FALLBACK_LANG = "en";
    private static final Pattern LANGUAGE_TOKEN_PATTERN = Pattern.compile("([A-Za-z]{2,8})");

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
        String dayKey = getDayKeyForDate(date, pascha, lang);
        return dayKey == null ? null : getLabelForDayKey(dayKey, lang);
    }

    public String getDayKeyForDate(LocalDate date, LocalDate pascha, String lang) {
        String specialDayKey = resolveSpecialMovableDayKey(date, pascha);
        if (specialDayKey != null) {
            return specialDayKey;
        }

        return resolveLabelEntityForDate(date, pascha, lang)
                .map(this::ensureDayKey)
                .orElse(null);
    }

    public String getLabelForDayKey(String dayKey, String lang) {
        if (dayKey == null || dayKey.isBlank()) {
            return null;
        }

        String normalizedLang = normalizeLang(lang);
        Optional<LiturgicalLabel> storedLabel = findLabelByAnyKey(dayKey, normalizedLang);
        if (storedLabel.isPresent()) {
            return storedLabel.get().getText();
        }

        DayKeyParts parts = parseDayKey(dayKey);
        if (parts != null) {
            Optional<LiturgicalLabel> seasonalLabel = parts.isSunday()
                    ? findSundayLabel(parts.season(), parts.weekIndex(), normalizedLang)
                    : findDayLabel(parts.season(), parts.weekIndex(), parts.dayOfWeek(), normalizedLang);
            if (seasonalLabel.isPresent()) {
                return seasonalLabel.get().getText();
            }
        }

        return buildNeutralDisplayText(dayKey);
    }

    public Optional<LiturgicalLabel> resolveLabelEntityForDate(LocalDate date, LocalDate pascha, String lang) {
        String normalizedLang = normalizeLang(lang);
        String specialDayKey = resolveSpecialMovableDayKey(date, pascha);
        if (specialDayKey != null) {
            return buildSpecialLabel(specialDayKey, normalizedLang);
        }

        DayOfWeek day = date.getDayOfWeek();
        LocalDate previousPascha = paschaDateCalculator.getPaschaDate(date.getYear() - 1);
        LocalDate pentecostSunday = pascha.plusDays(49);
        LocalDate previousPentecostSunday = previousPascha.plusDays(49);
        LocalDate triodionStart = pascha.minusWeeks(10);

        boolean isInTriodion = !date.isBefore(triodionStart) && date.isBefore(pascha);
        boolean isInPascha = !date.isBefore(pascha) && date.isBefore(pentecostSunday);

        Optional<LiturgicalLabel> specialLabel = findSpecialLabelForDate(date, pascha, pentecostSunday, normalizedLang);
        if (specialLabel.isPresent()) {
            return specialLabel;
        }

        if (date.equals(pentecostSunday) && day == DayOfWeek.SUNDAY) {
            return resolvePentecostSundayLabel(0, normalizedLang);
        }

        // Legacy fallback only. In the Antiochian workflow this day should normally be handled
        // by SATURDAY_OF_SOULS_BEFORE_PENTECOST above.
        if (date.equals(pentecostSunday.minusDays(1)) && day == DayOfWeek.SATURDAY) {
            return findDayLabel("pentecost", 0, "saturday", normalizedLang);
        }

        if (isInTriodion) {
            int weekIndex = (int) ChronoUnit.WEEKS.between(triodionStart, date);
            return day == DayOfWeek.SUNDAY
                    ? findSundayLabel("triodion", weekIndex, normalizedLang)
                    : findDayLabel("triodion", weekIndex, day.name().toLowerCase(Locale.ROOT), normalizedLang);
        }

        if (isInPascha) {
            int weekIndex = (int) ChronoUnit.WEEKS.between(pascha, date);
            return day == DayOfWeek.SUNDAY
                    ? findSundayLabel("pascha", weekIndex, normalizedLang)
                    : findDayLabel("pascha", weekIndex, day.name().toLowerCase(Locale.ROOT), normalizedLang);
        }

        // Outside Triodion/Pascha:
        // - dates after this year's Pentecost continue from this year's Pentecost cycle
        // - dates before this year's Triodion continue from the previous year's Pentecost cycle
        if (!date.isBefore(pentecostSunday.plusDays(1))) {
            int weekIndex = countSundaysBetween(pentecostSunday, date);
            return day == DayOfWeek.SUNDAY
                    ? resolvePentecostSundayLabel(weekIndex, normalizedLang)
                    : findDayLabel("pentecost", weekIndex, day.name().toLowerCase(Locale.ROOT), normalizedLang);
        }

        int weekIndex = countSundaysBetween(previousPentecostSunday, date);
        return day == DayOfWeek.SUNDAY
                ? resolvePentecostSundayLabel(weekIndex, normalizedLang)
                : findDayLabel("pentecost", weekIndex, day.name().toLowerCase(Locale.ROOT), normalizedLang);
    }

    public List<LiturgicalLabel> getLabelsByLang(String lang) {
        return labelRepository.findByLang(normalizeLang(lang));
    }

    public LiturgicalLabel getLabelByKeyAndLang(String key, String lang) {
        return findLabelByLabelKeyOnly(key, normalizeLang(lang)).orElse(null);
    }

    public LiturgicalLabel getLabelByDayKeyAndLang(String dayKey, String lang) {
        String normalizedLang = normalizeLang(lang);
        Optional<LiturgicalLabel> stored = findLabelByAnyKey(dayKey, normalizedLang);
        if (stored.isPresent()) {
            return stored.get();
        }

        String text = getLabelForDayKey(dayKey, normalizedLang);
        if (text == null) {
            return null;
        }

        DayKeyParts parts = parseDayKey(dayKey);
        String type = parts == null ? "special" : (parts.isSunday() ? "sunday" : "weekday");
        String labelKey = parts == null ? dayKey : buildLegacyLabelKey(parts);

        return LiturgicalLabel.builder()
                .dayKey(dayKey)
                .labelKey(labelKey)
                .lang(normalizedLang)
                .text(text)
                .type(type)
                .season(parts != null ? parts.season() : null)
                .weekIndex(parts != null ? parts.weekIndex() : null)
                .dayOfWeek(parts != null && !parts.isSunday() ? parts.dayOfWeek() : null)
                .build();
    }

    public List<LiturgicalLabel> getAllLabels() {
        return labelRepository.findAll();
    }

    public LiturgicalLabel saveLabel(LiturgicalLabel label) {
        if (label.getDayKey() == null || label.getDayKey().isBlank()) {
            label.setDayKey(deriveDayKey(label));
        }
        if (label.getLang() != null) {
            label.setLang(normalizeLang(label.getLang()));
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

        LocalDate meatfareSoulSaturday = pascha.minusDays(57);
        if (date.equals(meatfareSoulSaturday)) {
            return findLabelByAnyKey(DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE, lang);
        }

        LocalDate pentecostSoulSaturday = pentecostSunday.minusDays(1);
        if (date.equals(pentecostSoulSaturday)) {
            return findLabelByAnyKey(DAY_KEY_SATURDAY_OF_SOULS_BEFORE_PENTECOST, lang);
        }

        return Optional.empty();
    }

    private Optional<LiturgicalLabel> findDayLabel(String season, int weekIndex, String dayOfWeek, String lang) {
        return findDayLabelExact(season, weekIndex, dayOfWeek, lang)
                .or(() -> fallbackDayLabelLookup(season, weekIndex, dayOfWeek, lang));
    }

    private Optional<LiturgicalLabel> findSundayLabel(String season, int weekIndex, String lang) {
        return findSundayLabelExact(season, weekIndex, lang)
                .or(() -> fallbackSundayLabelLookup(season, weekIndex, lang));
    }

    private Optional<LiturgicalLabel> resolvePentecostSundayLabel(int weekIndex, String lang) {
        if (weekIndex < 0) {
            return Optional.empty();
        }
        return findSundayLabel("pentecost", weekIndex, lang);
    }

    private Optional<LiturgicalLabel> findDayLabelExact(String season, int weekIndex, String dayOfWeek, String lang) {
        return labelRepository.findByTypeAndSeasonAndWeekIndexAndDayOfWeekAndLang(
                "weekday", season, weekIndex, dayOfWeek.toLowerCase(Locale.ROOT), lang
        );
    }

    private Optional<LiturgicalLabel> fallbackDayLabelLookup(String season, int weekIndex, String dayOfWeek, String requestedLang) {
        if (!SECONDARY_FALLBACK_LANG.equals(requestedLang)) {
            Optional<LiturgicalLabel> english = findDayLabelExact(season, weekIndex, dayOfWeek, SECONDARY_FALLBACK_LANG);
            if (english.isPresent()) {
                return english;
            }
        }
        if (!DEFAULT_LABEL_LANG.equals(requestedLang)) {
            return findDayLabelExact(season, weekIndex, dayOfWeek, DEFAULT_LABEL_LANG);
        }
        return Optional.empty();
    }

    private Optional<LiturgicalLabel> findSundayLabelExact(String season, int weekIndex, String lang) {
        Optional<LiturgicalLabel> label = labelRepository.findByTypeAndSeasonAndWeekIndexAndDayOfWeekIsNullAndLang(
                "sunday", season, weekIndex, lang
        );
        return label.isPresent()
                ? label
                : labelRepository.findByTypeAndSeasonAndWeekIndexAndDayOfWeekAndLang("sunday", season, weekIndex, "", lang);
    }

    private Optional<LiturgicalLabel> fallbackSundayLabelLookup(String season, int weekIndex, String requestedLang) {
        if (!SECONDARY_FALLBACK_LANG.equals(requestedLang)) {
            Optional<LiturgicalLabel> english = findSundayLabelExact(season, weekIndex, SECONDARY_FALLBACK_LANG);
            if (english.isPresent()) {
                return english;
            }
        }
        if (!DEFAULT_LABEL_LANG.equals(requestedLang)) {
            return findSundayLabelExact(season, weekIndex, DEFAULT_LABEL_LANG);
        }
        return Optional.empty();
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

    private String resolveSpecialMovableDayKey(LocalDate date, LocalDate pascha) {
        if (date.equals(pascha.minusDays(57))) return DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE;
        if (date.equals(pascha.minusDays(8))) return DAY_KEY_LAZARUS_SATURDAY;
        if (date.equals(pascha.minusDays(7))) return DAY_KEY_PALM_SUNDAY;
        if (date.equals(pascha.minusDays(6))) return DAY_KEY_HOLY_MONDAY;
        if (date.equals(pascha.minusDays(5))) return DAY_KEY_HOLY_TUESDAY;
        if (date.equals(pascha.minusDays(4))) return DAY_KEY_HOLY_WEDNESDAY;
        if (date.equals(pascha.minusDays(3))) return DAY_KEY_HOLY_THURSDAY;
        if (date.equals(pascha.minusDays(2))) return DAY_KEY_HOLY_FRIDAY;
        if (date.equals(pascha.minusDays(1))) return DAY_KEY_HOLY_SATURDAY;
        if (date.equals(pascha)) return DAY_KEY_PASCHA_SUNDAY;
        if (date.equals(pascha.plusDays(6))) return DAY_KEY_RENEWAL_SATURDAY;
        if (date.equals(pascha.plusDays(7))) return DAY_KEY_THOMAS_SUNDAY;
        if (date.equals(pascha.plusDays(24))) return DAY_KEY_MID_PENTECOST;
        if (date.equals(pascha.plusDays(39))) return DAY_KEY_ASCENSION;
        if (date.equals(pascha.plusDays(49))) return DAY_KEY_PENTECOST_SUNDAY;
        if (date.equals(pascha.plusDays(50))) return DAY_KEY_MONDAY_OF_HOLY_SPIRIT;
        if (date.equals(pascha.plusDays(56))) return DAY_KEY_ALL_SAINTS_SUNDAY;
        return null;
    }

    private Optional<LiturgicalLabel> buildSpecialLabel(String dayKey, String lang) {
        Optional<LiturgicalLabel> stored = findLabelByAnyKey(dayKey, lang);
        if (stored.isPresent()) {
            return stored;
        }

        return Optional.of(
                LiturgicalLabel.builder()
                        .dayKey(dayKey)
                        .labelKey(dayKey)
                        .lang(lang)
                        .text(buildNeutralDisplayText(dayKey))
                        .type("special")
                        .build()
        );
    }

    private Optional<LiturgicalLabel> findLabelByAnyKey(String dayKey, String requestedLang) {
        Optional<LiturgicalLabel> exact = findLabelByAnyKeyExact(dayKey, requestedLang);
        if (exact.isPresent()) {
            return exact;
        }

        if (!SECONDARY_FALLBACK_LANG.equals(requestedLang)) {
            Optional<LiturgicalLabel> english = findLabelByAnyKeyExact(dayKey, SECONDARY_FALLBACK_LANG);
            if (english.isPresent()) {
                return english;
            }
        }

        if (!DEFAULT_LABEL_LANG.equals(requestedLang)) {
            Optional<LiturgicalLabel> defaultLang = findLabelByAnyKeyExact(dayKey, DEFAULT_LABEL_LANG);
            if (defaultLang.isPresent()) {
                return defaultLang;
            }
        }

        return Optional.empty();
    }

    private Optional<LiturgicalLabel> findLabelByAnyKeyExact(String key, String lang) {
        Optional<LiturgicalLabel> byDayKey = labelRepository.findByDayKeyAndLang(key, lang);
        return byDayKey.isPresent()
                ? byDayKey
                : labelRepository.findByLabelKeyAndLang(key, lang);
    }

    private Optional<LiturgicalLabel> findLabelByLabelKeyOnly(String key, String requestedLang) {
        String normalizedLang = normalizeLang(requestedLang);
        Optional<LiturgicalLabel> exact = labelRepository.findByLabelKeyAndLang(key, normalizedLang);
        if (exact.isPresent()) {
            return exact;
        }
        if (!SECONDARY_FALLBACK_LANG.equals(normalizedLang)) {
            Optional<LiturgicalLabel> english = labelRepository.findByLabelKeyAndLang(key, SECONDARY_FALLBACK_LANG);
            if (english.isPresent()) {
                return english;
            }
        }
        if (!DEFAULT_LABEL_LANG.equals(normalizedLang)) {
            return labelRepository.findByLabelKeyAndLang(key, DEFAULT_LABEL_LANG);
        }
        return Optional.empty();
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return DEFAULT_LABEL_LANG;
        }

        Matcher matcher = LANGUAGE_TOKEN_PATTERN.matcher(lang.trim());
        if (matcher.find()) {
            return matcher.group(1).toLowerCase(Locale.ROOT);
        }

        return DEFAULT_LABEL_LANG;
    }

    private String buildNeutralDisplayText(String dayKey) {
        if (dayKey == null || dayKey.isBlank()) {
            return null;
        }

        String[] words = dayKey.toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                builder.append(word.substring(1));
            }
        }
        return builder.toString();
    }

    private record DayKeyParts(String season, String dayOfWeek, int weekIndex) {
        boolean isSunday() {
            return "sunday".equals(dayOfWeek);
        }
    }
}