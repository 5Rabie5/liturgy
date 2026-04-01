package at.antiochorthodox.liturgy.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadingAssignmentItemDto {

    private String readingType;
    private String readingKey;
    private Integer sequence;
    private String usage;
    private Boolean primaryAssignment;

    /**
     * Hydrated reading document:
     * EpistleReading or GospelReading
     */
    private Object reading;
}