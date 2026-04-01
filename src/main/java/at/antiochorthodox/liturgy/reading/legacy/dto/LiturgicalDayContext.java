package at.antiochorthodox.liturgy.reading.legacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiturgicalDayContext {

    private String tradition;

    private String dayKey;
    private String calendarDayKey;
    private String readingDayKey;

    private String dayLabel;
    private String slot;
    private String sourceType;

    private String epistleKey;
    private String gospelKey;
}