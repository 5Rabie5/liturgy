package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import at.antiochorthodox.liturgy.repository.LiturgicalDayReadingMapRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReadingDayKeyResolverImpl implements ReadingDayKeyResolver {

    private static final String TRADITION = "ANTIOCHIAN";

    private final LiturgicalDayReadingMapRepository mapRepository;

    private static final Map<String, String> EXPLICIT_ALIASES = Map.of(
            "TRIODION_W08_SUNDAY", "GREAT_LENT_W05_SUNDAY"
    );

    public ReadingDayKeyResolverImpl(LiturgicalDayReadingMapRepository mapRepository) {
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

        // 1) direct match
        tried.add(calendarDayKey);
        if (exists(calendarDayKey, normalizedSlot)) {
            return ReadingDayKeyResolution.builder()
                    .calendarDayKey(calendarDayKey)
                    .resolvedDayKey(calendarDayKey)
                    .reason("direct")
                    .triedKeys(tried)
                    .build();
        }

        // 2) explicit aliases
        String alias = EXPLICIT_ALIASES.get(calendarDayKey);
        if (hasText(alias)) {
            tried.add(alias);
            if (exists(alias, normalizedSlot)) {
                return ReadingDayKeyResolution.builder()
                        .calendarDayKey(calendarDayKey)
                        .resolvedDayKey(alias)
                        .reason("explicit_alias")
                        .triedKeys(tried)
                        .build();
            }
        }

        // 3) rule-based normalization
        String ruleBased = normalizeByRule(calendarDayKey);
        if (hasText(ruleBased) && !ruleBased.equals(calendarDayKey) && !tried.contains(ruleBased)) {
            tried.add(ruleBased);
            if (exists(ruleBased, normalizedSlot)) {
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
        // مؤقتًا نبدأ صغيرًا وآمنًا
        // لاحقًا يمكن توسيع القواعد
        return key;
    }

    private boolean exists(String dayKey, String slot) {
        if (hasText(slot) && mapRepository.findByTraditionAndDayKeyAndSlot(TRADITION, dayKey, slot).isPresent()) {
            return true;
        }
        return mapRepository.findFirstByTraditionAndDayKeyOrderBySlotAsc(TRADITION, dayKey).isPresent();
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase() : null;
    }

    private boolean hasText(String v) {
        return v != null && !v.isBlank();
    }
}