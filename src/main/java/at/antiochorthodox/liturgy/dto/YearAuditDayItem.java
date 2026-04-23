package at.antiochorthodox.liturgy.dto;

import at.antiochorthodox.liturgy.model.YearAuditIssueType;
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
public class YearAuditDayItem {

    private LocalDate date;

    private String calendarDayKey;
    private String resolvedDayKey;
    private String liturgicalName;

    private String readingSlot;
    private String readingSourceType;
    private String epistleKey;
    private String gospelKey;

    private String fixedFeast;
    private String movableFeast;

    private List<YearAuditIssueType> issueTypes;
    private String note;
}
