package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.model.MarriageBanReason;
import at.antiochorthodox.liturgy.repository.MarriageBanReasonRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

@Service
public class MarriageAllowedService {

    private final MarriageBanReasonRepository banReasonRepository;
    private final PaschaDateCalculator paschaCalculator;

    public MarriageAllowedService(
            MarriageBanReasonRepository banReasonRepository,
            PaschaDateCalculator paschaCalculator
    ) {
        this.banReasonRepository = banReasonRepository;
        this.paschaCalculator = paschaCalculator;
    }

    public MarriageAllowedResponse isMarriageAllowed(LocalDate date, String lang) {
        int year = date.getYear();
        LocalDate pascha = paschaCalculator.getPaschaDate(year);

        // 1. اليوم السابق للظهور الإلهي - 5 كانون الثاني
        if (date.getMonth() == Month.JANUARY && date.getDayOfMonth() == 5)
            return new MarriageAllowedResponse(date, false, getMessage("BEFORE_EPIPHANY", lang));

        // 2. من أربعاء الجبن إلى سبت التجديدات
        LocalDate cheeseSunday = pascha.minusWeeks(8); // أحد مرفع الجبن
        LocalDate cheeseWednesday = cheeseSunday.with(DayOfWeek.WEDNESDAY);
        LocalDate saturdayBeforeThomas = pascha.plusDays(6); // سبت التجديدات

        if (!date.isBefore(cheeseWednesday) && !date.isAfter(saturdayBeforeThomas))
            return new MarriageAllowedResponse(date, false, getMessage("CHEESE_TO_RENEWAL", lang));

        // 3. أحد العنصرة
        if (date.equals(paschaCalculator.getPentecostDate(year)))
            return new MarriageAllowedResponse(date, false, getMessage("PENTECOST_SUNDAY", lang));

        // 4. صوم السيدة العذراء (1 - 15 آب)
        if (date.getMonth() == Month.AUGUST && date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 15)
            return new MarriageAllowedResponse(date, false, getMessage("DORMITION_FAST", lang));

        // 5. قطع رأس يوحنا المعمدان - 29 آب
        if (date.getMonth() == Month.AUGUST && date.getDayOfMonth() == 29)
            return new MarriageAllowedResponse(date, false, getMessage("JOHN_BEHEADING", lang));

        // 6. رفع الصليب الكريم - 14 أيلول
        if (date.getMonth() == Month.SEPTEMBER && date.getDayOfMonth() == 14)
            return new MarriageAllowedResponse(date, false, getMessage("HOLY_CROSS", lang));

        // 7. صوم الميلاد - 20 إلى 25 كانون الأول
        if (date.getMonth() == Month.DECEMBER && date.getDayOfMonth() >= 20 && date.getDayOfMonth() <= 25)
            return new MarriageAllowedResponse(date, false, getMessage("NATIVITY_FAST", lang));

        // إذا لم ينطبق أي سبب
        return new MarriageAllowedResponse(date, true, getMessage("ALLOWED", lang));
    }

    private String getMessage(String code, String lang) {
        return banReasonRepository.findByCode(code)
                .map((MarriageBanReason reason) -> {
                    Map<String, String> msgMap = reason.getMessage();
                    return msgMap.getOrDefault(lang, msgMap.getOrDefault("en", "Marriage restriction: " + code));
                })
                .orElse("Marriage restriction: " + code);
    }

}
