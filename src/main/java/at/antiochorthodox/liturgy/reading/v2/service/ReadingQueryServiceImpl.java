package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class ReadingQueryServiceImpl implements ReadingQueryService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final ReadingContextService readingContextService;
    private final LiturgicalReadingAssignmentQueryService liturgicalReadingAssignmentQueryService;
    private final ServiceReadingsAssembler serviceReadingsAssembler;

    @Override
    public List<ServiceReadingsDto> getByDate(LocalDate date, String lang, String tradition) {
        ReadingContext context = resolveContext(date, lang, tradition);
        List<LiturgicalReadingAssignment> assignments = liturgicalReadingAssignmentQueryService.findAssignmentsForDay(context);
        return serviceReadingsAssembler.assemble(assignments);
    }

    @Override
    public List<ServiceReadingsDto> getByDateAndSlot(LocalDate date, String slot, String lang, String tradition) {
        ReadingContext context = resolveContext(date, lang, tradition, slot);
        List<LiturgicalReadingAssignment> assignments =
                liturgicalReadingAssignmentQueryService.findAssignmentsForDayAndSlot(context, slot);
        return serviceReadingsAssembler.assemble(assignments);
    }

    @Override
    public List<ServiceReadingsDto> getByDateAndService(LocalDate date, String serviceKey, String lang, String tradition) {
        ReadingContext context = resolveContext(date, lang, tradition);
        List<LiturgicalReadingAssignment> assignments =
                liturgicalReadingAssignmentQueryService.findAssignmentsForService(context, serviceKey);
        return serviceReadingsAssembler.assemble(assignments);
    }

    private ReadingContext resolveContext(LocalDate date, String lang, String tradition) {
        ReadingContext context = readingContextService.resolveForDate(date, lang, tradition);
        return requireResolvedContext(context, date, tradition);
    }

    private ReadingContext resolveContext(LocalDate date, String lang, String tradition, String slot) {
        ReadingContext context = readingContextService.resolveForDate(date, lang, tradition, slot);
        return requireResolvedContext(context, date, tradition);
    }

    private ReadingContext requireResolvedContext(ReadingContext context, LocalDate date, String tradition) {
        if (context == null) {
            throw new IllegalStateException("Could not resolve ReadingContext for date: " + date);
        }

        if (!hasText(context.getTradition())) {
            context.setTradition(hasText(tradition) ? tradition : DEFAULT_TRADITION);
        }

        return context;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
