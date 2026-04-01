package at.antiochorthodox.liturgy.model;

import at.antiochorthodox.liturgy.reading.legacy.model.LiturgicalCalendarReadings;
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
    private String readingSlot;      // resolved slot for the map entry
    private String readingSourceType; // daily / fixed_feast / saint / resurrection / hour / service
    private String epistleKey;       // resolved epistle cycle key for the day / slot
    private String gospelKey;        // resolved gospel cycle key for the day / slot

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
