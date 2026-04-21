package at.antiochorthodox.liturgy.reading.v2.summary;

public record SundayReadingSummary(
        String slot,
        String sourceType,
        String epistleKey,
        String gospelKey
) {
    public boolean isEmpty() {
        return epistleKey == null && gospelKey == null;
    }
}
