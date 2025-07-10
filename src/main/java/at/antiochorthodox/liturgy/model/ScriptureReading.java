package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReading {

    private String title;          // العنوان: "فصل من أعمال الرسل" أو "فصل شريف من بشارة..."
    private String reference;      // المرجع: "أعمال 5:12" أو "يوحنا 20:1-10"
    private String content;        // النص الكامل

    private String type;           // "epistle" أو "gospel"
    private String sourceId;       // ID الخاص بـ EpistleReading أو GospelReading
    private String liturgicalName; // الاسم الليتورجي للقراءة
    private String lang;
    private String desc;

    // Optional (only for epistles)
    private String prokeimenon;
    private String tone;
    private String stikheron;
}

