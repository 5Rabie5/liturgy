package at.antiochorthodox.liturgy.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReading {
    private String title;             // عنوان رئيسي: "قراءة الرسالة" أو "فصل شريف من الإنجيل"
    private String reference;         // مرجع القراءة: "أعمال 1:1-8" أو "يوحنا 20:1-10"
    private String type;              // نوع القراءة: "epistle", "gospel", ...
    private String sourceId;          // لتعقب المصدر (id من جدول الرسائل/الإنجيل)
    private String liturgicalName;    // الاسم الطقسي: "أحد توما"
    private String lang;              // اللغة
    private String desc;              // وصف إضافي

    // --- القطع الخاصة بالرسالة (Epistle فقط) ---
    private String prokeimenon1Title;
    private String prokeimenon1Tone;
    private String prokeimenon1Verse;
    private String prokeimenon1Stikheron;

    private String prokeimenon2Title;
    private String prokeimenon2Tone;
    private String prokeimenon2Verse;
    private String prokeimenon2Stikheron;

    // --- القطع المشتركة (أي رسالة أو إنجيل أو في بعض التقاليد) ---
    private String readingTitle;         // مثال: "فصل من أعمال الرسل" أو "فصل شريف من الإنجيل"
    private String readingContent;       // النص الكامل

    // --- القطع الخاصة بالإنجيل (Gospel فقط) ---
    private String prokeimenonTitle;     // بروكيمنون الإنجيل (نادراً في التقليد الأنطاكي)
    private String prokeimenonTone;
    private String prokeimenonVerse;

    private String alleluiaTitle;
    private String alleluiaTone;
    private String alleluiaVerse;
    private String alleluiaStikheron;

    // حقول التتبع
    private String reason;               // "liturgicalName", "fixedFeast", ...
    private String reasonDetail;         // تفاصيل مثل: "عيد البشارة"، "أحد توما"
}
