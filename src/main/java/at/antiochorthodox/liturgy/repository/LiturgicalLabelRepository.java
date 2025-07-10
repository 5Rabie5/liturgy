package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.LiturgicalLabel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface LiturgicalLabelRepository extends MongoRepository<LiturgicalLabel, String> {
    List<LiturgicalLabel> findByLang(String lang);
    Optional<LiturgicalLabel> findByLabelKeyAndLang(String labelKey, String lang);
}
