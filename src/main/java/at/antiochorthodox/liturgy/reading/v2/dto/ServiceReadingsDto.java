package at.antiochorthodox.liturgy.reading.v2.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ServiceReadingsDto {

    private String dayKey;
    private String calendarDayKey;
    private String slot;
    private String serviceKey;
    private String sourceType;
    private String groupKey;

    private List<ReadingAssignmentItemDto> readings;
}