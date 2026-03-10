package at.antiochorthodox.liturgy.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalCalendarDay {
    private LocalDate date;

    private String liturgicalName;   // display label
    private String liturgicalDayKey; // stable day identity
    private String epistleKey;       // resolved epistle cycle key for the day
    private String gospelKey;        // resolved gospel cycle key for the day

    private List<String> saints;

    private String gospelReading;
    private String epistleReading;
    private List<String> alternativeReadings;

    private String fixedFeast;
    private String movableFeast;

    private String fastingLevel;
    private String lang;
    private String desc;
    private Boolean marriageAllowed;
    private String marriageNote;

    private LiturgicalCalendarReadings readings;
}
