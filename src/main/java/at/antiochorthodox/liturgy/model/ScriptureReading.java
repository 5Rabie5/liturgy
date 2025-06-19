package at.antiochorthodox.liturgy.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReading {

    private String readingType; // "GOSPEL" or "EPISTLE"
    private String reference;   // e.g. "Matthew 5:1-12"
    private String content;     // (Optional) The reading text
    private String language;    // e.g. "ar", "en", "gr"
}
