package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalPriorityRule;

import java.util.List;
import java.util.Optional;

public interface LiturgicalPriorityRuleService {
    List<LiturgicalPriorityRule> findAll();

    Optional<LiturgicalPriorityRule> findById(String id);

    List<LiturgicalPriorityRule> findByOccasionTypes(List<String> occasionTypes);

    List<LiturgicalPriorityRule> findByOccasionType(String occasionType);

    List<LiturgicalPriorityRule> searchByDescription(String text);

    LiturgicalPriorityRule save(LiturgicalPriorityRule rule);

    void deleteById(String id);
}
