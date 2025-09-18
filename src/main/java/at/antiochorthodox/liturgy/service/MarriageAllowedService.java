package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.MarriageAllowedResponse;
import at.antiochorthodox.liturgy.model.MarriageBanReason;
import at.antiochorthodox.liturgy.repository.MarriageBanReasonRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

@Service
public class MarriageAllowedService {

    // استخدم نفس الأكواد الموجودة في ملف JSON المُحدَّث
    private static final String CODE_EVE_OF_THEOPHANY       = "EVE_OF_THEOPHANY";
    private static final String CODE_LENT_TO_RENEWAL_BLOCK  = "LENT_TO_RENEWAL_BLOCK";
    private static final String CODE_PENTECOST_SUNDAY       = "PENTECOST_SUNDAY";
    private static final String CODE_FAST_OF_THEOTOKOS      = "FAST_OF_THEOTOKOS";
    private static final String CODE_BEHEADING_OF_JOHN      = "BEHEADING_OF_JOHN";
    private static final String CODE_EXALTATION_OF_CROSS    = "EXALTATION_OF_THE_CROSS";
    private static final String CODE_NATIVITY_FAST_SHORT    = "NATIVITY_FAST_SHORT";
    private static final String CODE_ALLOWED                = "ALLOWED";

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

        // 1) عشية الظهور الإلهي — 5 كانون الثاني
        if (date.getMonth() == Month.JANUARY && date.getDayOfMonth() == 5) {
            return new MarriageAllowedResponse(date, false, getMessage(CODE_EVE_OF_THEOPHANY, lang));
        }

        // 2) فترة واحدة: من أربعاء مرفع الجبن إلى سبت التجديدات (قبل أحد توما)
        // أحد مرفع الجبن = فصح - 7 أسابيع
        LocalDate cheesefareSunday = pascha.minusWeeks(7);
        // أربعاء مرفع الجبن (الأربعاء نفسه إذا كان اليوم أحد/اثنين/ثلاثاء/أربعاء من نفس الأسبوع)
        LocalDate cheesefareWednesday = cheesefareSunday.with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY));
        // سبت التجديدات = فصح + 6 أيام
        LocalDate renewalSaturday = pascha.plusDays(6);

        if (!date.isBefore(cheesefareWednesday) && !date.isAfter(renewalSaturday)) {
            return new MarriageAllowedResponse(date, false, getMessage(CODE_LENT_TO_RENEWAL_BLOCK, lang));
        }

        // 3) أحد العنصرة = فصح + 49 يوم
        if (date.equals(pascha.plusDays(49))) {
            return new MarriageAllowedResponse(date, false, getMessage(CODE_PENTECOST_SUNDAY, lang));
        }

        // 4) صوم السيدة (1–15 آب)
        if (date.getMonth() == Month.AUGUST && date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 15) {
            return new MarriageAllowedResponse(date, false, getMessage(CODE_FAST_OF_THEOTOKOS, lang));
        }

        // 5) قطع رأس يوحنا — 29 آب
        if (date.getMonth() == Month.AUGUST && date.getDayOfMonth() == 29) {
            return new MarriageAllowedResponse(date, false, getMessage(CODE_BEHEADING_OF_JOHN, lang));
        }

        // 6) رفع الصليب — 14 أيلول
        if (date.getMonth() == Month.SEPTEMBER && date.getDayOfMonth() == 14) {
            return new MarriageAllowedResponse(date, false, getMessage(CODE_EXALTATION_OF_CROSS, lang));
        }

        // 7) صوم الميلاد القصير حسب الصورة — 20–25 كانون الأول
        if (date.getMonth() == Month.DECEMBER && date.getDayOfMonth() >= 20 && date.getDayOfMonth() <= 25) {
            return new MarriageAllowedResponse(date, false, getMessage(CODE_NATIVITY_FAST_SHORT, lang));
        }

        // إذا ما في مانع
        return new MarriageAllowedResponse(date, true, getMessage(CODE_ALLOWED, lang));
    }

    private String getMessage(String code, String lang) {
        return banReasonRepository.findByCode(code)
                .map((MarriageBanReason reason) -> {
                    Map<String, String> msgMap = reason.getMessage();
                    // جرّب اللغة المطلوبة، ثم الإنجليزية، ثم fallback على نص افتراضي واضح
                    return msgMap.getOrDefault(lang, msgMap.getOrDefault("en", "Marriage restriction: " + code));
                })
                .orElse("Marriage restriction: " + code);
    }
}
