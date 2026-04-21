package at.antiochorthodox.liturgy.config;

import at.antiochorthodox.liturgy.model.LiturgicalReadingAssignment;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
@RequiredArgsConstructor
public class LiturgicalReadingAssignmentIndexesConfig {

    private final MongoTemplate mongoTemplate;

    @PostConstruct
    public void createIndexes() {
        var indexOps = mongoTemplate.indexOps(LiturgicalReadingAssignment.class);

        indexOps.createIndex(new Index()
                .on("tradition", Sort.Direction.ASC)
                .on("dayKey", Sort.Direction.ASC)
                .on("slot", Sort.Direction.ASC)
                .on("sourceType", Sort.Direction.ASC)
                .on("primaryAssignment", Sort.Direction.ASC)
                .on("sequence", Sort.Direction.ASC)
                .named("idx_lra_day_slot_source_primary_seq"));

        indexOps.createIndex(new Index()
                .on("tradition", Sort.Direction.ASC)
                .on("serviceKey", Sort.Direction.ASC)
                .on("sequence", Sort.Direction.ASC)
                .named("idx_lra_service_seq"));

        indexOps.createIndex(new Index()
                .on("tradition", Sort.Direction.ASC)
                .on("dayKey", Sort.Direction.ASC)
                .on("groupKey", Sort.Direction.ASC)
                .on("sequence", Sort.Direction.ASC)
                .named("idx_lra_day_group_seq"));

        indexOps.createIndex(new Index()
                .on("tradition", Sort.Direction.ASC)
                .on("calendarDayKey", Sort.Direction.ASC)
                .on("slot", Sort.Direction.ASC)
                .on("sequence", Sort.Direction.ASC)
                .named("idx_lra_calendar_day_slot_seq"));

        indexOps.createIndex(new Index()
                .on("readingType", Sort.Direction.ASC)
                .on("readingKey", Sort.Direction.ASC)
                .named("idx_lra_reading_type_key"));

        indexOps.createIndex(new Index()
                .on("tradition", Sort.Direction.ASC)
                .on("dayKey", Sort.Direction.ASC)
                .on("slot", Sort.Direction.ASC)
                .on("serviceKey", Sort.Direction.ASC)
                .on("sourceType", Sort.Direction.ASC)
                .on("readingType", Sort.Direction.ASC)
                .on("readingKey", Sort.Direction.ASC)
                .on("sequence", Sort.Direction.ASC)
                .unique()
                .named("ux_lra_logical_identity"));
    }
}