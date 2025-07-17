package at.antiochorthodox.liturgy.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReadingOption {
    private String label;                  // "Feast Readings", "Sunday Readings", etc.
    private String reason;                 // Same values as above
    private String reasonDetail;           // Details (e.g. "البشارة", "القديس جاورجيوس")
    private Boolean preferred;             // Is this the primary reading for the day?
    private ScriptureReading gospel;
    private ScriptureReading epistle;
    private List<ScriptureReading> alternativeReadings;
    private String lang;
    private String desc;
}

