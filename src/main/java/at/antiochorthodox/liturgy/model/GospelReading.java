package at.antiochorthodox.liturgy.model;

import lombok.*;
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

    private String title;
    private String reference;
    private String type;                 // دائما "gospel"
    private String liturgicalName;
    private String lang;
    private String desc;

    // نص الإنجيل
    private String readingTitle;
    private String readingContent;

    // --- بروكيمنون خاص بالإنجيل (نادراً حسب التقليد) ---
    private String prokeimenonTitle;
    private String prokeimenonTone;
    private String prokeimenonVerse;

    // --- هللويا (للأيام أو الأسابيع التي تسبق الإنجيل بها هللويا) ---
    private String alleluiaTitle;
    private String alleluiaTone;
    private String alleluiaVerse;
    private String alleluiaStikheron;
}
