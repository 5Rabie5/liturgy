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

        // Epistle readings
        List<EpistleReading> epistles = epistleRepo.findByLiturgicalNameAndLang(liturgicalName, lang);
        for (EpistleReading e : epistles) {
            result.add(ScriptureReading.builder()
                    .title(e.getTitle())
                    .reference(e.getReference())
                    .type("epistle")
                    .sourceId(e.getId())
                    .liturgicalName(e.getLiturgicalName())
                    .lang(e.getLang())
                    .desc(e.getDesc())
                    // --- الحقول الرسالية ---
                    .prokeimenon1Title(e.getProkeimenon1Title())
                    .prokeimenon1Tone(e.getProkeimenon1Tone())
                    .prokeimenon1Verse(e.getProkeimenon1Verse())
                    .prokeimenon1Stikheron(e.getProkeimenon1Stikheron())
                    .prokeimenon2Title(e.getProkeimenon2Title())
                    .prokeimenon2Tone(e.getProkeimenon2Tone())
                    .prokeimenon2Verse(e.getProkeimenon2Verse())
                    .prokeimenon2Stikheron(e.getProkeimenon2Stikheron())
                    .readingTitle(e.getReadingTitle())
                    .readingContent(e.getReadingContent())
                    .alleluiaTitle(e.getAlleluiaTitle())
                    .alleluiaTone(e.getAlleluiaTone())
                    .alleluiaVerse(e.getAlleluiaVerse())
                    .alleluiaStikheron(e.getAlleluiaStikheron())
                    .reason(reason)
                    .reasonDetail(reasonDetail)
                    .build());
        }

        // Gospel readings
        List<GospelReading> gospels = gospelRepo.findByLiturgicalNameAndLang(liturgicalName, lang);
        for (GospelReading g : gospels) {
            result.add(ScriptureReading.builder()
                    .title(g.getTitle())
                    .reference(g.getReference())
                    .type("gospel")
                    .sourceId(g.getId())
                    .liturgicalName(g.getLiturgicalName())
                    .lang(g.getLang())
                    .desc(g.getDesc())
                    // --- الإنجيل فقط ---
                    .readingTitle(g.getReadingTitle())
                    .readingContent(g.getReadingContent())
                    .prokeimenonTitle(g.getProkeimenonTitle())
                    .prokeimenonTone(g.getProkeimenonTone())
                    .prokeimenonVerse(g.getProkeimenonVerse())
                    .alleluiaTitle(g.getAlleluiaTitle())
                    .alleluiaTone(g.getAlleluiaTone())
                    .alleluiaVerse(g.getAlleluiaVerse())
                    .alleluiaStikheron(g.getAlleluiaStikheron())
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
                    .type("epistle")
                    .liturgicalName(reading.getLiturgicalName())
                    .lang(reading.getLang())
                    .desc(reading.getDesc())
                    // --- الحقول الرسالية ---
                    .prokeimenon1Title(reading.getProkeimenon1Title())
                    .prokeimenon1Tone(reading.getProkeimenon1Tone())
                    .prokeimenon1Verse(reading.getProkeimenon1Verse())
                    .prokeimenon1Stikheron(reading.getProkeimenon1Stikheron())
                    .prokeimenon2Title(reading.getProkeimenon2Title())
                    .prokeimenon2Tone(reading.getProkeimenon2Tone())
                    .prokeimenon2Verse(reading.getProkeimenon2Verse())
                    .prokeimenon2Stikheron(reading.getProkeimenon2Stikheron())
                    .readingTitle(reading.getReadingTitle())
                    .readingContent(reading.getReadingContent())
                    .alleluiaTitle(reading.getAlleluiaTitle())
                    .alleluiaTone(reading.getAlleluiaTone())
                    .alleluiaVerse(reading.getAlleluiaVerse())
                    .alleluiaStikheron(reading.getAlleluiaStikheron())
                    .build();
            EpistleReading saved = epistleRepo.save(entity);
            reading.setSourceId(saved.getId());
            return reading;
        } else if ("gospel".equalsIgnoreCase(reading.getType())) {
            GospelReading entity = GospelReading.builder()
                    .title(reading.getTitle())
                    .reference(reading.getReference())
                    .type("gospel")
                    .liturgicalName(reading.getLiturgicalName())
                    .lang(reading.getLang())
                    .desc(reading.getDesc())
                    // --- الإنجيل فقط ---
                    .readingTitle(reading.getReadingTitle())
                    .readingContent(reading.getReadingContent())
                    .prokeimenonTitle(reading.getProkeimenonTitle())
                    .prokeimenonTone(reading.getProkeimenonTone())
                    .prokeimenonVerse(reading.getProkeimenonVerse())
                    .alleluiaTitle(reading.getAlleluiaTitle())
                    .alleluiaTone(reading.getAlleluiaTone())
                    .alleluiaVerse(reading.getAlleluiaVerse())
                    .alleluiaStikheron(reading.getAlleluiaStikheron())
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
