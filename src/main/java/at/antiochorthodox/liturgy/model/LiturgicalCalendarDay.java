package at.antiochorthodox.liturgy.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalCalendarDay {
    private LocalDate date;                   // التاريخ الميلادي

    private String dayName;                   // اسم الأحد/المناسبة

    private List<String> saints;              // القديسون المعيَّد لهم في هذا اليوم

    private String gospelReading;             // مرجع الإنجيل (مثلاً: متى 5:1-12)
    private String epistleReading;            // مرجع الرسالة (مثلاً: كورنثوس...)
    private List<String> alternativeReadings; // إذا وجد أكثر من قراءة (اختياري)

    private String fixedFeast;                // العيد الثابت (مثلاً: البشارة)
    private String movableFeast;              // العيد المتغير (مثلاً: الشعانين)

    private String fastingLevel;               // نوع الصوم أو ملاحظة سريعة (صوم انقطاعي/صوم غير صارم/بدون صوم)
    private String lang;
    private String desc;
}
