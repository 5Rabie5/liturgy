package at.antiochorthodox.liturgy.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalSunday {
    private LocalDate date;
    private String label;
    private String type;
    private Integer weekAfterPentecost;
    private String note;
    private String lang;
}
