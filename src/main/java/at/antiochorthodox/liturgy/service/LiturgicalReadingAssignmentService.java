package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;

import java.util.List;

/**
 * Compatibility facade kept for older code that still expects the old service
 * name. New code should inject {@code reading.v2.service.ReadingQueryService}
 * plus the v2 context service directly.
 */
@Deprecated
public interface LiturgicalReadingAssignmentService {

    List<ServiceReadingsDto> getAssignmentsForDay(LiturgicalDayContext context);

    List<ServiceReadingsDto> getAssignmentsForDayAndSlot(LiturgicalDayContext context, String slot);

    List<ServiceReadingsDto> getAssignmentsForService(LiturgicalDayContext context, String serviceKey);
}
