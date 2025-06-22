package at.antiochorthodox.liturgy;

import at.antiochorthodox.liturgy.model.LiturgicalSunday;
import at.antiochorthodox.liturgy.service.LiturgicalSundayLabelService;
import at.antiochorthodox.liturgy.service.LiturgicalSundayNetworkService;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
@SpringBootTest
class LiturgicalSundayNetworkServiceTest {

    private PaschaDateCalculator paschaDateCalculator;
    private LiturgicalSundayLabelService labelService;
    private LiturgicalSundayNetworkService service;

    @Autowired
    private LiturgicalSundayNetworkService liturgicalSundayNetworkService;
    @BeforeEach
    void setup() {
        paschaDateCalculator = Mockito.mock(PaschaDateCalculator.class);
        labelService = Mockito.mock(LiturgicalSundayLabelService.class);

        service = new LiturgicalSundayNetworkService(paschaDateCalculator, labelService);
    }

    @Test
    void testFullPaschalNetwork2022() {
        int year = 2022;
        String lang = "ar";
        LocalDate paschaDate = LocalDate.of(2022, 4, 24);
        Mockito.when(paschaDateCalculator.getPaschaDate(year)).thenReturn(paschaDate);

        // جهز التسميات المتوقعة لكل أحد خاص (باستخدام HashMap بدلاً من Map.of)
        Map<String, String> expectedLabels = new HashMap<>();
        expectedLabels.put("PASCHA", "الفصح");
        expectedLabels.put("THOMAS_SUNDAY", "أحد توما");
        expectedLabels.put("MYRRH_BEARING_WOMEN", "أحد حاملات الطيب");
        expectedLabels.put("PARALYTIC", "أحد المخلع");
        expectedLabels.put("SAMARITAN_WOMAN", "أحد السامرية");
        expectedLabels.put("BLIND_MAN", "أحد الأعمى");
        expectedLabels.put("FATHERS_OF_1ST_COUNCIL", "أحد آباء المجمع");
        expectedLabels.put("PENTECOST", "أحد العنصرة");
        expectedLabels.put("ALL_SAINTS", "أحد جميع القديسين");
        expectedLabels.put("SUNDAY_OF_ANCESTORS", "أحد الأجداد");
        expectedLabels.put("SUNDAY_OF_FATHERS", "أحد الآباء");
        expectedLabels.put("SUNDAY_AFTER_THE_CROSS", "أحد رفع الصليب");
        expectedLabels.put("SUNDAY_OF_ELIAS", "أحد النبي إيليا");
        expectedLabels.put("NATIVITY", "عيد الميلاد");
        expectedLabels.put("AFTER_PENTECOST", "الأحد بعد العنصرة");
        expectedLabels.put("ORDINARY_SUNDAY", "أحد عادي");

        // مَوك لكل تسمية مستخدمة
        expectedLabels.forEach((key, val) ->
                Mockito.when(labelService.getLabel(eq(key), anyString())).thenReturn(val)
        );

        List<LiturgicalSunday> sundays = service.buildLiturgicalSundays(year, lang);

        // ===== تحقق من الفصح وكل الآحاد الفصحية =====
        assertEquals("الفصح",
                getSundayLabel(sundays, LocalDate.of(2022, 4, 24)));

        assertEquals("أحد توما",
                getSundayLabel(sundays, LocalDate.of(2022, 5, 1)));

        assertEquals("أحد حاملات الطيب",
                getSundayLabel(sundays, LocalDate.of(2022, 5, 8)));

        assertEquals("أحد المخلع",
                getSundayLabel(sundays, LocalDate.of(2022, 5, 15)));

        assertEquals("أحد السامرية",
                getSundayLabel(sundays, LocalDate.of(2022, 5, 22)));

        assertEquals("أحد الأعمى",
                getSundayLabel(sundays, LocalDate.of(2022, 5, 29)));

        assertEquals("أحد آباء المجمع",
                getSundayLabel(sundays, LocalDate.of(2022, 6, 5)));

        assertEquals("أحد العنصرة",
                getSundayLabel(sundays, LocalDate.of(2022, 6, 12)));

        assertEquals("أحد جميع القديسين",
                getSundayLabel(sundays, LocalDate.of(2022, 6, 19)));

        // ===== تحقق من آحاد بعد العنصرة (العادي/الأحد X بعد العنصرة) =====
        // الأحد الثاني بعد العنصرة
        String labelAfterPentecost = sundays.stream()
                .filter(s -> s.getDate().equals(LocalDate.of(2022, 6, 26)))
                .findFirst()
                .orElseThrow()
                .getLabel();
        assertTrue(labelAfterPentecost.contains("الأحد بعد العنصرة"));

        // تحقق من أحد الأجداد، الآباء، قبل الميلاد، الصليب، إيليا والميلاد
        assertEquals("أحد الأجداد",
                getSundayLabel(sundays, LocalDate.of(2022, 12, 11)));

        assertEquals("أحد الآباء",
                getSundayLabel(sundays, LocalDate.of(2022, 12, 18)));

        assertEquals("عيد الميلاد",
                getSundayLabel(sundays, LocalDate.of(2022, 12, 25)));

        // أحد رفع الصليب (الأقرب بعد 14 أيلول)
        assertEquals("أحد رفع الصليب",
                getSundayLabel(sundays, LocalDate.of(2022, 9, 18)));

        // أحد النبي إيليا (الأقرب بعد 20 تموز)
        assertEquals("أحد النبي إيليا",
                getSundayLabel(sundays, LocalDate.of(2022, 7, 24)));

        // ===== تحقق من الباقي أنها "أحد عادي" =====
        List<LiturgicalSunday> ordinarySundays = sundays.stream()
                .filter(s -> s.getLabel().equals("أحد عادي"))
                .collect(Collectors.toList());
        assertFalse(ordinarySundays.isEmpty());

        // تحقق من أن عدد الآحاد في السنة الميلادية = عدد آحاد الأحد (عادةً 52 أو 53)
        assertTrue(sundays.size() == 52 || sundays.size() == 53);
    }

    // دالة مساعدة للحصول على تسمية أحد بتاريخ معين
    private String getSundayLabel(List<LiturgicalSunday> sundays, LocalDate date) {
        return sundays.stream()
                .filter(s -> s.getDate().equals(date))
                .findFirst()
                .orElseThrow(() -> new AssertionError("لم يتم إيجاد أحد بتاريخ: " + date))
                .getLabel();
    }
    @Test
    void printLiturgicalSundays2022() {
        int year = 2022;
        String lang = "ar";
        // يمكنك إزالة mock إذا تستخدم الخدمة الأصلية
        List<LiturgicalSunday> sundays = liturgicalSundayNetworkService.buildLiturgicalSundays(year, lang);

        System.out.println("=== الشبكة الفصحية لسنة " + year + " ===");
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
