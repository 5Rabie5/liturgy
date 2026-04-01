package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.reading.v2.dto.ReadingAssignmentItemDto;
import at.antiochorthodox.liturgy.reading.v2.dto.ServiceReadingsDto;
import at.antiochorthodox.liturgy.reading.v2.model.LiturgicalReadingAssignment;
import at.antiochorthodox.liturgy.repository.EpistleReadingRepository;
import at.antiochorthodox.liturgy.repository.GospelReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceReadingsAssemblerImpl implements ServiceReadingsAssembler {

    private final EpistleReadingRepository epistleReadingRepository;
    private final GospelReadingRepository gospelReadingRepository;

    @Override
    public List<ServiceReadingsDto> assemble(List<LiturgicalReadingAssignment> assignments) {
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
                    .collect(Collectors.toList());

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

        if ("epistle".equals(readingType)) {
            return epistleReadingRepository.findByReadingKey(readingKey).orElse(null);
        }

        if ("gospel".equals(readingType)) {
            return gospelReadingRepository.findByReadingKey(readingKey).orElse(null);
        }

        return null;
    }
}
