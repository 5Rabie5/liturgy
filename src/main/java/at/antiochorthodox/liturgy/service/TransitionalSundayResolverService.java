package at.antiochorthodox.liturgy.service;

import at.antiochorthodox.liturgy.dto.TransitionalSundayResolution;
import at.antiochorthodox.liturgy.model.LiturgicalCalendarDay;

import java.time.LocalDate;

public interface TransitionalSundayResolverService {

    boolean isInWindow(LocalDate date);

    TransitionalSundayResolution resolve(LocalDate date, String lang, LiturgicalCalendarDay baseDay);
}
