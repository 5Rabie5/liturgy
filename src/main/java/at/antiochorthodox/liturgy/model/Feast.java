package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("feasts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feast {

    @Id
    private String id;

    private String name;
    private String type;         // fixed, movable, pascha, etc.
    private String date;         // "MM-dd" for fixed OR "PASCHA+1", "PASCHA-40"
    private String shortdesc;
    private String desc;
    private String iconUrl;
    private String tag;
    private String lang;
    private Integer priority;    // ترتيب حسب الأهمية
    private String group;        // تصنيف العيد
}
