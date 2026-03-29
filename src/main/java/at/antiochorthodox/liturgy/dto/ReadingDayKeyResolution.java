package at.antiochorthodox.liturgy.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReadingDayKeyResolution {
    private String calendarDayKey;
    private String resolvedDayKey;
    private String reason;
    private List<String> triedKeys;
}