package at.antiochorthodox.liturgy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransitionalSundayResolution {

    private final boolean inWindow;
    private final boolean overrideApplied;

    private final String effectiveDayKey;
    private final String effectiveReadingDayKey;
    private final String effectiveLiturgicalName;

    private final String effectiveEpistleKey;
    private final String effectiveGospelKey;

    private final String decisionBasis;
    private final String sourceReference;
    private final String note;
}