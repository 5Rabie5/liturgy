# Architecture update summary

This package was rebuilt starting from the **older codebase** and then extended
only with the **clean v2 reading core** from the newer direction.

## What was added
- `reading/v2/controller/V2ReadingController`
- `reading/v2/dto/ReadingContext`
- `reading/v2/service/AssignmentReadingDayKeyResolver`
- `reading/v2/service/AssignmentReadingDayKeyResolverImpl`
- `reading/v2/service/ReadingContextService`
- `reading/v2/service/ReadingContextServiceImpl`
- `reading/v2/service/LiturgicalReadingAssignmentQueryService`
- `reading/v2/service/LiturgicalReadingAssignmentQueryServiceImpl`
- `reading/v2/service/ServiceReadingsAssembler`
- `reading/v2/service/ServiceReadingsAssemblerImpl`
- `reading/v2/service/ReadingQueryService`
- `reading/v2/service/ReadingQueryServiceImpl`

## What was kept only for compatibility
- legacy `ScriptureReadingController`
- legacy text/day-key services
- `LiturgicalReadingAssignmentService*`
- `V2ReadingQueryService*` as a compatibility alias

## Important design decision
The canonical entry point for new assignment-based code is now:

- `reading.v2.service.ReadingQueryService`

The older name `V2ReadingQueryService` remains only as a thin adapter so older
wiring does not break.

## Intentional exclusions
Admin/audit/backfill/canonicalization recovery tooling from the newer branch was
**not** merged into the runtime core. The goal is to keep the project suitable
for future integration into a larger codebase without carrying unnecessary
operational complexity.
