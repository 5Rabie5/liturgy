package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "marriage_ban_reasons")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarriageBanReason {
    @Id
    private String id;

    private String code;       // مثل: BEFORE_EPIPHANY, CHEESE_TO_RENEWAL, etc.
    private String lang;       // ar, en, de, fr, nl
    private Map<String, String> message;   // "اليوم السابق للظهور الإلهي (لا يسمح بالزواج)"
}
