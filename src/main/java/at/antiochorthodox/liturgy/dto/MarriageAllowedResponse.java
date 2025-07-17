package at.antiochorthodox.liturgy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
@Getter
@Data
@AllArgsConstructor
public class MarriageAllowedResponse {
    private LocalDate date;
    private boolean allowed;
    private String message; // تم تعديل الاسم من reason إلى message
}
