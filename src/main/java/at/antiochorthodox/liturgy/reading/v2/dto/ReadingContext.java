package at.antiochorthodox.liturgy.reading.v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingContext {

    private String tradition;

    private String calendarDayKey;
    private String readingDayKey;

    private String dayKey;
    private String dayLabel;
}
