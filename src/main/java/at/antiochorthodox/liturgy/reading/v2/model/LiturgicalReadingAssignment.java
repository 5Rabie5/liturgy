package at.antiochorthodox.liturgy.reading.v2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "liturgical_reading_assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiturgicalReadingAssignment {

    @Id
    private String id;

    /**
     * ANTIOCHIAN, BYZANTINE, etc.
     */
    private String tradition;

    /**
     * Canonical reading day key after normalization,
     * e.g. HOLY_THURSDAY, GREAT_LENT_W05_SUNDAY, FIXED_01_06
     */
    private String dayKey;

    /**
     * Calendar day key before normalization if needed,
     * e.g. TRIODION_W08_SUNDAY
     */
    private String calendarDayKey;

    /**
     * liturgy, vespers, matins, hour_01, hour_03, hour_06, hour_09, default...
     */
    private String slot;

    /**
     * Stable service identity,
     * e.g. HOLY_THURSDAY_LITURGY, HOLY_THURSDAY_VESPERS, HOLY_FRIDAY_HOUR_06
     */
    private String serviceKey;

    /**
     * day, fixed_feast, saint, resurrection, hour, service
     */
    private String sourceType;

    /**
     * epistle or gospel
     */
    private String readingType;

    /**
     * EP_0304, MARK_LENT_SUNDAY_5, etc.
     */
    private String readingKey;

    /**
     * Global order inside the same service.
     * Examples:
     * liturgy: epistle=1, gospel=2
     * passion gospels: 1..12
     * holy friday hours: 1..n
     */
    private Integer sequence;

    /**
     * Optional grouping identifier for multi-reading services,
     * e.g. PASSION_GOSPELS, HOLY_FRIDAY_HOURS, LITURGY_MAIN
     */
    private String groupKey;

    /**
     * Optional usage hint for finer distinction:
     * main, feast, saint, resurrection, optional, etc.
     */
    private String usage;

    /**
     * Free notes for exceptional cases or editorial comments.
     */
    private String notes;

    /**
     * Whether this assignment is primary in its service context.
     */
    private Boolean primaryAssignment;
}