# Architecture Transition Guide

## Canonical reading flow for new code

Use this path for all new development:

1. `reading.v2.controller.V2ReadingController`
2. `reading.v2.service.ReadingQueryService`
3. `reading.v2.service.ReadingContextService`
4. `reading.v2.service.LiturgicalReadingAssignmentQueryService`
5. `reading.v2.service.ServiceReadingsAssembler`

This is the clean assignment-based flow.

## Compatibility-only surfaces

These classes remain intentionally, but they are no longer the preferred core:

- `controller/ScriptureReadingController.java`
- `service/ScriptureReadingService.java`
- `service/ScriptureReadingServiceImpl.java`
- `service/LiturgicalDayContextService.java`
- `service/LiturgicalDayReadingsService.java`
- `service/LiturgicalReadingAssignmentService.java`
- `service/LiturgicalReadingAssignmentServiceImpl.java`
- `reading/v2/service/V2ReadingQueryService.java`
- `reading/v2/service/V2ReadingQueryServiceImpl.java`

## Practical migration rule

- Need grouped legacy calendar readings? keep using the legacy services.
- Need exact service-based liturgical assignments? use the v2 flow only.
- Need a new feature? start in `ReadingQueryService`, not in the compatibility layer.

## What was intentionally avoided

The following categories were **not** pulled into the core from the newer codebase:

- Sunday audit utilities
- backfill tools
- canonicalization repair tools
- residual recovery tools
- large admin-only recovery services

Those belong in admin tooling or a separate module, not inside the core runtime path.
