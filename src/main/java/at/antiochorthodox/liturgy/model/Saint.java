package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("saint") // يجب أن يطابق اسم الكولكشن في MongoDB
public class Saint {
    @Id
    private String id;

    private String name;
    private String title;
    private String biographyUrl;
    private String iconUrl;
    private String shortdescription;
    private String description;
    private String lang;
    private List<SaintFeastInfo> feasts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaintFeastInfo {
        private String datesaintfeast;    // الشكل: MM-dd
        private String shortdescription;
        private String description;
        private String tag;
        private String iconurl;
    }
}
