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

        LocalDate paschaDate = PaschaDateCalculator.calculateOrthodoxEaster(year);

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
}