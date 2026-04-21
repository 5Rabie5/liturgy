package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.reading.v2.dto.ReadingContext;

import java.util.List;

public interface LiturgicalReadingAssignmentQueryService {

    List<LiturgicalReadingAssignment> findAssignmentsForDay(ReadingContext context);

    List<LiturgicalReadingAssignment> findAssignmentsForDayAndSlot(ReadingContext context, String slot);

    List<LiturgicalReadingAssignment> findAssignmentsForService(ReadingContext context, String serviceKey);
}
