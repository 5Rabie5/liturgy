package at.antiochorthodox.liturgy.reading.legacy.model;

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
    private String slot;        // default / liturgy / matins / vespers / wedding / funeral / etc.
    private String sourceType;  // daily / fixed_feast / saint / resurrection / hour / service
    private String epistleKey;  // stable epistle cycle key for this slot
    private String gospelKey;   // stable gospel cycle key for this slot
    private String notes;
}
