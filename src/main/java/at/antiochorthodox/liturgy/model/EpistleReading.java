package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("epistle_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EpistleReading {
    @Id
    private String id;

    private String title;           // مثل: "فصل من أعمال الرسل القديسين"
    private String reference;       // مثل: "أعمال 5:12"
    private String content;         // نص الرسالة

    private String prokeimenon;     // نص البروكيمونون
    private String tone;            // اللحن (اللحن الثالث، الرابع...)
    private String stikheron;       // الاستيخون

    private String liturgicalName;  // 🔁 الاسم الليتورجي مثل: "الأحد الثالث بعد الفصح"
    private String lang;
    private String desc;
}
