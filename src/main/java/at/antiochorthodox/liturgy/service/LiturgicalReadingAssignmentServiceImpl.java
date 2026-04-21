package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;
import at.antiochorthodox.liturgy.reading.v2.service.LiturgicalReadingAssignmentQueryService;
import at.antiochorthodox.liturgy.reading.v2.service.ServiceReadingsAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Compatibility adapter around the split v2 services.
 *
 * <p>This class exists so older internal code can keep using the historical
 * service name while the real work now lives in dedicated v2 services.</p>
 */
@Service
@RequiredArgsConstructor
@Deprecated
public class LiturgicalReadingAssignmentServiceImpl implements LiturgicalReadingAssignmentService {

    private final LiturgicalReadingAssignmentQueryService liturgicalReadingAssignmentQueryService;
    private final ServiceReadingsAssembler serviceReadingsAssembler;

    @Override
    public List<ServiceReadingsDto> getAssignmentsForDay(LiturgicalDayContext context) {
        return assemble(liturgicalReadingAssignmentQueryService.findAssignmentsForDay(toReadingContext(context)));
    }

    @Override
    public List<ServiceReadingsDto> getAssignmentsForDayAndSlot(LiturgicalDayContext context, String slot) {
        return assemble(liturgicalReadingAssignmentQueryService.findAssignmentsForDayAndSlot(toReadingContext(context), slot));
    }

    @Override
    public List<ServiceReadingsDto> getAssignmentsForService(LiturgicalDayContext context, String serviceKey) {
        return assemble(liturgicalReadingAssignmentQueryService.findAssignmentsForService(toReadingContext(context), serviceKey));
    }

    private List<ServiceReadingsDto> assemble(List<LiturgicalReadingAssignment> assignments) {
        return serviceReadingsAssembler.assemble(assignments);
    }

    private ReadingContext toReadingContext(LiturgicalDayContext context) {
        if (context == null) {
            throw new IllegalArgumentException("LiturgicalDayContext must not be null");
        }

        return ReadingContext.builder()
                .tradition(context.getTradition())
                .calendarDayKey(context.getCalendarDayKey())
                .readingDayKey(firstNonBlank(context.getReadingDayKey(), context.getDayKey()))
                .dayKey(firstNonBlank(context.getDayKey(), context.getReadingDayKey()))
                .dayLabel(context.getDayLabel())
                .lookupDayKeys(null)
                .build();
    }

    private String firstNonBlank(String first, String second) {
        if (hasText(first)) {
            return first;
        }
        return second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
