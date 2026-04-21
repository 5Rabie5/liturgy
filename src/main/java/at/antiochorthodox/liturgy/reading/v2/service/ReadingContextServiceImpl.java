package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.dto.ReadingDayKeyResolution;
import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;
import at.antiochorthodox.liturgy.service.LiturgicalLabelService;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReadingContextServiceImpl implements ReadingContextService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final LiturgicalLabelService liturgicalLabelService;
    private final PaschaDateCalculator paschaDateCalculator;
    private final AssignmentReadingDayKeyResolver readingDayKeyResolver;

    @Override
    public ReadingContext resolveForDate(LocalDate date, String lang, String tradition) {
        return resolveForDate(date, lang, tradition, null);
    }

    @Override
    public ReadingContext resolveForDate(LocalDate date, String lang, String tradition, String slot) {
        if (date == null) {
            return null;
        }

        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        String calendarDayKey = liturgicalLabelService.getDayKeyForDate(date, pascha, lang);

        ReadingDayKeyResolution resolution =
                readingDayKeyResolver.resolve(calendarDayKey, date, slot);

        String canonicalDayKey = resolution != null ? resolution.getCanonicalDayKey() : null;
        String readingDayKey = resolution != null ? resolution.getResolvedDayKey() : null;
        if (!hasText(readingDayKey)) {
            return null;
        }

        String outwardDayKey = hasText(canonicalDayKey) ? canonicalDayKey : readingDayKey;
        String dayLabel = liturgicalLabelService.getLabelForDayKey(outwardDayKey, lang);
        String resolvedTradition = hasText(tradition) ? tradition : DEFAULT_TRADITION;

        return ReadingContext.builder()
                .tradition(resolvedTradition)
                .calendarDayKey(calendarDayKey)
                .readingDayKey(readingDayKey)
                .dayKey(outwardDayKey)
                .dayLabel(hasText(dayLabel) ? dayLabel : outwardDayKey)
                .lookupDayKeys(resolution != null ? resolution.getLookupDayKeys() : null)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
