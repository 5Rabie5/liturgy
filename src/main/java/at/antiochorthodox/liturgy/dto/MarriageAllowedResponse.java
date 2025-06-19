package at.antiochorthodox.liturgy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MarriageAllowedResponse {
    private LocalDate date;
    private boolean allowed;
    private String reason;
}
