Sunday summary pass 2

Added a final legacy fallback from readings.liturgicalDay after movableFeast and fixedFeast.

This helps Sunday summaries where:
- v2 has no summary candidate
- movable feast is empty
- fixed feast is empty
- but liturgicalDay.readings still contains a gospel and/or epistle

Typical target cases:
- TRIODION_W00_SUNDAY
- TRIODION_W01_SUNDAY

Fallback order is now:
1. v2 service summary
2. movableFeast
3. fixedFeast
4. liturgicalDay
