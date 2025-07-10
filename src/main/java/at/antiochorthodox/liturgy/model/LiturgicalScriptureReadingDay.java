package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Document("scripture_reading_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalScriptureReadingDay {
    @Id
    private String id;

    private LocalDate date;
    private String dayName;            // اسم اليوم (مثلاً: أحد القيامة)
    private String season;            // الموسم الطقسي (الفصح، الصوم، إلخ)
    private String note;

    private List<String> epistleLiturgicalNames;  // أسماء ليتورجية للرسائل (مثل "الأحد الثاني بعد العنصرة")
    private List<String> gospelLiturgicalNames;   // أسماء ليتورجية للأناجيل (مثل "عيد القديس لوقا")

    private String lang;
    private String desc;
}
