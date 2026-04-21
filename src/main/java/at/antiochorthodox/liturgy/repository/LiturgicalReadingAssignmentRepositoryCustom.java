package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;

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