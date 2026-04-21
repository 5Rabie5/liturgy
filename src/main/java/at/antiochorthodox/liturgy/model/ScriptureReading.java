package at.antiochorthodox.liturgy.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptureReading {
    private String sourceId;

    private String liturgicalName;    // legacy display / fallback lookup name
    private String dayKey;            // unique day identity for the liturgical day
    private String slot;              // default / liturgy / matins / vespers / wedding / funeral / etc.
    private String sourceType;        // daily / fixed_feast / saint / resurrection / hour / service
    private String readingKey;        // stable reading identity for epistle/gospel cycles

    private String title;
    private String reference;
    private String type;              // epistle / gospel
    private String lang;
    private String desc;

    // ---------- Common ----------
    private String readingTitle;
    private String readingContent;
    private String alleluiaTitle;
    private String alleluiaTone;
    private String alleluiaVerse;
    private String alleluiaStikheron;

    // ---------- Epistle ----------
    private String prokeimenon1Title;
    private String prokeimenon1Tone;
    private String prokeimenon1Verse;
    private String prokeimenon1Stikheron;

    private String prokeimenon2Title;
    private String prokeimenon2Tone;
    private String prokeimenon2Verse;
    private String prokeimenon2Stikheron;

    // ---------- Gospel ----------
    private String prokeimenonTitle;
    private String prokeimenonTone;
    private String prokeimenonVerse;
}
