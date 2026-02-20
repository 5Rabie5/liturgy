package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingGroup {
    private String key;                  // نفس الليتورجيName اللي بنبحث فيه (مثلاً "أحد القيامة" أو "عيد القديس ...")
    private String label;                // عنوان للعرض (Sunday / Fixed Feast / Saint ...)
    private List<ScriptureReading> readings; // كل القراءات (epistle + gospel + أي شي)
    private String desc;
}