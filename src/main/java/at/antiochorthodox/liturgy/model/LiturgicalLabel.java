package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("liturgical_labels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalLabel {
    @Id
    private String id;

    private String labelKey;   // مثال: "SUNDAY_OF_THOMAS"
    private String lang;       // مثال: "ar", "en", "fr"
    private String text;       // النص المترجم
}
