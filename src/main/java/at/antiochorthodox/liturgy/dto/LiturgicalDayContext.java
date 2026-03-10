package at.antiochorthodox.liturgy.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalDayContext {
    private String dayKey;
    private String dayLabel;
    private String epistleKey;
    private String gospelKey;
}
