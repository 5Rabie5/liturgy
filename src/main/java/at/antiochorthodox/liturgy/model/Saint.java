package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Builder
@Document("saints")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Saint {

    @Id
    private String id;

    private String name;
    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor

    public static class SaintFeastInfo {
        private String datesaintfeast;
        private String shortdescription;
        private String description;
        private String tag;
        private String iconurl;
    }
    private String title;
    private String biographyUrl;
    private String iconUrl;
    private String shortdescription;
    private String description;
    private List<SaintFeastInfo> feasts;

    private String lang;
}