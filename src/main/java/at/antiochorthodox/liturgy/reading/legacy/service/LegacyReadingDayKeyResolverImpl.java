package at.antiochorthodox.liturgy.reading.legacy.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import at.antiochorthodox.liturgy.reading.legacy.repository.LiturgicalDayReadingMapRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LegacyReadingDayKeyResolverImpl implements LegacyReadingDayKeyResolver {

    private static final String TRADITION = "ANTIOCHIAN";

    private static final Map<String, String> EXPLICIT_ALIASES = Map.of(
            "TRIODION_W08_SUNDAY", "GREAT_LENT_W05_SUNDAY"
    );

    private final LiturgicalDayReadingMapRepository mapRepository;

    public LegacyReadingDayKeyResolverImpl(LiturgicalDayReadingMapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    @Override
    public ReadingDayKeyResolution resolve(String calendarDayKey, LocalDate date, String slot) {
        List<String> tried = new ArrayList<>();

        if (!hasText(calendarDayKey)) {
            return ReadingDayKeyResolution.builder()
                    .calendarDayKey(calendarDayKey)
                    .resolvedDayKey(null)
                    .reason("blank")
                    .triedKeys(tried)
                    .build();
        }

        String normalizedSlot = normalizeSlot(slot);

        tried.add(calendarDayKey);
        if (existsInMap(calendarDayKey, normalizedSlot)) {
            return ReadingDayKeyResolution.builder()
                    .calendarDayKey(calendarDayKey)
                    .resolvedDayKey(calendarDayKey)
                    .reason("direct")
                    .triedKeys(tried)
                    .build();
        }

        String alias = EXPLICIT_ALIASES.get(calendarDayKey);
        if (hasText(alias)) {
            tried.add(alias);
            if (existsInMap(alias, normalizedSlot)) {
                return ReadingDayKeyResolution.builder()
                        .calendarDayKey(calendarDayKey)
                        .resolvedDayKey(alias)
                        .reason("explicit_alias")
                        .triedKeys(tried)
                        .build();
            }
        }

        String ruleBased = normalizeByRule(calendarDayKey);
        if (hasText(ruleBased) && !ruleBased.equals(calendarDayKey) && !tried.contains(ruleBased)) {
            tried.add(ruleBased);
            if (existsInMap(ruleBased, normalizedSlot)) {
                return ReadingDayKeyResolution.builder()
                        .calendarDayKey(calendarDayKey)
                        .resolvedDayKey(ruleBased)
                        .reason("rule_based")
                        .triedKeys(tried)
                        .build();
            }
        }

        return ReadingDayKeyResolution.builder()
                .calendarDayKey(calendarDayKey)
                .resolvedDayKey(calendarDayKey)
                .reason("unresolved_fallback")
                .triedKeys(tried)
                .build();
    }

    private String normalizeByRule(String key) {
        return key;
    }

    private boolean existsInMap(String dayKey, String slot) {
        if (hasText(slot) && mapRepository.findByTraditionAndDayKeyAndSlot(TRADITION, dayKey, slot).isPresent()) {
            return true;
        }
        return mapRepository.findFirstByTraditionAndDayKeyOrderBySlotAsc(TRADITION, dayKey).isPresent();
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
