package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("fasting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fasting {

    @Id
    private String id;

    private String lang;
    private String name;
    private String type; // christmas, lent, apostles, dormition, wednesday-friday...

    private String startDate; // MM-dd (ثابت)
    private String endDate;   // MM-dd (ثابت)

    private Integer startOffsetFromPascha; // متغير - قبل الفصح أو بعده
    private Integer endOffsetFromPascha;

    private boolean repeatWeekly; // لأربعاء وجمعة

    private int fastingLevel;  // 1 إلى 5 حسب الرمز
    private String symbol;     // الرمز الظاهر في التقويم ✠ أو ● أو ▣ أو لا شيء

    private List<String> allowed;
    private List<String> notAllowed;

    private boolean fastFree; // يوم خالٍ من الصوم

    private String shortdesc; // وصف مختصر
    private String description; // وصف مفصل للصوم إن وُجد
}
