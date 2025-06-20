package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Fasting;
import at.antiochorthodox.liturgy.model.Feast;
import at.antiochorthodox.liturgy.repository.FastingRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FastingServiceImpl implements FastingService {

    private final FastingRepository repository;
    private final  PaschaDateCalculator paschaDateCalculator;
    @Override
    public Optional<Fasting> getById(String id) {
        return repository.findById(id);
    }

    @Override
    public List<Fasting> getByLanguage(String lang) {
        return repository.findByLang(lang);
    }

    @Override
    public List<Fasting> getByLanguageAndType(String lang, String type) {
        return repository.findByLangAndType(lang, type);
    }

    @Override
    public List<Fasting> getWeeklyFasting(String lang) {
        return repository.findByLangAndRepeatWeeklyTrue(lang);
    }

    @Override
    public Fasting save(Fasting fasting) {
        return repository.save(fasting);
    }

    @Override
    public List<Fasting> saveAll(List<Fasting> fastingList) {
        return repository.saveAll(fastingList);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public List<Fasting> getAllFastingForYear(String lang, int year) {
        List<Fasting> all = repository.findByLang(lang);
        List<Fasting> result = new ArrayList<>();

        LocalDate paschaDate = PaschaDateCalculator.getPaschaDate(year);

        for (Fasting fasting : all) {
            if (fasting.getStartDate() != null && fasting.getEndDate() != null) {
                result.add(fasting);
            } else if (fasting.getStartOffsetFromPascha() != null && fasting.getEndOffsetFromPascha() != null) {
                LocalDate start = paschaDate.plusDays(fasting.getStartOffsetFromPascha());
                LocalDate end = paschaDate.plusDays(fasting.getEndOffsetFromPascha());

                Fasting adjusted = Fasting.builder()
                        .id(fasting.getId())
                        .name(fasting.getName())
                        .type(fasting.getType())
                        .symbol(fasting.getSymbol())
                        .description(fasting.getDescription())
                        .shortdesc(fasting.getShortdesc())
                        .lang(fasting.getLang())
                        .repeatWeekly(fasting.isRepeatWeekly())
                        .startOffsetFromPascha(fasting.getStartOffsetFromPascha())
                        .endOffsetFromPascha(fasting.getEndOffsetFromPascha())
                        .fastingLevel(fasting.getFastingLevel())
                        .fastFree(fasting.isFastFree())
                        .build();

                adjusted.setStartDate(start.toString());
                adjusted.setEndDate(end.toString());

                result.add(adjusted);
            }
        }

        return result;
    }
    @Override
    public Optional<Fasting> getFastingForDate(String lang, LocalDate date) {
        List<Fasting> all = getAllFastingForYear(lang, date.getYear());
        // ابحث عن الصوم الذي يغطي هذا اليوم حسب startDate/endDate
        return all.stream()
                .filter(fasting -> {
                    LocalDate start = LocalDate.parse(fasting.getStartDate());
                    LocalDate end = LocalDate.parse(fasting.getEndDate());
                    return (date.isEqual(start) || date.isAfter(start)) &&
                            (date.isEqual(end) || date.isBefore(end));
                })
                .sorted(Comparator.comparingInt(Fasting::getFastingLevel)) // أو حسب الأولوية
                .findFirst();
    }



    @Override
    public String getFastingTypeByLangAndDate(String lang, LocalDate date) {
        LocalDate paschaDate = PaschaDateCalculator.getPaschaDate(date.getYear());
        List<Fasting> fastings = repository.findByLang(lang);

        // تكرار على جميع الأصوام المعرّفة
        for (Fasting fast : fastings) {
            // 1. حالة repeatWeekly (أربعاء أو جمعة)
            if (fast.isRepeatWeekly()) {
                if (date.getDayOfWeek().getValue() == 3 || date.getDayOfWeek().getValue() == 5) { // Wednesday=3, Friday=5
                    return fast.getSymbol() != null ? fast.getSymbol() : fast.getName();
                }
            }
            // 2. حالة تواريخ ثابتة (MM-dd)
            if (fast.getStartDate() != null && fast.getEndDate() != null) {
                String current = String.format("%02d-%02d", date.getMonthValue(), date.getDayOfMonth());
                boolean inRange = isInRange(current, fast.getStartDate(), fast.getEndDate());
                if (inRange) {
                    return fast.getSymbol() != null ? fast.getSymbol() : fast.getName();
                }
            }
            // 3. حالة offsets من الفصح (متغير)
            if (fast.getStartOffsetFromPascha() != null && fast.getEndOffsetFromPascha() != null && paschaDate != null) {
                LocalDate start = paschaDate.plusDays(fast.getStartOffsetFromPascha());
                LocalDate end = paschaDate.plusDays(fast.getEndOffsetFromPascha());
                if ((date.isEqual(start) || date.isAfter(start)) && (date.isEqual(end) || date.isBefore(end))) {
                    return fast.getSymbol() != null ? fast.getSymbol() : fast.getName();
                }
            }
        }
        // إذا لا يوجد أي صوم يطابق
        return "بدون صوم خاص";
    }

    // فحص أن current (MM-dd) بين start وend (يدعم عبور السنة)
    private boolean isInRange(String current, String start, String end) {
        int c = Integer.parseInt(current.replace("-", ""));
        int s = Integer.parseInt(start.replace("-", ""));
        int e = Integer.parseInt(end.replace("-", ""));
        if (s <= e) {
            return (c >= s && c <= e);
        } else { // إذا كان الصوم يعبر نهاية السنة (مثلاً: من 11-15 إلى 01-05)
            return (c >= s || c <= e);
        }
    }
}