package at.antiochorthodox.liturgy.reading.v2.summary;

import at.antiochorthodox.liturgy.dto.ReadingAssignmentItemDto;
import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class SundayReadingSummarySelector {

    public Optional<SundayReadingSummary> selectBestForSunday(List<ServiceReadingsDto> services) {
        if (services == null || services.isEmpty()) {
            return Optional.empty();
        }

        return services.stream()
                .filter(Objects::nonNull)
                .map(this::toCandidate)
                .filter(Objects::nonNull)
                .sorted(
                        Comparator
                                .comparingInt((Candidate c) -> slotPriority(c.summary().slot()))
                                .thenComparingInt(c -> -completenessScore(c.summary()))
                                .thenComparingInt(c -> sourceTypePriority(c.summary().sourceType()))
                )
                .map(Candidate::summary)
                .findFirst();
    }

    private Candidate toCandidate(ServiceReadingsDto service) {
        if (service.getReadings() == null || service.getReadings().isEmpty()) {
            return null;
        }

        String epistleKey = firstReadingKeyByType(service.getReadings(), "epistle");
        String gospelKey = firstReadingKeyByType(service.getReadings(), "gospel");

        if (epistleKey == null && gospelKey == null) {
            return null;
        }

        SundayReadingSummary summary = new SundayReadingSummary(
                normalize(service.getSlot()),
                normalize(service.getSourceType()),
                epistleKey,
                gospelKey
        );

        return new Candidate(summary);
    }

    private String firstReadingKeyByType(List<ReadingAssignmentItemDto> readings, String type) {
        return readings.stream()
                .filter(Objects::nonNull)
                .filter(r -> type.equalsIgnoreCase(r.getReadingType()))
                .sorted(Comparator.comparingInt(r -> r.getSequence() == null ? Integer.MAX_VALUE : r.getSequence()))
                .map(ReadingAssignmentItemDto::getReadingKey)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private int slotPriority(String slot) {
        if (slot == null) {
            return 99;
        }
        return switch (slot) {
            case "liturgy" -> 0;
            case "default" -> 1;
            case "matins" -> 2;
            default -> 9;
        };
    }

    private int sourceTypePriority(String sourceType) {
        if (sourceType == null) {
            return 99;
        }
        return switch (sourceType) {
            case "fixed_feast" -> 0;
            case "day" -> 1;
            case "movable_feast" -> 2;
            case "service" -> 3;
            case "saint" -> 4;
            default -> 9;
        };
    }

    private int completenessScore(SundayReadingSummary summary) {
        boolean hasEpistle = summary.epistleKey() != null;
        boolean hasGospel = summary.gospelKey() != null;

        if (hasEpistle && hasGospel) {
            return 2;
        }
        if (hasGospel) {
            return 1;
        }
        if (hasEpistle) {
            return 0;
        }
        return -1;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private record Candidate(SundayReadingSummary summary) {
    }
}
