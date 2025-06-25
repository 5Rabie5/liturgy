package at.antiochorthodox.liturgy.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Document("byzantine_piece_days")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiturgicalByzantinePieceDay {
    @Id
    private String id;

    private LocalDate date;          // تاريخ اليوم الميلادي
    private String dayName;          // اسم اليوم (أحد، عيد...)
    private String season;           // الموسم (فصح، صوم...)
    private List<ByzantinePiece> pieces; // جميع القطع البيزنطية لليوم
    private String lang;
    private String desc;
}
