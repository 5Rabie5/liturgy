package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "liturgical_labels")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LiturgicalLabel {
    @Id
    private String id;

    private String labelKey;    // legacy/week label key (can repeat across weekdays)
    private String dayKey;      // new unique key for a specific liturgical day
    private String lang;        // "ar", "en", "fr", "de", "nl"
    private String text;        // display text

    private String type;        // sunday / weekday
    private String season;      // pascha / pentecost / triodion
    private Integer weekIndex;  // week number in the season
    private String dayOfWeek;   // monday, tuesday ...
}
