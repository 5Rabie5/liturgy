package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

@Service
public class MarriageAllowedService {

    private final LiturgicalCalendarService calendarService;

    @Autowired
    public MarriageAllowedService(LiturgicalCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    public MarriageAllowedResponse isMarriageAllowed(LocalDate date) {
        // 1. اليوم السابق للظهور الإلهي
        if (date.getMonth() == Month.JANUARY && date.getDayOfMonth() == 5)
            return new MarriageAllowedResponse(date, false, "اليوم السابق لعيد الظهور الإلهي (لا يسمح بالزواج)");

        // 2. الصوم الكبير
        if (calendarService.isGreatLent(date))
            return new MarriageAllowedResponse(date, false, "الصوم الكبير (لا يسمح بالزواج)");

        // 3. صوم السيدة العذراء
        if (calendarService.isDormitionFast(date))
            return new MarriageAllowedResponse(date, false, "صوم السيدة العذراء (لا يسمح بالزواج)");

        // 4. تذكار قطع رأس يوحنا المعمدان
        if (date.getMonth() == Month.AUGUST && date.getDayOfMonth() == 29)
            return new MarriageAllowedResponse(date, false, "تذكار قطع رأس يوحنا المعمدان (لا يسمح بالزواج)");

        // 5. تذكار رفع الصليب الكريم
        if (date.getMonth() == Month.SEPTEMBER && date.getDayOfMonth() == 14)
            return new MarriageAllowedResponse(date, false, "تذكار رفع الصليب الكريم (لا يسمح بالزواج)");

        // 6. صوم الميلاد
        if (calendarService.isNativityFast(date))
            return new MarriageAllowedResponse(date, false, "صوم الميلاد (لا يسمح بالزواج)");

        // 7. أحد العنصرة
        if (calendarService.isPentecostSunday(date))
            return new MarriageAllowedResponse(date, false, "أحد العنصرة (لا يسمح بالزواج)");

        // إذا لم يتحقق أي شرط
        return new MarriageAllowedResponse(date, true, "مسموح بالزواج في هذا اليوم");
    }
}
