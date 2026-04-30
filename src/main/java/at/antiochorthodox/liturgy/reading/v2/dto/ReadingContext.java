package at.antiochorthodox.liturgy.reading.v2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingContext {

    private String tradition;

    private LocalDate date;

    private String calendarDayKey;
    private String readingDayKey;

    private String dayKey;
    private String dayLabel;
    private List<String> lookupDayKeys;
}
