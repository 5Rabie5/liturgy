package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "epistle_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpistleReading {
    @Id
    private String id;

    private String readingKey;      // stable key for the epistle reading cycle
    private String liturgicalName;  // legacy fallback / display label

    private String title;
    private String reference;
    private String type;
    private String lang;
    private String desc;

    private String prokeimenon1Title;
    private String prokeimenon1Tone;
    private String prokeimenon1Verse;
    private String prokeimenon1Stikheron;

    private String prokeimenon2Title;
    private String prokeimenon2Tone;
    private String prokeimenon2Verse;
    private String prokeimenon2Stikheron;

    private String readingTitle;
    private String readingContent;

    private String alleluiaTitle;
    private String alleluiaTone;
    private String alleluiaVerse;
    private String alleluiaStikheron;
}
