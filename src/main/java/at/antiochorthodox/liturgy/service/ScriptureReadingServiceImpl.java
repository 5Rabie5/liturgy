package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.LiturgicalDayContext;
import at.antiochorthodox.liturgy.model.EpistleReading;
import at.antiochorthodox.liturgy.model.GospelReading;
import at.antiochorthodox.liturgy.model.ScriptureReading;
import at.antiochorthodox.liturgy.repository.EpistleReadingRepository;
import at.antiochorthodox.liturgy.repository.GospelReadingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScriptureReadingServiceImpl implements ScriptureReadingService {

    private final EpistleReadingRepository epistleRepo;
    private final GospelReadingRepository gospelRepo;
    private final LiturgicalDayContextService liturgicalDayContextService;

    public ScriptureReadingServiceImpl(
            EpistleReadingRepository epistleRepo,
            GospelReadingRepository gospelRepo,
            LiturgicalDayContextService liturgicalDayContextService
    ) {
        this.epistleRepo = epistleRepo;
        this.gospelRepo = gospelRepo;
        this.liturgicalDayContextService = liturgicalDayContextService;
    }

    @Override
    public List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String type, String lang) {
        return getReadingsByDateAndType(date, null, type, lang);
    }

    @Override
    public List<ScriptureReading> getReadingsByDateAndType(LocalDate date, String slot, String type, String lang) {
        String normalizedLang = normalizeLang(lang);
        LiturgicalDayContext context = liturgicalDayContextService.resolveForDate(date, normalizedLang, slot);
        if (context == null) {
            return List.of();
        }
        return getReadingsByResolvedContext(context, type, normalizedLang);
    }

    @Override
    public List<ScriptureReading> getReadingsByDayKey(String dayKey, String type, String lang) {
        return getReadingsByDayKey(dayKey, null, type, lang);
    }

    @Override
    public List<ScriptureReading> getReadingsByDayKey(String dayKey, String slot, String type, String lang) {
        String normalizedLang = normalizeLang(lang);
        LiturgicalDayContext context = liturgicalDayContextService.resolveByDayKey(dayKey, normalizedLang, slot);
        if (context == null) {
            return List.of();
        }
        return getReadingsByResolvedContext(context, type, normalizedLang);
    }

    @Override
    public List<ScriptureReading> getReadingsByLiturgicalName(String liturgicalName, String type, String lang) {
        if (liturgicalName == null || liturgicalName.isBlank()) {
            return List.of();
        }

        String normalizedLang = normalizeLang(lang);
        String t = normalizeType(type);
        boolean needEpistle = "any".equals(t) || "epistle".equals(t);
        boolean needGospel = "any".equals(t) || "gospel".equals(t);

        List<ScriptureReading> result = new ArrayList<>();

        if (needEpistle) {
            for (EpistleReading e : epistleRepo.findByLiturgicalNameAndLang(liturgicalName, normalizedLang)) {
                result.add(toDto(e, null, null, null));
            }
        }

        if (needGospel) {
            for (GospelReading g : gospelRepo.findByLiturgicalNameAndLang(liturgicalName, normalizedLang)) {
                result.add(toDto(g, null, null, null));
            }
        }

        return result;
    }

    @Override
    public ScriptureReading getReadingByKey(String readingKey, String lang) {
        if (!hasText(readingKey)) {
            return null;
        }

        String normalizedLang = normalizeLang(lang);

        List<EpistleReading> epistles = epistleRepo.findByReadingKeyAndLang(readingKey, normalizedLang);
        if (epistles != null && !epistles.isEmpty()) {
            return toDto(epistles.get(0), null, null, null);
        }

        List<GospelReading> gospels = gospelRepo.findByReadingKeyAndLang(readingKey, normalizedLang);
        if (gospels != null && !gospels.isEmpty()) {
            return toDto(gospels.get(0), null, null, null);
        }

        return null;
    }

    @Override
    public ScriptureReading saveReading(ScriptureReading reading) {
        String resolvedReadingKey = firstNonBlank(reading.getReadingKey(), reading.getLiturgicalName());
        if (resolvedReadingKey == null) {
            throw new IllegalArgumentException("readingKey or liturgicalName is required");
        }

        if ("epistle".equalsIgnoreCase(reading.getType())) {
            EpistleReading entity = EpistleReading.builder()
                    .readingKey(resolvedReadingKey)
                    .title(reading.getTitle())
                    .reference(reading.getReference())
                    .type("epistle")
                    .liturgicalName(reading.getLiturgicalName())
                    .lang(reading.getLang())
                    .desc(reading.getDesc())
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
            reading.setReadingKey(saved.getReadingKey());
            reading.setSourceId(saved.getId());
            return reading;
        }

        if ("gospel".equalsIgnoreCase(reading.getType())) {
            GospelReading entity = GospelReading.builder()
                    .readingKey(resolvedReadingKey)
                    .title(reading.getTitle())
                    .reference(reading.getReference())
                    .type("gospel")
                    .liturgicalName(reading.getLiturgicalName())
                    .lang(reading.getLang())
                    .desc(reading.getDesc())
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
            reading.setReadingKey(saved.getReadingKey());
            reading.setSourceId(saved.getId());
            return reading;
        }

        throw new IllegalArgumentException("Unsupported reading type: " + reading.getType());
    }

    @Override
    public List<ScriptureReading> saveReadings(List<ScriptureReading> readings) {
        List<ScriptureReading> saved = new ArrayList<>();
        for (ScriptureReading reading : readings) {
            saved.add(saveReading(reading));
        }
        return saved;
    }

    private List<ScriptureReading> getReadingsByResolvedContext(LiturgicalDayContext context, String type, String lang) {
        String t = normalizeType(type);
        boolean needEpistle = "any".equals(t) || "epistle".equals(t);
        boolean needGospel = "any".equals(t) || "gospel".equals(t);

        List<ScriptureReading> result = new ArrayList<>();

        boolean resolvedEpistleByKey = false;
        boolean resolvedGospelByKey = false;

        if (needEpistle && hasText(context.getEpistleKey())) {
            List<EpistleReading> epistles = epistleRepo.findByReadingKeyAndLang(context.getEpistleKey(), lang);
            if (!epistles.isEmpty()) {
                resolvedEpistleByKey = true;
                for (EpistleReading e : epistles) {
                    addIfAbsent(result, toDto(e, context.getDayKey(), context.getSlot(), context.getSourceType()));
                }
            }
        }

        if (needGospel && hasText(context.getGospelKey())) {
            List<GospelReading> gospels = gospelRepo.findByReadingKeyAndLang(context.getGospelKey(), lang);
            if (!gospels.isEmpty()) {
                resolvedGospelByKey = true;
                for (GospelReading g : gospels) {
                    addIfAbsent(result, toDto(g, context.getDayKey(), context.getSlot(), context.getSourceType()));
                }
            }
        }

        if (hasText(context.getDayLabel())) {
            if (needEpistle && !resolvedEpistleByKey) {
                for (ScriptureReading legacy : getReadingsByLiturgicalName(context.getDayLabel(), "epistle", lang)) {
                    legacy.setDayKey(context.getDayKey());
                    legacy.setSlot(context.getSlot());
                    legacy.setSourceType(context.getSourceType());
                    addIfAbsent(result, legacy);
                }
            }
            if (needGospel && !resolvedGospelByKey) {
                for (ScriptureReading legacy : getReadingsByLiturgicalName(context.getDayLabel(), "gospel", lang)) {
                    legacy.setDayKey(context.getDayKey());
                    legacy.setSlot(context.getSlot());
                    legacy.setSourceType(context.getSourceType());
                    addIfAbsent(result, legacy);
                }
            }
        }

        return result;
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isBlank()) {
            return "ar";
        }

        String cleaned = lang.trim().toLowerCase().replaceAll("[^a-z_]", "");
        if (cleaned.isBlank()) {
            return "ar";
        }

        return cleaned;
    }

    private String normalizeType(String type) {
        return (type == null || type.isBlank()) ? "any" : type.trim().toLowerCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private void addIfAbsent(List<ScriptureReading> result, ScriptureReading candidate) {
        String candidateKey = firstNonBlank(candidate.getSourceId(), candidate.getReadingKey(), candidate.getReference());
        for (ScriptureReading existing : result) {
            String existingKey = firstNonBlank(existing.getSourceId(), existing.getReadingKey(), existing.getReference());
            if (existing.getType() != null && existing.getType().equalsIgnoreCase(candidate.getType())
                    && existingKey != null && existingKey.equals(candidateKey)) {
                return;
            }
        }
        result.add(candidate);
    }

    private ScriptureReading toDto(EpistleReading e, String dayKey, String slot, String sourceType) {
        return ScriptureReading.builder()
                .title(e.getTitle())
                .reference(e.getReference())
                .type("epistle")
                .sourceId(e.getId())
                .readingKey(e.getReadingKey())
                .dayKey(dayKey)
                .slot(slot)
                .sourceType(sourceType)
                .liturgicalName(e.getLiturgicalName())
                .lang(e.getLang())
                .desc(e.getDesc())
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
                .build();
    }

    private ScriptureReading toDto(GospelReading g, String dayKey, String slot, String sourceType) {
        return ScriptureReading.builder()
                .title(g.getTitle())
                .reference(g.getReference())
                .type("gospel")
                .sourceId(g.getId())
                .readingKey(g.getReadingKey())
                .dayKey(dayKey)
                .slot(slot)
                .sourceType(sourceType)
                .liturgicalName(g.getLiturgicalName())
                .lang(g.getLang())
                .desc(g.getDesc())
                .readingTitle(g.getReadingTitle())
                .readingContent(g.getReadingContent())
                .prokeimenonTitle(g.getProkeimenonTitle())
                .prokeimenonTone(g.getProkeimenonTone())
                .prokeimenonVerse(g.getProkeimenonVerse())
                .alleluiaTitle(g.getAlleluiaTitle())
                .alleluiaTone(g.getAlleluiaTone())
                .alleluiaVerse(g.getAlleluiaVerse())
                .alleluiaStikheron(g.getAlleluiaStikheron())
                .build();
    }

    @Override
    public List<ScriptureReading> getReadingsByLegacyName(String liturgicalName, String type, String lang) {
        return getReadingsByLiturgicalName(liturgicalName, type, lang);
    }
}