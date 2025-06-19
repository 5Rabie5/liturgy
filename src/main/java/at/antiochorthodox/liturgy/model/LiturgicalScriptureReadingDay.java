package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Document("scripture_reading_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalScriptureReadingDay {
    @Id
    private String id;

    private LocalDate date;           // Gregorian date
    private String dayName;           // Name of the day (Sunday, Feast...)
    private String season;            // Season (Pascha, Lent, etc.)
    private String note;              // General note

    private List<ScriptureReadingOption> options; // List of possible options for readings
}
