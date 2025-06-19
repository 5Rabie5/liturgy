package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalPriorityRule;
import at.antiochorthodox.liturgy.repository.LiturgicalPriorityRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LiturgicalPriorityRuleServiceImpl implements LiturgicalPriorityRuleService {

    private final LiturgicalPriorityRuleRepository repository;

    @Autowired
    public LiturgicalPriorityRuleServiceImpl(LiturgicalPriorityRuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<LiturgicalPriorityRule> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<LiturgicalPriorityRule> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<LiturgicalPriorityRule> findByOccasionTypes(List<String> occasionTypes) {
        return repository.findByOccasionTypesIn(occasionTypes);
    }

    @Override
    public List<LiturgicalPriorityRule> findByOccasionType(String occasionType) {
        return repository.findByOccasionTypesContains(occasionType);
    }

    @Override
    public List<LiturgicalPriorityRule> searchByDescription(String text) {
        return repository.findByDescriptionContainingIgnoreCase(text);
    }

    @Override
    public LiturgicalPriorityRule save(LiturgicalPriorityRule rule) {
        return repository.save(rule);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
