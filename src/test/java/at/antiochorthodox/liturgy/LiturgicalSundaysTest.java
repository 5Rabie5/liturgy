package at.antiochorthodox.liturgy;

import at.antiochorthodox.liturgy.model.LiturgicalSunday;
import at.antiochorthodox.liturgy.service.LiturgicalSundays;
import at.antiochorthodox.liturgy.service.LiturgicalSundayLabelService;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

class LiturgicalSundaysTest {
    @Test
    void printLiturgicalSundays2022() {
        int year = 2022;
        String lang = "ar";

        LiturgicalSundayLabelService labelService = Mockito.mock(LiturgicalSundayLabelService.class);
        PaschaDateCalculator paschaDateCalculator = Mockito.mock(PaschaDateCalculator.class);

        // --- هنا يجب أن تضع Mock لكل المفاتيح المستخدمة في الكود ---
        Mockito.when(labelService.getLabel(Mockito.eq("AFTER_PENTECOST"), Mockito.eq(lang))).thenReturn("بعد العنصرة");
        Mockito.when(labelService.getLabel("PHARISEE_PUBLICAN", lang)).thenReturn("أحد الفريسي والعشار");
        Mockito.when(labelService.getLabel("PRODIGAL_SON", lang)).thenReturn("أحد الإبن الشاطر");
        Mockito.when(labelService.getLabel("BRANCH_SUNDAY", lang)).thenReturn("أحد مرفع اللحم");
        Mockito.when(labelService.getLabel("CHEESE_SUNDAY", lang)).thenReturn("أحد مرفع الجبن");
        Mockito.when(labelService.getLabel("FIRST_LENT_SUNDAY_ORTHODOX", lang)).thenReturn("الأحد الأول من الصوم (الأرثوذكسية)");
        Mockito.when(labelService.getLabel("SECOND_LENT_SUNDAY_PALAMAS", lang)).thenReturn("الأحد الثاني من الصوم (غريغوريوس بالاماس)");
        Mockito.when(labelService.getLabel("THIRD_LENT_SUNDAY_CROSS_VENERATION", lang)).thenReturn("الأحد الثالث من الصوم (السجود للصليب)");
        Mockito.when(labelService.getLabel("FOURTH_LENT_SUNDAY_JOHN_CLIMACUS", lang)).thenReturn("الأحد الرابع من الصوم (يوحنا السلمي)");
        Mockito.when(labelService.getLabel("FIFTH_LENT_SUNDAY_MARY_EGYPT", lang)).thenReturn("الأحد الخامس من الصوم (مريم المصرية)");
        Mockito.when(labelService.getLabel("PALM_SUNDAY", lang)).thenReturn("أحد الشعانين");
        Mockito.when(labelService.getLabel("PASCHA_SUNDAY", lang)).thenReturn("أحد الفصح (أحد القيامة)");
        Mockito.when(labelService.getLabel("THOMAS_SUNDAY", lang)).thenReturn("أحد توما");
        Mockito.when(labelService.getLabel("MYRRH_BEARING_WOMEN_SUNDAY", lang)).thenReturn("أحد حاملات الطيب");
        Mockito.when(labelService.getLabel("PARALYTIC_SUNDAY", lang)).thenReturn("أحد المخلع");
        Mockito.when(labelService.getLabel("SAMARITAN_WOMAN_SUNDAY", lang)).thenReturn("أحد السامرية");
        Mockito.when(labelService.getLabel("BLIND_MAN_SUNDAY", lang)).thenReturn("أحد الأعمى");
        Mockito.when(labelService.getLabel("FATHERS_OF_1ST_COUNCIL", lang)).thenReturn("أحد آباء المجمع");
        Mockito.when(labelService.getLabel("PENTECOST_SUNDAY", lang)).thenReturn("أحد العنصرة");
        Mockito.when(labelService.getLabel("ALL_SAINTS_SUNDAY", lang)).thenReturn("أحد جميع القديسين");

        // اليوم الفعلي للفصح
        Mockito.when(paschaDateCalculator.getPaschaDate(year)).thenReturn(LocalDate.of(2022, 4, 24));
        LocalDate pentecost = LocalDate.of(2022, 6, 12);

        LiturgicalSundays service = new LiturgicalSundays();
        List<LiturgicalSunday> sundays = service.buildLiturgicalSundays(
                year,
                pentecost,
                labelService,
                paschaDateCalculator,
                lang
        );

        System.out.println("=== قائمة الآحاد الطقسية لسنة " + year + " (بعد العنصرة دائريًا)===");
        for (LiturgicalSunday s : sundays) {
            System.out.printf(
                    "%s: %s (%s)%s%n",
                    s.getDate(),
                    s.getLabel(),
                    s.getType(),
                    s.getWeekAfterPentecost() != null ? String.format(" [الأحد %d بعد العنصرة]", s.getWeekAfterPentecost()) : ""
            );
        }
    }
}
