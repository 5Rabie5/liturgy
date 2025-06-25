package at.antiochorthodox.liturgy.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaschalNetworkEntry {
    private String id;
    private String weekName;
    private int offsetFromPascha;
    private String gospelReading;
    private String epistleReading;
    private String tone;
    private String kathisma;
    private String kontakion;
    private String details;
    private String lang;
    private String desc;
}
