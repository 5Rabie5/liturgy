package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;
import at.antiochorthodox.liturgy.reading.v2.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.reading.v2.model.LiturgicalReadingAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class V2ReadingQueryServiceImpl implements V2ReadingQueryService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final ReadingContextService readingContextService;
    private final LiturgicalReadingAssignmentService liturgicalReadingAssignmentService;
    private final ServiceReadingsAssembler serviceReadingsAssembler;

    @Override
    public List<ServiceReadingsDto> getByDate(LocalDate date, String lang, String tradition) {
        ReadingContext context = resolveContext(date, lang, tradition);
        List<LiturgicalReadingAssignment> assignments = liturgicalReadingAssignmentService.findAssignmentsForDay(context);
        return serviceReadingsAssembler.assemble(assignments);
    }

    @Override
    public List<ServiceReadingsDto> getByDateAndSlot(LocalDate date, String slot, String lang, String tradition) {
        ReadingContext context = resolveContext(date, lang, tradition, slot);
        List<LiturgicalReadingAssignment> assignments = liturgicalReadingAssignmentService.findAssignmentsForDayAndSlot(context, slot);
        return serviceReadingsAssembler.assemble(assignments);
    }

    @Override
    public List<ServiceReadingsDto> getByDateAndService(LocalDate date, String serviceKey, String lang, String tradition) {
        ReadingContext context = resolveContext(date, lang, tradition);
        List<LiturgicalReadingAssignment> assignments = liturgicalReadingAssignmentService.findAssignmentsForService(context, serviceKey);
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

        if (context.getTradition() == null || context.getTradition().isBlank()) {
            context.setTradition(hasText(tradition) ? tradition : DEFAULT_TRADITION);
        }

        return context;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
