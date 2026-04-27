Transitional Sunday Override Feature

Added support for the unstable Antiochian Sunday window between the post-Theophany period and the beginning of the Triodion.

What was added:
- transitional_sunday_overrides Mongo collection model
- repository lookup by exact date + slot
- resolver service that marks this window and applies annual overrides when present
- calendar day response now exposes decisionBasis and sourceReference
- calendar day builder applies the override layer after the normal Sunday summary logic

Default behavior:
- if the date is outside the window: no special behavior
- if the date is inside the window and no override exists: BASE_RULE is reported
- if an override exists: YEAR_OVERRIDE (or the stored decisionBasis) is applied
