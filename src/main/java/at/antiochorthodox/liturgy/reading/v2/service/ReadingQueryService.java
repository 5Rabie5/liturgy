package at.antiochorthodox.liturgy.reading.v2.service;

import at.antiochorthodox.liturgy.dto.ServiceReadingsDto;

import java.time.LocalDate;
import java.util.List;

/**
 * Canonical application entry point for the reading-assignment flow.
 *
 * <p>This is the preferred service to inject for v2 reading queries.
 * Older names such as {@code V2ReadingQueryService} remain only as
 * compatibility aliases while downstream code is migrated.</p>
 */
public interface ReadingQueryService {

    List<ServiceReadingsDto> getByDate(LocalDate date, String lang, String tradition);

    List<ServiceReadingsDto> getByDateAndSlot(LocalDate date, String slot, String lang, String tradition);

    List<ServiceReadingsDto> getByDateAndService(LocalDate date, String serviceKey, String lang, String tradition);
}
