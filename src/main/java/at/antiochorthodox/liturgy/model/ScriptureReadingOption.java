package at.antiochorthodox.liturgy.model;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReadingOption {

    private String label;       // e.g. "Feast Readings", "Sunday Readings"
    private String reason;      // Reason for this option (overlap, exception, etc.)
    private Boolean preferred;  // Is this the default/typical option?

    private List<ScriptureReading> readings; // List of scripture readings for this option
}
