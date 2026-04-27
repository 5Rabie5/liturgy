package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("transitional_sunday_overrides")
public class TransitionalSundayOverride {

    @Id
    private String id;

    private String tradition;
    private Integer year;
    private LocalDate date;

    private String periodKey;
    private String slot;

    private String sourceDayKey;

    private String selectedEpistleKey;
    private String selectedGospelKey;

    private String decisionBasis;
    private String sourceReference;
    private String notes;

    private Boolean enabled;
}