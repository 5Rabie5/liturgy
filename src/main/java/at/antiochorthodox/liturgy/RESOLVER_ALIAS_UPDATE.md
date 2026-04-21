Resolver alias update

What changed:
- Added canonical + lookup-key support to ReadingDayKeyResolution
- Added resolveLookupDayKeys(...) to both resolver interfaces
- Updated both resolver implementations to support canonical aliases for Great Lent Sundays:
  - GREAT_LENT_W01_SUNDAY -> TRIODION_W04_SUNDAY
  - GREAT_LENT_W02_SUNDAY -> TRIODION_W05_SUNDAY
  - GREAT_LENT_W03_SUNDAY -> TRIODION_W06_SUNDAY
  - GREAT_LENT_W04_SUNDAY -> TRIODION_W07_SUNDAY
  - GREAT_LENT_W05_SUNDAY -> TRIODION_W08_SUNDAY
- Updated ReadingContextServiceImpl and LiturgicalDayContextService to expose canonical outward day keys while still allowing legacy lookup fallback.
- Updated LiturgicalReadingAssignmentQueryServiceImpl to query by resolver-provided lookup keys instead of a single day key.

Main goal:
- Keep the new API outwardly canonical (TRIODION_*),
- while still reading legacy assignments stored under GREAT_LENT_* day keys.
