package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalPriorityRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiturgicalPriorityRuleRepository extends MongoRepository<LiturgicalPriorityRule, String> {

    // ابحث عن قواعد تقاطع بين مناسبتين معينتين (أي ترتيب)
    List<LiturgicalPriorityRule> findByOccasionTypesIn(List<String> occasionTypes);

    // ابحث عن كل القواعد التي فيها مناسبة معينة
    List<LiturgicalPriorityRule> findByOccasionTypesContains(String occasionType);

    // ابحث عن القاعدة بوصفها
    List<LiturgicalPriorityRule> findByDescriptionContainingIgnoreCase(String description);

    // يمكن إضافة أي دوال فلترة أخرى حسب الحاجة
}
