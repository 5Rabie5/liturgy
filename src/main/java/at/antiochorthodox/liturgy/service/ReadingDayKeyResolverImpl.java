package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReadingDayKeyResolverImpl implements ReadingDayKeyResolver {

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
        List<String> lookupDayKeys = buildLookupDayKeys(canonicalDayKey);

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
        return buildLookupDayKeys(canonicalize(calendarDayKey));
    }

    private String canonicalize(String dayKey) {
        if (!hasText(dayKey)) {
            return null;
        }
        return LEGACY_TO_CANONICAL.getOrDefault(dayKey, dayKey);
    }

    private List<String> buildLookupDayKeys(String canonicalDayKey) {
        if (!hasText(canonicalDayKey)) {
            return List.of();
        }
        return LOOKUP_KEYS.getOrDefault(canonicalDayKey, List.of(canonicalDayKey));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}