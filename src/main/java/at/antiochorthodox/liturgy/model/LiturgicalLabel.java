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

    private String labelKey;    // مثل: PASCHA_SUNDAY أو MONDAY_AFTER_PASCHA
    private String lang;        // "ar", "en", "fr", "de", "nl"
    private String text;        // النص المعروض

    private String type;        // sunday / weekday
    private String season;      // pascha / pentecost
    private Integer weekIndex;  // رقم الأسبوع
    private String dayOfWeek;   // monday, tuesday ...
}