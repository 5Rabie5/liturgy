package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
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

    private Boolean repeatWeekly; // لأربعاء وجمعة

    private Integer fastingLevel;  // 1 إلى 5 حسب الرمز
    private String symbol;     // الرمز الظاهر في التقويم ✠ أو ● أو ▣ أو لا شيء

    private List<String> allowed;
    private List<String> notAllowed;

    private Boolean fastFree; // يوم خالٍ من الصوم

    private String shortdesc; // وصف مختصر
      private String desc;
  }
