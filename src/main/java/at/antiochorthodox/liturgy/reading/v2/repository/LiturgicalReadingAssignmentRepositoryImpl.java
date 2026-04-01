package at.antiochorthodox.liturgy.reading.v2.repository;

import at.antiochorthodox.liturgy.reading.v2.model.LiturgicalReadingAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class LiturgicalReadingAssignmentRepositoryImpl
        implements LiturgicalReadingAssignmentRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<LiturgicalReadingAssignment> findAssignments(
            String tradition,
            String dayKey,
            String slot,
            String serviceKey,
            String sourceType,
            String readingType
    ) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (tradition != null && !tradition.isBlank()) {
            criteriaList.add(Criteria.where("tradition").is(tradition));
        }
        if (dayKey != null && !dayKey.isBlank()) {
            criteriaList.add(Criteria.where("dayKey").is(dayKey));
        }
        if (slot != null && !slot.isBlank()) {
            criteriaList.add(Criteria.where("slot").is(slot));
        }
        if (serviceKey != null && !serviceKey.isBlank()) {
            criteriaList.add(Criteria.where("serviceKey").is(serviceKey));
        }
        if (sourceType != null && !sourceType.isBlank()) {
            criteriaList.add(Criteria.where("sourceType").is(sourceType));
        }
        if (readingType != null && !readingType.isBlank()) {
            criteriaList.add(Criteria.where("readingType").is(readingType));
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        query.with(Sort.by(
                Sort.Order.asc("serviceKey"),
                Sort.Order.asc("sequence")
        ));

        return mongoTemplate.find(query, LiturgicalReadingAssignment.class);
    }
}