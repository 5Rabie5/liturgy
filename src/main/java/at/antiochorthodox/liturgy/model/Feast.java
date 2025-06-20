// --- Feast.java (Model) ---
package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("feasts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feast {

    @Id
    private String id;

    private String name;
    private String lang;

    private String type; // "fixed" or "paschal"
    private String date; // for fixed feasts (format: MM-dd)
    private Integer offsetFromPascha; // for paschal feasts (e.g. -7 for Palm Sunday)

    private String group;     // مثل: سيدية، شهود، أنبياء
    private String priority;  // مرتبة طقسية أو أهمية

    private String shortdesc;
    private String desc;
    private String iconUrl;


}
