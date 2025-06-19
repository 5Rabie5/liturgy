package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;

@Service
public class LiturgicalCalendarService {

    // هل اليوم ضمن الصوم الكبير؟
    public boolean isGreatLent(LocalDate date) {
        LocalDate lentStart = PaschaDateCalculator.getGreatLentStart(date.getYear());
        LocalDate lentEnd = PaschaDateCalculator.getGreatLentEnd(date.getYear());
        return !date.isBefore(lentStart) && !date.isAfter(lentEnd);
    }

    // هل اليوم بين سبت التجديدات وأحد توما؟
    public boolean isRenewalWeekSaturdayToThomasSunday(LocalDate date) {
        LocalDate renewalSaturday = PaschaDateCalculator.getRenewalSaturday(date.getYear());
        LocalDate thomasSunday = PaschaDateCalculator.getThomasSunday(date.getYear());
        return !date.isBefore(renewalSaturday) && !date.isAfter(thomasSunday);
    }

    // هل اليوم هو أحد العنصرة؟
    public boolean isPentecostSunday(LocalDate date) {
        return date.equals(PaschaDateCalculator.getPentecostDate(date.getYear()));
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
