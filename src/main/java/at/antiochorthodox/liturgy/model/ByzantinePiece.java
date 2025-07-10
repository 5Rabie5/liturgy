package at.antiochorthodox.liturgy.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ByzantinePiece {

    private ByzantinePieceType pieceType; // Enum محدد مسبقًا
    private String content;               // نص القطعة (اختياري)
    private Integer tone;                 // رقم اللحن (1-8) أو null
    private String toneName;              // اسم اللحن (الأول، الثاني، ... أو null)
    private String language;
    private String lang;
    private String desc;
}
