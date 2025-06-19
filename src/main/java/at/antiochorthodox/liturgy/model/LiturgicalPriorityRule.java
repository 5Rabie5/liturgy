package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document("liturgical_priority_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalPriorityRule {
    @Id
    private String id;

    // أنواع المناسبات التي تتقاطع (مثلاً عيد سيّدي متغير + أحد الصوم)
    private List<String> occasionTypes;    // مثال: ["Feast_Lord_Movable", "Sunday_Lent"]

    private String description;            // وصف القاعدة (بالعربية أو الإنجليزية)

    // أولوية كل عنصر طقسي في التقاطع
    private String gospelPriority;         // الإنجيل لمن؟ (الأحد/العيد/القديس...)
    private String epistlePriority;        // الرسالة لمن؟
    private String katavasiaPriority;      // الكاطافاسيات لمن؟
    private String iothinaPriority;        // الأيوتينة لمن؟
    private String kontakionPriority;      // الخنداق لمن؟
    private String tonePriority;           // اللحن لأي مناسبة (رقم أو اسم أو "حسب الدورة")

    private Boolean allowAlternative;      // هل يجوز قراءة البديل أيضًا؟
    private String alternativeExplanation; // شرح في حال وجود أكثر من خيار (أو سبب السماح)

    private String notes;                  // أي ملاحظة إضافية أو استثناء أو قرار محلي
}
