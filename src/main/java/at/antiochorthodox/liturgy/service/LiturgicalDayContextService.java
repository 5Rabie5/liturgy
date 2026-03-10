package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.model.LiturgicalDayReadingMap;
import at.antiochorthodox.liturgy.repository.LiturgicalDayReadingMapRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LiturgicalDayContextService {

    private static final String TRADITION = "ANTIOCHIAN";

    private final LiturgicalLabelService liturgicalLabelService;
    private final LiturgicalDayReadingMapRepository mapRepository;
    private final PaschaDateCalculator paschaDateCalculator;

    public LiturgicalDayContextService(
            LiturgicalLabelService liturgicalLabelService,
            LiturgicalDayReadingMapRepository mapRepository,
            PaschaDateCalculator paschaDateCalculator
    ) {
        this.liturgicalLabelService = liturgicalLabelService;
        this.mapRepository = mapRepository;
        this.paschaDateCalculator = paschaDateCalculator;
    }

    public LiturgicalDayContext resolveForDate(LocalDate date, String lang) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        String dayKey = liturgicalLabelService.getDayKeyForDate(date, pascha, lang);

        if (!hasText(dayKey)) {
            return null;
        }

        return resolveByDayKey(dayKey, lang);
    }

    public LiturgicalDayContext resolveByDayKey(String dayKey, String lang) {
        if (!hasText(dayKey)) {
            return null;
        }

        String dayLabel = liturgicalLabelService.getLabelForDayKey(dayKey, lang);
        LiturgicalDayReadingMap map =
                mapRepository.findByTraditionAndDayKey(TRADITION, dayKey).orElse(null);

        return LiturgicalDayContext.builder()
                .dayKey(dayKey)
                .dayLabel(hasText(dayLabel) ? dayLabel : dayKey)
                .epistleKey(map != null ? map.getEpistleKey() : null)
                .gospelKey(map != null ? map.getGospelKey() : null)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}