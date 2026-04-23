package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class ReadingDayKeyResolverImpl implements ReadingDayKeyResolver {

    private static final String DEFAULT_SLOT = "default";
    private static final String LITURGY_SLOT = "liturgy";
    private static final String MATINS_SLOT = "matins";

    private static final Map<String, String> LEGACY_TO_CANONICAL = Map.of(
            "GREAT_LENT_W01_SUNDAY", "TRIODION_W04_SUNDAY",
            "GREAT_LENT_W02_SUNDAY", "TRIODION_W05_SUNDAY",
            "GREAT_LENT_W03_SUNDAY", "TRIODION_W06_SUNDAY",
            "GREAT_LENT_W04_SUNDAY", "TRIODION_W07_SUNDAY",
            "GREAT_LENT_W05_SUNDAY", "TRIODION_W08_SUNDAY"
    );

    private static final Map<String, List<String>> LOOKUP_KEYS = Map.of(
            "TRIODION_W04_SUNDAY", List.of("TRIODION_W04_SUNDAY", "GREAT_LENT_W01_SUNDAY"),
            "TRIODION_W05_SUNDAY", List.of("TRIODION_W05_SUNDAY", "GREAT_LENT_W02_SUNDAY"),
            "TRIODION_W06_SUNDAY", List.of("TRIODION_W06_SUNDAY", "GREAT_LENT_W03_SUNDAY"),
            "TRIODION_W07_SUNDAY", List.of("TRIODION_W07_SUNDAY", "GREAT_LENT_W04_SUNDAY"),
            "TRIODION_W08_SUNDAY", List.of("TRIODION_W08_SUNDAY", "GREAT_LENT_W05_SUNDAY")
    );

    @Override
    public ReadingDayKeyResolution resolve(String calendarDayKey, LocalDate date, String slot) {
        String canonicalDayKey = canonicalize(calendarDayKey);
        List<String> lookupDayKeys = buildLookupDayKeys(canonicalDayKey, slot);

        return ReadingDayKeyResolution.builder()
                .calendarDayKey(calendarDayKey)
                .canonicalDayKey(canonicalDayKey)
                .resolvedDayKey(canonicalDayKey)
                .reason(canonicalDayKey == null
                        ? "No calendarDayKey resolved"
                        : "Resolved with canonical alias mapping")
                .lookupDayKeys(lookupDayKeys)
                .triedKeys(lookupDayKeys)
                .build();
    }

    @Override
    public List<String> resolveLookupDayKeys(String calendarDayKey, LocalDate date, String slot) {
        return buildLookupDayKeys(canonicalize(calendarDayKey), slot);
    }

    private String canonicalize(String dayKey) {
        if (!hasText(dayKey)) {
            return null;
        }
        return LEGACY_TO_CANONICAL.getOrDefault(dayKey, dayKey);
    }

    private List<String> buildLookupDayKeys(String canonicalDayKey, String slot) {
        if (!hasText(canonicalDayKey)) {
            return List.of();
        }

        List<String> specialAliases = specialLookupAliases(canonicalDayKey, slot);
        if (!specialAliases.isEmpty()) {
            return specialAliases;
        }

        return LOOKUP_KEYS.getOrDefault(canonicalDayKey, List.of(canonicalDayKey));
    }

    private List<String> specialLookupAliases(String canonicalDayKey, String slot) {
        String normalizedSlot = normalizeSlot(slot);
        LinkedHashSet<String> keys = new LinkedHashSet<>();

        switch (canonicalDayKey) {
            case LiturgicalLabelService.DAY_KEY_HOLY_MONDAY -> {
                if (MATINS_SLOT.equals(normalizedSlot)) {
                    keys.add("LYWM_ALATHNYN_ALAZYM_ALMQDS_FY_ALSHR");
                } else {
                    keys.add("LYWM_ALATHNYN_ALAZYM_ALMQDS_FY_ALQDAS");
                    if (normalizedSlot == null) {
                        keys.add("LYWM_ALATHNYN_ALAZYM_ALMQDS_FY_ALSHR");
                    }
                }
            }
            case LiturgicalLabelService.DAY_KEY_HOLY_TUESDAY -> {
                if (MATINS_SLOT.equals(normalizedSlot)) {
                    keys.add("LYWM_ALTHLATHAA_ALAZYM_ALMQDS_FY_ALSHR");
                } else {
                    keys.add("LYWM_ALTHLATHAA_ALAZYM_ALMQDS_FY_ALQDAS");
                    if (normalizedSlot == null) {
                        keys.add("LYWM_ALTHLATHAA_ALAZYM_ALMQDS_FY_ALSHR");
                    }
                }
            }
            case LiturgicalLabelService.DAY_KEY_HOLY_WEDNESDAY -> keys.add("HOLY_WEDNESDAY_MATINS");
            case LiturgicalLabelService.DAY_KEY_RENEWAL_SATURDAY -> keys.add("PASCHA_W01_SATURDAY");
            case LiturgicalLabelService.DAY_KEY_MID_PENTECOST -> keys.add("PASCHA_W03_WEDNESDAY");
            case LiturgicalLabelService.DAY_KEY_ASCENSION -> keys.add("PASCHA_W06_THURSDAY");
            case LiturgicalLabelService.DAY_KEY_MONDAY_OF_HOLY_SPIRIT -> keys.add("PENTECOST_W01_MONDAY");
            case LiturgicalLabelService.DAY_KEY_SATURDAY_OF_SOULS_BEFORE_MEATFARE,
                 LiturgicalLabelService.DAY_KEY_SATURDAY_OF_SOULS_BEFORE_PENTECOST -> keys.add("COMMON_DEPARTED");
            default -> {
            }
        }

        return List.copyOf(keys);
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
