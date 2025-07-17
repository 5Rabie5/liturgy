package at.antiochorthodox.liturgy.repository;

import at.antiochorthodox.liturgy.model.MarriageBanReason;
import com.mongodb.client.MongoIterable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MarriageBanReasonRepository extends MongoRepository<MarriageBanReason, String> {
    Optional<MarriageBanReason> findByCodeAndLang(String code, String lang);

    Optional<MarriageBanReason> findByCode(String code);
}
