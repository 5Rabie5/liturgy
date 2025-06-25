package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaschalNetworkRow {
    private LocalDate date;      // تاريخ الأحد
    private int weekNumber;      // رقم الأسبوع (أو أحد... الخ)
    private String weekName;     // اسم الأحد (أحد توما، المخلع...)
    private String tone;         // اللحن
    private String gospelReading;// الإنجيل (أو مرجع القراءة)
    private String epistleReading; // الرسالة
    private String kathisma;     // الكاطافاسيات
    private String kontakion;    // الخنداق/الأيوتينة
    private String lang;
    private String desc;
    // أي أعمدة أخرى من الجدول حسب حاجتك
}
