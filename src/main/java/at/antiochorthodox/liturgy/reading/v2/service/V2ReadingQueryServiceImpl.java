package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * @deprecated Prefer injecting {@link ReadingQueryService}. This adapter keeps
 * older wiring alive without duplicating the real implementation.
 */
@Service
@RequiredArgsConstructor
@Deprecated
public class V2ReadingQueryServiceImpl implements V2ReadingQueryService {

    private final ReadingQueryServiceImpl readingQueryService;

    @Override
    public List<ServiceReadingsDto> getByDate(LocalDate date, String lang, String tradition) {
        return readingQueryService.getByDate(date, lang, tradition);
    }

    @Override
    public List<ServiceReadingsDto> getByDateAndSlot(LocalDate date, String slot, String lang, String tradition) {
        return readingQueryService.getByDateAndSlot(date, slot, lang, tradition);
    }

    @Override
    public List<ServiceReadingsDto> getByDateAndService(LocalDate date, String serviceKey, String lang, String tradition) {
        return readingQueryService.getByDateAndService(date, serviceKey, lang, tradition);
    }
}
