package at.antiochorthodox.liturgy.model;

import lombok.*;
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

    private String title;
    private String reference;
    private String type;                // دائما "epistle"
    private String liturgicalName;
    private String lang;
    private String desc;

    // --- القطع الطقسية الرسالية ---
    private String prokeimenon1Title;
    private String prokeimenon1Tone;
    private String prokeimenon1Verse;
    private String prokeimenon1Stikheron;

    private String prokeimenon2Title;
    private String prokeimenon2Tone;
    private String prokeimenon2Verse;
    private String prokeimenon2Stikheron;

    // نص الرسالة
    private String readingTitle;
    private String readingContent;

    // --- هللويا (عادة للرسالة فقط) ---
    private String alleluiaTitle;
    private String alleluiaTone;
    private String alleluiaVerse;
    private String alleluiaStikheron;

    // --- legacy (اختياري، للحفاظ على بيانات قديمة فقط) ---
    private String prokeimenon;
    private String tone;
    private String stikheron;
}
