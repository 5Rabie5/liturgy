package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
@AllArgsConstructor
@Service
public class LiturgicalCalendarService {
private final PaschaDateCalculator paschaDateCalculator;



    // هل اليوم ضمن الصوم الكبير؟
    public boolean isGreatLent(LocalDate date) {
        LocalDate lentStart = paschaDateCalculator.getGreatLentStart(date.getYear());
        LocalDate lentEnd = paschaDateCalculator.getGreatLentEnd(date.getYear());
        return !date.isBefore(lentStart) && !date.isAfter(lentEnd);
    }

    // هل اليوم بين سبت التجديدات وأحد توما؟
    public boolean isRenewalWeekSaturdayToThomasSunday(LocalDate date) {
        LocalDate renewalSaturday = paschaDateCalculator.getRenewalSaturday(date.getYear());
        LocalDate thomasSunday = paschaDateCalculator.getThomasSunday(date.getYear());
        return !date.isBefore(renewalSaturday) && !date.isAfter(thomasSunday);
    }

    // هل اليوم هو أحد العنصرة؟
    public boolean isPentecostSunday(LocalDate date) {
        return date.equals(paschaDateCalculator.getPentecostDate(date.getYear()));
    }

    // هل اليوم في صوم الميلاد (20-25 كانون الأول)؟
    public boolean isNativityFast(LocalDate date) {
        return date.getMonth() == Month.DECEMBER && date.getDayOfMonth() >= 20 && date.getDayOfMonth() <= 25;
    }

    // هل اليوم في صوم السيدة العذراء (1-15 آب)؟
    public boolean isDormitionFast(LocalDate date) {
        return date.getMonth() == Month.AUGUST && date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 15;
    }

    // يمكنك إضافة أي منطق/فترات إضافية بنفس الأسلوب
}
