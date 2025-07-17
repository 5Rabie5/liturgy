package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReading {
    private String title;
    private String reference;
    private String content;
    private String type;           // "epistle", "gospel", ...
    private String sourceId;
    private String liturgicalName;
    private String lang;
    private String desc;
    private String prokeimenon;
    private String tone;
    private String stikheron;

    // New fields for traceability
    private String reason;        // "liturgicalName", "fixedFeast", "movableFeast", "saint"
    private String reasonDetail;  // e.g., "Annunciation", "St. George", "Sunday of Thomas"
}
