package at.antiochorthodox.liturgy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingDayKeyResolution {
    private String calendarDayKey;
    private String canonicalDayKey;
    private String resolvedDayKey;
    private String reason;
    private List<String> lookupDayKeys;
    private List<String> triedKeys;
}
