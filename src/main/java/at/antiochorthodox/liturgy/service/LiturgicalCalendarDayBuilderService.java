package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;
import at.antiochorthodox.liturgy.model.LiturgicalScriptureReadingDay;
import at.antiochorthodox.liturgy.model.ScriptureReadingOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import at.antiochorthodox.liturgy.model.ScriptureReading;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class LiturgicalCalendarDayBuilderService {

    private final LiturgicalScriptureReadingDayService liturgicalScriptureReadingDayService;
    private final SaintService saintService;
    private final FeastService feastService;
    private final FastingService fastingService;

    @Autowired
    public LiturgicalCalendarDayBuilderService(

            LiturgicalScriptureReadingDayService liturgicalScriptureReadingDayService,
            SaintService saintService,
            FeastService feastService,
            FastingService fastingService
    ) {
        this.liturgicalScriptureReadingDayService = liturgicalScriptureReadingDayService;
        this.saintService = saintService;
        this.feastService = feastService;
        this.fastingService = fastingService;
    }

    public LiturgicalCalendarDay buildLiturgicalDay(LocalDate date, String lang) {
        // 1. جلب القراءات لليوم (تأخذ أول خيار مفضل أو أول خيار افتراضي)
        Optional<LiturgicalScriptureReadingDay> readingsOpt = liturgicalScriptureReadingDayService.findByDate(date);

        String gospelReading = null;
        String epistleReading = null;
        List<String> alternativeReadings = Collections.emptyList();

        if (readingsOpt.isPresent() && readingsOpt.get().getOptions() != null && !readingsOpt.get().getOptions().isEmpty()) {
            List<ScriptureReadingOption> options = readingsOpt.get().getOptions();
            // ابحث عن preferred أو أول خيار متاح
            ScriptureReadingOption option = options.stream()
                    .filter(opt -> Boolean.TRUE.equals(opt.getPreferred()))
                    .findFirst()
                    .orElse(options.get(0));

            // الإنجيل
            if (option.getGospel() != null && lang.equals(option.getGospel().getLang())) {
                gospelReading = option.getGospel().getReference();
            } else if (option.getGospel() != null) {
                // إذا لم توجد نسخة بلغة lang، أعطِ أي نسخة متوفرة
                gospelReading = option.getGospel().getReference();
            }

            // الرسالة
            if (option.getEpistle() != null && lang.equals(option.getEpistle().getLang())) {
                epistleReading = option.getEpistle().getReference();
            } else if (option.getEpistle() != null) {
                epistleReading = option.getEpistle().getReference();
            }

            // القراءات البديلة (كلها أو فقط التي باللغة المطلوبة)
            alternativeReadings = option.getAlternativeReadings() != null ?
                    option.getAlternativeReadings().stream()
                            .filter(reading -> lang.equals(reading.getLang()))
                            .map(ScriptureReading::getReference)
                            .toList()
                    : Collections.emptyList();

            // إذا لم يوجد بدائل بنفس اللغة أعد كل البدائل بغض النظر عن اللغة (احتياطي)
            if (alternativeReadings.isEmpty() && option.getAlternativeReadings() != null) {
                alternativeReadings = option.getAlternativeReadings().stream()
                        .map(ScriptureReading::getReference)
                        .toList();
            }
        }

        // 2. القديسون (الآن مع lang)
        List<String> saints = saintService.findNamesByLangAndDate(lang, date);

        // 3. الأعياد (الآن مع lang)
        String fixedFeast = feastService.findFixedFeastNameByLangAndDate(lang, date);
        String movableFeast = feastService.findMovableFeastNameByLangAndDate(lang, date);

        // 4. نوع الصوم أو رمزه (الآن مع lang)
        String fastingType = fastingService.getFastingTypeByLangAndDate(lang, date);
        String fastingLevel = fastingService.getFastingEvelByLangAndDate(lang, date);
//        System.out.println("Building for date: " + date + ", lang: " + lang);


//        System.out.println("Saints: " + saints);


//        System.out.println("Fixed Feast: " + fixedFeast);
        // 5. بناء اليوم الليتورجي
        return LiturgicalCalendarDay.builder()
                .date(date)
                .saints(saints)
                .gospelReading(gospelReading)
                .epistleReading(epistleReading)
                .alternativeReadings(alternativeReadings)
                .fixedFeast(fixedFeast)
                .movableFeast(movableFeast)
                .fastingLevel(fastingLevel)
                .build();


    }

}
