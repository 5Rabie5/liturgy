package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.EpistleReading;
import at.antiochorthodox.liturgy.model.GospelReading;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.repository.EpistleReadingRepository;
import at.antiochorthodox.liturgy.repository.GospelReadingRepository;
import at.antiochorthodox.liturgy.service.LiturgicalWeekService;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
import at.antiochorthodox.liturgy.service.ScriptureReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScriptureReadingServiceImpl implements ScriptureReadingService {

    private final EpistleReadingRepository epistleRepo;
    private final GospelReadingRepository gospelRepo;
    private final LiturgicalWeekService liturgicalWeekService;
    private final PaschaDateCalculator paschaDateCalculator;

    @Autowired
    public ScriptureReadingServiceImpl(
            EpistleReadingRepository epistleRepo,
            GospelReadingRepository gospelRepo,
            LiturgicalWeekService liturgicalWeekService,
            PaschaDateCalculator paschaDateCalculator
    ) {
        this.epistleRepo = epistleRepo;
        this.gospelRepo = gospelRepo;
        this.liturgicalWeekService = liturgicalWeekService;
        this.paschaDateCalculator = paschaDateCalculator;
    }

    @Override
    public List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String type, String lang) {
        String liturgicalName = liturgicalWeekService.findLiturgicalNameForDate(
                date,
                paschaDateCalculator.getPaschaDate(date.getYear()),
                paschaDateCalculator.getNextPhariseePublicanSunday(paschaDateCalculator.getPaschaDate(date.getYear())),
                lang
        );

        return getReadingsByLiturgicalName(liturgicalName, lang).stream()
                .filter(r -> r.getType().equalsIgnoreCase(type))
                .toList();
    }

    @Override
    public List<ScriptureReading> getReadingsByLiturgicalName(String liturgicalName, String lang) {
        List<ScriptureReading> result = new ArrayList<>();

        List<EpistleReading> epistles = epistleRepo.findByLiturgicalNameAndLang(liturgicalName, lang);
        for (EpistleReading e : epistles) {
            result.add(ScriptureReading.builder()
                    .title(e.getTitle())
                    .reference(e.getReference())
                    .content(e.getContent())
                    .type("epistle")
                    .sourceId(e.getId())
                    .liturgicalName(e.getLiturgicalName())
                    .lang(e.getLang())
                    .desc(e.getDesc())
                    .prokeimenon(e.getProkeimenon())
                    .tone(e.getTone())
                    .stikheron(e.getStikheron())
                    .build());
        }

        List<GospelReading> gospels = gospelRepo.findByLiturgicalNameAndLang(liturgicalName, lang);
        for (GospelReading g : gospels) {
            result.add(ScriptureReading.builder()
                    .title(g.getTitle())
                    .reference(g.getReference())
                    .content(g.getContent())
                    .type("gospel")
                    .sourceId(g.getId())
                    .liturgicalName(g.getLiturgicalName())
                    .lang(g.getLang())
                    .desc(g.getDesc())
                    .build());
        }

        return result;
    }

    @Override
    public List<ScriptureReading> getAllReadingsForDay(LocalDate date, String lang) {
        String liturgicalName = liturgicalWeekService.findLiturgicalNameForDate(
                date,
                paschaDateCalculator.getPaschaDate(date.getYear()),
                paschaDateCalculator.getNextPhariseePublicanSunday(paschaDateCalculator.getPaschaDate(date.getYear())),
                lang
        );

        return getReadingsByLiturgicalName(liturgicalName, lang);
    }
}
