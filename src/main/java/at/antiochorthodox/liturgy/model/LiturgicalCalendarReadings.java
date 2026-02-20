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
public class LiturgicalCalendarReadings {
    private ReadingGroup liturgicalDay;          // قراءات اليوم الليتورجي
    private ReadingGroup fixedFeast;             // قراءات العيد الثابت (إن وجد)
    private ReadingGroup movableFeast;           // قراءات العيد المتحرك (إن وجد)
    private List<ReadingGroup> saints;           // قراءات كل القديسين (قائمة)
}