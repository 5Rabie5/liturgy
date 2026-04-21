# Sunday summary update

This update changes `LiturgicalCalendarDayBuilderService` so that Sunday calendar responses
prefer the canonical v2 assignment flow when filling the top-level summary fields:

- `readingSlot`
- `readingSourceType`
- `epistleKey`
- `gospelKey`

Priority order for Sunday summary selection:

1. `liturgy`
2. `default`
3. `matins`
4. any other slot

If no v2 summary is available, the builder falls back to grouped legacy readings in this order:

1. movable feast group
2. fixed feast group

This is intended to fix Sunday cases like Palm Sunday where the old calendar summary could point to
`default` instead of the full liturgy assignment.
