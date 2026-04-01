package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.dto.ReadingAssignmentItemDto;
import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.repository.EpistleReadingRepository;
import at.antiochorthodox.liturgy.repository.GospelReadingRepository;
import at.antiochorthodox.liturgy.repository.LiturgicalReadingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiturgicalReadingAssignmentServiceImpl implements LiturgicalReadingAssignmentService {

    private static final String DEFAULT_TRADITION = "ANTIOCHIAN";

    private final LiturgicalReadingAssignmentRepository assignmentRepository;
    private final EpistleReadingRepository epistleReadingRepository;
    private final GospelReadingRepository gospelReadingRepository;

    @Override
    public List<ServiceReadingsDto> getAssignmentsForDay(LiturgicalDayContext context) {
        String tradition = resolveTradition(context);
        String dayKey = resolveReadingDayKey(context);

        List<LiturgicalReadingAssignment> assignments =
                assignmentRepository.findByTraditionAndDayKeyOrderByServiceKeyAscSequenceAsc(
                        tradition,
                        dayKey
                );

        return groupAndHydrate(assignments);
    }

    @Override
    public List<ServiceReadingsDto> getAssignmentsForDayAndSlot(LiturgicalDayContext context, String slot) {
        String tradition = resolveTradition(context);
        String dayKey = resolveReadingDayKey(context);

        List<LiturgicalReadingAssignment> assignments =
                assignmentRepository.findByTraditionAndDayKeyAndSlotOrderByServiceKeyAscSequenceAsc(
                        tradition,
                        dayKey,
                        slot
                );

        return groupAndHydrate(assignments);
    }

    @Override
    public List<ServiceReadingsDto> getAssignmentsForService(LiturgicalDayContext context, String serviceKey) {
        String tradition = resolveTradition(context);
        String dayKey = resolveReadingDayKey(context);

        List<LiturgicalReadingAssignment> assignments =
                assignmentRepository.findByTraditionAndDayKeyAndServiceKeyOrderBySequenceAsc(
                        tradition,
                        dayKey,
                        serviceKey
                );

        return groupAndHydrate(assignments);
    }

    private String resolveTradition(LiturgicalDayContext context) {
        if (context == null || context.getTradition() == null || context.getTradition().isBlank()) {
            return DEFAULT_TRADITION;
        }
        return context.getTradition();
    }

    private String resolveReadingDayKey(LiturgicalDayContext context) {
        if (context == null) {
            throw new IllegalArgumentException("LiturgicalDayContext must not be null");
        }

        if (context.getReadingDayKey() != null && !context.getReadingDayKey().isBlank()) {
            return context.getReadingDayKey();
        }

        if (context.getDayKey() != null && !context.getDayKey().isBlank()) {
            return context.getDayKey();
        }

        throw new IllegalStateException("No readingDayKey/dayKey found in LiturgicalDayContext");
    }

    private List<ServiceReadingsDto> groupAndHydrate(List<LiturgicalReadingAssignment> assignments) {
        Map<String, List<LiturgicalReadingAssignment>> grouped = new LinkedHashMap<>();

        for (LiturgicalReadingAssignment assignment : assignments) {
            grouped.computeIfAbsent(assignment.getServiceKey(), k -> new ArrayList<>()).add(assignment);
        }

        List<ServiceReadingsDto> result = new ArrayList<>();

        for (Map.Entry<String, List<LiturgicalReadingAssignment>> entry : grouped.entrySet()) {
            List<LiturgicalReadingAssignment> serviceAssignments = entry.getValue();
            LiturgicalReadingAssignment first = serviceAssignments.get(0);

            List<ReadingAssignmentItemDto> readings = serviceAssignments.stream()
                    .map(this::toReadingItemDto)
                    .toList();

            result.add(ServiceReadingsDto.builder()
                    .dayKey(first.getDayKey())
                    .calendarDayKey(first.getCalendarDayKey())
                    .slot(first.getSlot())
                    .serviceKey(first.getServiceKey())
                    .sourceType(first.getSourceType())
                    .groupKey(first.getGroupKey())
                    .readings(readings)
                    .build());
        }

        return result;
    }

    private ReadingAssignmentItemDto toReadingItemDto(LiturgicalReadingAssignment assignment) {
        Object hydratedReading = hydrateReading(assignment.getReadingType(), assignment.getReadingKey());

        return ReadingAssignmentItemDto.builder()
                .readingType(assignment.getReadingType())
                .readingKey(assignment.getReadingKey())
                .sequence(assignment.getSequence())
                .usage(assignment.getUsage())
                .primaryAssignment(assignment.getPrimaryAssignment())
                .reading(hydratedReading)
                .build();
    }

    private Object hydrateReading(String readingType, String readingKey) {
        if (readingType == null || readingKey == null) {
            return null;
        }

        return switch (readingType) {
            case "epistle" -> epistleReadingRepository.findByReadingKey(readingKey).orElse(null);
            case "gospel" -> gospelReadingRepository.findByReadingKey(readingKey).orElse(null);
            default -> null;
        };
    }
}