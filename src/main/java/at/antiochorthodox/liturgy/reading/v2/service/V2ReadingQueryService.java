package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.reading.v2.dto.ServiceReadingsDto;

import java.time.LocalDate;
import java.util.List;

public interface V2ReadingQueryService {

    List<ServiceReadingsDto> getByDate(LocalDate date, String lang, String tradition);

    List<ServiceReadingsDto> getByDateAndSlot(LocalDate date, String slot, String lang, String tradition);

    List<ServiceReadingsDto> getByDateAndService(LocalDate date, String serviceKey, String lang, String tradition);
}
