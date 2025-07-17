package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.EpistleReading;
import at.antiochorthodox.liturgy.model.GospelReading;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.repository.EpistleReadingRepository;
import at.antiochorthodox.liturgy.repository.GospelReadingRepository;
import at.antiochorthodox.liturgy.util.PaschaDateCalculator;
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
        return getReadingsByLiturgicalName(liturgicalName, lang, "liturgicalName", liturgicalName).stream()
                .filter(r -> r.getType().equalsIgnoreCase(type))
                .toList();
    }

    @Override
    public List<ScriptureReading> getReadingsByLiturgicalName(String liturgicalName, String lang, String reason, String reasonDetail) {
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
                    .reason(reason)
                    .reasonDetail(reasonDetail)
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
                    .reason(reason)
                    .reasonDetail(reasonDetail)
                    .build());
        }

        return result;
    }

    @Override
    public ScriptureReading saveReading(ScriptureReading reading) {
        if ("epistle".equalsIgnoreCase(reading.getType())) {
            EpistleReading entity = EpistleReading.builder()
                    .title(reading.getTitle())
                    .reference(reading.getReference())
                    .content(reading.getContent())
                    .liturgicalName(reading.getLiturgicalName())
                    .lang(reading.getLang())
                    .desc(reading.getDesc())
                    .prokeimenon(reading.getProkeimenon())
                    .tone(reading.getTone())
                    .stikheron(reading.getStikheron())
                    .build();
            EpistleReading saved = epistleRepo.save(entity);
            reading.setSourceId(saved.getId());
            return reading;
        } else if ("gospel".equalsIgnoreCase(reading.getType())) {
            GospelReading entity = GospelReading.builder()
                    .title(reading.getTitle())
                    .reference(reading.getReference())
                    .content(reading.getContent())
                    .liturgicalName(reading.getLiturgicalName())
                    .lang(reading.getLang())
                    .desc(reading.getDesc())
                    .build();
            GospelReading saved = gospelRepo.save(entity);
            reading.setSourceId(saved.getId());
            return reading;
        } else {
            throw new IllegalArgumentException("Unsupported reading type: " + reading.getType());
        }
    }

    @Override
    public List<ScriptureReading> saveReadings(List<ScriptureReading> readings) {
        List<ScriptureReading> saved = new ArrayList<>();
        for (ScriptureReading reading : readings) {
            saved.add(saveReading(reading));
        }
        return saved;
    }
}

