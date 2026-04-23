package at.antiochorthodox.liturgy.dto;

import at.antiochorthodox.liturgy.model.YearAuditIssueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearAuditReport {

    private int year;
    private String lang;

    private int totalDays;
    private long problemDays;

    private Map<YearAuditIssueType, Long> counts;
    private List<YearAuditDayItem> items;
}
