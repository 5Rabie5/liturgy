package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;

import java.util.List;

public interface LiturgicalReadingAssignmentService {

    List<ServiceReadingsDto> getAssignmentsForDay(LiturgicalDayContext context);

    List<ServiceReadingsDto> getAssignmentsForDayAndSlot(LiturgicalDayContext context, String slot);

    List<ServiceReadingsDto> getAssignmentsForService(LiturgicalDayContext context, String serviceKey);
}