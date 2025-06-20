package at.antiochorthodox.liturgy;

import at.antiochorthodox.liturgy.model.*;
import at.antiochorthodox.liturgy.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LiturgicalCalendarDayBuilderServiceTest {

    private LiturgicalScriptureReadingDayService liturgicalScriptureReadingDayService;
    private SaintService saintService;
    private FeastService feastService;
    private FastingService fastingService;
    private LiturgicalCalendarDayBuilderService builderService;

    @BeforeEach
    void setup() {
        // اصنع mocks
        liturgicalScriptureReadingDayService = mock(LiturgicalScriptureReadingDayService.class);
        saintService = mock(SaintService.class);
        feastService = mock(FeastService.class);
        fastingService = mock(FastingService.class);

        builderService = new LiturgicalCalendarDayBuilderService(
                liturgicalScriptureReadingDayService,
                saintService,
                feastService,
                fastingService
        );
    }

    @Test
    void testBuildLiturgicalDay_basicFields() {
        LocalDate date = LocalDate.of(2025, 3, 25);
        String lang = "ar";

        // Dummy: الإنجيل والرسالة
        ScriptureReading gospel = ScriptureReading.builder()
                .reference("لوقا 1: 26-38")
                .content("في ذلك الزمان...")
                .language("ar")
                .build();

        ScriptureReading epistle = ScriptureReading.builder()
                .reference("عبرانيين 2: 11-18")
                .content("يا إخوة...")
                .language("ar")
                .build();

        ScriptureReadingOption option = ScriptureReadingOption.builder()
                .label("عيد البشارة")
                .preferred(true)
                .gospel(gospel)
                .epistle(epistle)
                .alternativeReadings(Collections.emptyList())
                .build();

        LiturgicalScriptureReadingDay readingsDay = LiturgicalScriptureReadingDay.builder()
                .options(List.of(option))
                .build();

        when(liturgicalScriptureReadingDayService.findByDate(date))
                .thenReturn(Optional.of(readingsDay));

        when(saintService.findNamesByLangAndDate(lang, date))
                .thenReturn(List.of("القديس غريغوريوس", "القديسة مريم العذراء"));

        when(feastService.findFixedFeastNameByLangAndDate(lang, date))
                .thenReturn("عيد البشارة");

        when(feastService.findMovableFeastNameByLangAndDate(lang, date))
                .thenReturn(null);

        when(fastingService.getFastingTypeByLangAndDate(lang, date))
                .thenReturn("صوم انقطاعي");

        // نفذ الخدمة
        LiturgicalCalendarDay day = builderService.buildLiturgicalDay(date, lang);

        // تحقق من النتائج
        assertEquals(date, day.getDate());
        assertEquals(List.of("القديس غريغوريوس", "القديسة مريم العذراء"), day.getSaints());
        assertEquals("لوقا 1: 26-38", day.getGospelReading());
        assertEquals("عبرانيين 2: 11-18", day.getEpistleReading());
        assertEquals(Collections.emptyList(), day.getAlternativeReadings());
        assertEquals("عيد البشارة", day.getFixedFeast());
        assertNull(day.getMovableFeast());
        assertEquals("صوم انقطاعي", day.getFastingType());
    }
}
