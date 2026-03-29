package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import at.antiochorthodox.liturgy.model.LiturgicalDayReadingMap;
import at.antiochorthodox.liturgy.repository.LiturgicalDayReadingMapRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LiturgicalDayContextService {

    private static final String TRADITION = "ANTIOCHIAN";
    private static final String DEFAULT_SLOT = "default";
    private static final String LITURGY_SLOT = "liturgy";

    private final LiturgicalLabelService liturgicalLabelService;
    private final LiturgicalDayReadingMapRepository mapRepository;
    private final PaschaDateCalculator paschaDateCalculator;
    private final ReadingDayKeyResolver readingDayKeyResolver;

    public LiturgicalDayContextService(
            LiturgicalLabelService liturgicalLabelService,
            LiturgicalDayReadingMapRepository mapRepository,
            PaschaDateCalculator paschaDateCalculator,
            ReadingDayKeyResolver readingDayKeyResolver
    ) {
        this.liturgicalLabelService = liturgicalLabelService;
        this.mapRepository = mapRepository;
        this.paschaDateCalculator = paschaDateCalculator;
        this.readingDayKeyResolver = readingDayKeyResolver;
    }

    public LiturgicalDayContext resolveForDate(LocalDate date, String lang) {
        return resolveForDate(date, lang, null);
    }

    public LiturgicalDayContext resolveForDate(LocalDate date, String lang, String slot) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());

        String calendarDayKey = liturgicalLabelService.getDayKeyForDate(date, pascha, lang);

        ReadingDayKeyResolution resolution =
                readingDayKeyResolver.resolve(calendarDayKey, date, slot);

        String resolvedDayKey = resolution.getResolvedDayKey();

        if (!hasText(resolvedDayKey)) {
            return null;
        }

        LiturgicalDayContext context = resolveByDayKey(resolvedDayKey, lang, slot);
        if (context == null) {
            return null;
        }

        // أبقهما فقط إذا أضفتهما إلى LiturgicalDayContext
        context.setCalendarDayKey(calendarDayKey);
        context.setReadingDayKey(resolvedDayKey);

        return context;
    }

    public LiturgicalDayContext resolveByDayKey(String dayKey, String lang) {
        return resolveByDayKey(dayKey, lang, null);
    }

    public LiturgicalDayContext resolveByDayKey(String dayKey, String lang, String slot) {
        if (!hasText(dayKey)) {
            return null;
        }

        String dayLabel = liturgicalLabelService.getLabelForDayKey(dayKey, lang);
        LiturgicalDayReadingMap map = resolveMap(dayKey, slot);

        return LiturgicalDayContext.builder()
                .dayKey(dayKey)
                .dayLabel(hasText(dayLabel) ? dayLabel : dayKey)
                .slot(map != null ? map.getSlot() : normalizeSlot(slot))
                .sourceType(map != null ? map.getSourceType() : null)
                .epistleKey(map != null ? map.getEpistleKey() : null)
                .gospelKey(map != null ? map.getGospelKey() : null)
                .build();
    }

    private LiturgicalDayReadingMap resolveMap(String dayKey, String slot) {
        String normalizedSlot = normalizeSlot(slot);

        if (hasText(normalizedSlot)) {
            LiturgicalDayReadingMap exact =
                    mapRepository.findByTraditionAndDayKeyAndSlot(TRADITION, dayKey, normalizedSlot).orElse(null);
            if (exact != null) {
                return exact;
            }
        }

        LiturgicalDayReadingMap defaultEntry =
                mapRepository.findByTraditionAndDayKeyAndSlot(TRADITION, dayKey, DEFAULT_SLOT).orElse(null);
        if (defaultEntry != null) {
            return defaultEntry;
        }

        LiturgicalDayReadingMap liturgyEntry =
                mapRepository.findByTraditionAndDayKeyAndSlot(TRADITION, dayKey, LITURGY_SLOT).orElse(null);
        if (liturgyEntry != null) {
            return liturgyEntry;
        }

        return mapRepository.findFirstByTraditionAndDayKeyOrderBySlotAsc(TRADITION, dayKey).orElse(null);
    }

    private String normalizeSlot(String slot) {
        return hasText(slot) ? slot.trim().toLowerCase() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}