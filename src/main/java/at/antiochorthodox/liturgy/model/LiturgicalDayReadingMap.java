package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "liturgical_day_reading_maps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalDayReadingMap {
    @Id
    private String id;

    private String tradition;   // ANTIOCHIAN
    private String dayKey;      // stable day identity
    private String epistleKey;  // stable epistle cycle key
    private String gospelKey;   // stable gospel cycle key
    private String notes;
}
