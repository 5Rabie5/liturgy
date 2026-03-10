package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gospel_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GospelReading {
    @Id
    private String id;

    private String readingKey;      // stable key for the gospel reading cycle
    private String liturgicalName;  // legacy fallback / display label

    private String title;
    private String reference;
    private String type;
    private String lang;
    private String desc;

    private String readingTitle;
    private String readingContent;

    private String prokeimenonTitle;
    private String prokeimenonTone;
    private String prokeimenonVerse;

    private String alleluiaTitle;
    private String alleluiaTone;
    private String alleluiaVerse;
    private String alleluiaStikheron;
}
