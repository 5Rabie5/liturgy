package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.Fasting;
import at.antiochorthodox.liturgy.repository.FastingRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FastingServiceImpl implements FastingService {

    private final FastingRepository repository;
    private final PaschaDateCalculator paschaDateCalculator;

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
        LocalDate paschaDate = paschaDateCalculator.getPaschaDate(year);

        for (Fasting fasting : all) {
            // --- معالجة أصوام اليوم الواحد (startDate أو endDate فقط) ---
            if ((fasting.getStartDate() != null && (fasting.getEndDate() == null || fasting.getEndDate().equalsIgnoreCase("null")))
                    || ((fasting.getStartDate() == null || fasting.getStartDate().equalsIgnoreCase("null")) && fasting.getEndDate() != null)) {
                String theDay = fasting.getStartDate() != null ? fasting.getStartDate() : fasting.getEndDate();
                try {
                    LocalDate only = parseMMDD(theDay, year);
                    Fasting adjusted = Fasting.builder()
                            .id(fasting.getId())
                            .name(fasting.getName())
                            .type(fasting.getType())
                            .symbol(fasting.getSymbol())
                            .desc(fasting.getDesc())
                            .shortdesc(fasting.getShortdesc())
                            .lang(fasting.getLang())
                            .repeatWeekly(fasting.getRepeatWeekly())
                            .startOffsetFromPascha(fasting.getStartOffsetFromPascha())
                            .endOffsetFromPascha(fasting.getEndOffsetFromPascha())
                            .fastingLevel(fasting.getFastingLevel())
                            .fastFree(fasting.getFastFree())
                            .startDate(only.toString())
                            .endDate(only.toString())
                            .allowed(fasting.getAllowed())
                            .notAllowed(fasting.getNotAllowed())
                            .build();
                    result.add(adjusted);
                } catch (Exception e) {
                    System.err.println("خطأ في معالجة صوم اليوم الواحد: " + fasting.getName() + " " + theDay);
                }
                // --- صوم ثابت عادي ---
            } else if (fasting.getStartDate() != null && fasting.getEndDate() != null
                    && !fasting.getStartDate().equalsIgnoreCase("null") && !fasting.getEndDate().equalsIgnoreCase("null")) {
                LocalDate start, end;
                try {
                    start = parseMMDD(fasting.getStartDate(), year);
                    end = parseMMDD(fasting.getEndDate(), year);
                } catch (Exception e) {
                    System.err.println("تاريخ غير صالح في الصوم: " + fasting.getName() + " :: " + e.getMessage());
                    continue;
                }
                Fasting adjusted = Fasting.builder()
                        .id(fasting.getId())
                        .name(fasting.getName())
                        .type(fasting.getType())
                        .symbol(fasting.getSymbol())
                        .desc(fasting.getDesc())
                        .shortdesc(fasting.getShortdesc())
                        .lang(fasting.getLang())
                        .repeatWeekly(fasting.getRepeatWeekly())
                        .startOffsetFromPascha(fasting.getStartOffsetFromPascha())
                        .endOffsetFromPascha(fasting.getEndOffsetFromPascha())
                        .fastingLevel(fasting.getFastingLevel())
                        .fastFree(fasting.getFastFree())
                        .startDate(start != null ? start.toString() : null)
                        .endDate(end != null ? end.toString() : null)
                        .allowed(fasting.getAllowed())
                        .notAllowed(fasting.getNotAllowed())
                        .build();
                result.add(adjusted);

                // --- متغير بالنسبة للفصح ---
            } else if (fasting.getStartOffsetFromPascha() != null && fasting.getEndOffsetFromPascha() != null) {
                LocalDate start = paschaDate.plusDays(fasting.getStartOffsetFromPascha());
                LocalDate end = paschaDate.plusDays(fasting.getEndOffsetFromPascha());
                Fasting adjusted = Fasting.builder()
                        .id(fasting.getId())
                        .name(fasting.getName())
                        .type(fasting.getType())
                        .symbol(fasting.getSymbol())
                        .desc(fasting.getDesc())
                        .shortdesc(fasting.getShortdesc())
                        .lang(fasting.getLang())
                        .repeatWeekly(fasting.getRepeatWeekly())
                        .startOffsetFromPascha(fasting.getStartOffsetFromPascha())
                        .endOffsetFromPascha(fasting.getEndOffsetFromPascha())
                        .fastingLevel(fasting.getFastingLevel())
                        .fastFree(fasting.getFastFree())
                        .startDate(start.toString())
                        .endDate(end.toString())
                        .allowed(fasting.getAllowed())
                        .notAllowed(fasting.getNotAllowed())
                        .build();
                result.add(adjusted);

                // --- صوم أسبوعي فقط ---
            } else if (Boolean.TRUE.equals(fasting.getRepeatWeekly())) {
                result.add(fasting);
            }
        }
        return result;
    }


    // تحويل MM-dd لسنة معينة
    private LocalDate parseMMDD(String mmdd, int year) {
        if (mmdd == null || mmdd.trim().isEmpty() || mmdd.equalsIgnoreCase("null"))
            throw new IllegalArgumentException("MMDD date string is null or empty");
        return LocalDate.parse(year + "-" + mmdd);
    }

    @Override
    public Optional<Fasting> getFastingForDate(String lang, LocalDate date) {
        List<Fasting> all = getAllFastingForYear(lang, date.getYear());
        return all.stream()
                .filter(fasting -> isFastingCoversDate(fasting, date))
                .map(fasting -> getFastingWithExceptions(fasting, date))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparingInt(Fasting::getFastingLevel))
                .findFirst();
    }

    // دالة مساعدة: تبحث إذا كان هذا الصوم يغطي هذا اليوم (ثابت/متغير/أسبوعي)
    private boolean isFastingCoversDate(Fasting fasting, LocalDate date) {
        // صوم أسبوعي (الأربعاء أو الجمعة)
        if (Boolean.TRUE.equals(fasting.getRepeatWeekly()) &&
                (date.getDayOfWeek().getValue() == 3 || date.getDayOfWeek().getValue() == 5)) {
            return true;
        }
        // صوم بفترة start/end
        if (fasting.getStartDate() != null && fasting.getEndDate() != null) {
            try {
                LocalDate start = LocalDate.parse(fasting.getStartDate());
                LocalDate end = LocalDate.parse(fasting.getEndDate());
                return (date.isEqual(start) || date.isAfter(start)) &&
                        (date.isEqual(end) || date.isBefore(end));
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }


    @Override
    public String getFastingTypeByLangAndDate(String lang, LocalDate date) {
        Optional<Fasting> fasting = getFastingForDate(lang, date);
        if (fasting.isPresent()) {
            Fasting f = fasting.get();
            if (f.getSymbol() != null && !f.getSymbol().isEmpty()) {
                return f.getSymbol() + " (درجة " + f.getFastingLevel() + ")";
            }
            return f.getName() + " (درجة " + f.getFastingLevel() + ")";
        }
        return "بدون صوم خاص";
    }

    @Override
    public String getFastingEvelByLangAndDate(String lang, LocalDate date) {
        Optional<Fasting> fasting = getFastingForDate(lang, date);
        if (fasting.isPresent()) {
            Fasting f = fasting.get();
            return String.valueOf(f.getFastingLevel());
        }
        return "بدون صوم";
    }

    // === الاستثناءات
    private int applyFastingExceptions(
            LocalDate date,
            Integer fastingLevel,
            String fastingType,
            Boolean isWeeklyFasting) {

        if (Boolean.TRUE.equals(isWeeklyFasting) && (date.getDayOfWeek().getValue() == 3 || date.getDayOfWeek().getValue() == 5)) {
            if (isEpiphanyForefeastOrFeast(date)) return 0;
            if (isWeekOfPublicanAndPharisee(date)) return 0;
            if (isAfterPaschaUntilAscension(date)) return 0;
            if (isApodosisOfPascha(date) || isMidPentecost(date)) return 3;
            if ("apostles".equals(fastingType)) return 1;
        }
        if (fastingLevel != null && fastingLevel == 1 &&
                (date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7)) {
            return 2;
        }
        if (isAnnunciation(date) || isPalmSunday(date) || isTransfiguration(date)) return 3;
        return fastingLevel != null ? fastingLevel : 0;
    }

    private boolean isEpiphanyForefeastOrFeast(LocalDate date) {
        return (date.getMonthValue() == 1 && (date.getDayOfMonth() == 5 || date.getDayOfMonth() == 6));
    }

    private boolean isWeekOfPublicanAndPharisee(LocalDate date) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        LocalDate start = pascha.minusDays(70); // أحد الفريسي والعشار
        LocalDate end = start.plusDays(6); // أسبوع كامل
        return !date.isBefore(start) && !date.isAfter(end);
    }

    private boolean isAfterPaschaUntilAscension(LocalDate date) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        LocalDate ascension = pascha.plusDays(39); // الصعود بعد الفصح بـ39 يوم
        return date.isAfter(pascha) && date.isBefore(ascension);
    }

    private boolean isApodosisOfPascha(LocalDate date) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        return date.equals(pascha.plusDays(38)); // وداع الفصح
    }

    private boolean isMidPentecost(LocalDate date) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        return date.equals(pascha.plusDays(24)); // منتصف الخمسين
    }

    private boolean isAnnunciation(LocalDate date) {
        return (date.getMonthValue() == 3 && date.getDayOfMonth() == 25);
    }

    private boolean isPalmSunday(LocalDate date) {
        LocalDate pascha = paschaDateCalculator.getPaschaDate(date.getYear());
        return date.equals(pascha.minusDays(7));
    }

    private boolean isTransfiguration(LocalDate date) {
        return (date.getMonthValue() == 8 && date.getDayOfMonth() == 6);
    }

    // MM-dd مقارنة (إذا أردتها)
    private boolean isInRange(String current, String start, String end) {
        int c = Integer.parseInt(current.replace("-", ""));
        int s = Integer.parseInt(start.replace("-", ""));
        int e = Integer.parseInt(end.replace("-", ""));
        if (s <= e) {
            return (c >= s && c <= e);
        } else {
            return (c >= s || c <= e);
        }
    }

    /**
     * ترجع كائن Fasting بعد تطبيق الاستثناءات حسب التاريخ
     * Optional.empty() إذا اليوم بلا صوم (استثناء)
     */
    private Optional<Fasting> getFastingWithExceptions(Fasting fasting, LocalDate date) {
        int newLevel = applyFastingExceptions(
                date,
                fasting.getFastingLevel(),
                fasting.getType(),
                fasting.getRepeatWeekly()
        );
        if (newLevel == 0) return Optional.empty();
        return Optional.of(
                Fasting.builder()
                        .id(fasting.getId())
                        .lang(fasting.getLang())
                        .name(fasting.getName())
                        .type(fasting.getType())
                        .startDate(fasting.getStartDate())
                        .endDate(fasting.getEndDate())
                        .startOffsetFromPascha(fasting.getStartOffsetFromPascha())
                        .endOffsetFromPascha(fasting.getEndOffsetFromPascha())
                        .repeatWeekly(fasting.getRepeatWeekly())
                        .fastingLevel(newLevel)
                        .symbol(fasting.getSymbol())
                        .allowed(fasting.getAllowed())
                        .notAllowed(fasting.getNotAllowed())
                        .fastFree(fasting.getFastFree())
                        .shortdesc(fasting.getShortdesc())
                        .desc(fasting.getDesc())
                        .build()
        );
    }
}
