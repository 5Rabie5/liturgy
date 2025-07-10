package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("gospel_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GospelReading {
    @Id
    private String id;

    private String title;             // مثل: "فصل شريف من بشارة القديس يوحنا"
    private String reference;         // مثل: "يوحنا 20:1-10"
    private String content;           // نص الإنجيل

    private String type;              // مثل: "القداس الإلهي", "السَحر", "غروب"
    private String liturgicalName;    // مثل: "الأحد الثاني بعد القيامة"
    private String lang;
    private String desc;
}
