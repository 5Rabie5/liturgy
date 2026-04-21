# Runtime collections only

This pass removes operational dependence on `liturgical_day_reading_maps` from the reading flow.

## Runtime collections
- `liturgical_reading_assignments`
- `gospel_readings`
- `epistle_readings`
- `liturgical_labels`

## Legacy/reference only
- `liturgical_day_reading_maps`

## What changed
- `LiturgicalDayContextService` now builds its simplified legacy context from assignments.
- `ReadingDayKeyResolverImpl` now resolves against assignments and `calendarDayKey` fallback.
- Old map model/repository remain only for backward compatibility and historical reference.
