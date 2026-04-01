package at.antiochorthodox.liturgy.reading.v2.repository;

import at.antiochorthodox.liturgy.reading.v2.model.LiturgicalReadingAssignment;

import java.util.List;

public interface LiturgicalReadingAssignmentRepositoryCustom {

    List<LiturgicalReadingAssignment> findAssignments(
            String tradition,
            String dayKey,
            String slot,
            String serviceKey,
            String sourceType,
            String readingType
    );
}