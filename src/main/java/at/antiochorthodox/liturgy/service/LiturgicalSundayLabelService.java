package at.antiochorthodox.liturgy.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Service
public class LiturgicalSundayLabelService {

    private Map<String, Map<String, String>> labelMap = Collections.emptyMap();

    @PostConstruct
    public void loadLabels() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getClassLoader().getResourceAsStream("liturgical_sunday_labels.json");
            if (is == null) {
                throw new IllegalStateException("لم يتم العثور على ملف التسميات: liturgical_sunday_labels.json");
            }
            labelMap = mapper.readValue(is, new TypeReference<Map<String, Map<String, String>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("تعذر تحميل أو قراءة ملف التسميات liturgical_sunday_labels.json", e);
        }
    }

    /**
     * جلب التسمية بناءً على المفتاح واللغة المطلوبة.
     * إذا لم توجد اللغة المطلوبة، سيعود إلى الإنكليزية، ثم إلى المفتاح نفسه.
     *
     * @param key  (مثل: THOMAS_SUNDAY)
     * @param lang (ar/en/de/nl/fr ...)
     * @return التسمية المناسبة
     */
    public String getLabel(String key, String lang) {
        Map<String, String> translations = labelMap.get(key);
        if (translations == null) return key;
        // ابحث عن اللغة المطلوبة، أو fallback للإنكليزية أو المفتاح
        return translations.getOrDefault(lang, translations.getOrDefault("en", key));
    }
}
