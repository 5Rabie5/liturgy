package at.antiochorthodox.liturgy.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReadingOption {
    private String label;            // "Feast Readings", "Sunday Readings", ...
    private String reason;           // سبب تفعيل هذا الخيار
    private Boolean preferred;       // هو المفضل؟

    private ScriptureReading gospel; // الإنجيل (يمكن أن يكون null إذا لم يوجد)
    private ScriptureReading epistle;// الرسالة (يمكن أن يكون null إذا لم يوجد)
    private List<ScriptureReading> alternativeReadings; // أي قراءات أخرى إضافية
    private String lang;
    private String desc;
}
