# Monthly Wins Tracking Implementation

## Overview
Implemented automatic and manual tracking of monthly wins for frogs based on "This Month" points.

## Changes Made

### 1. Database Schema Updates

**AppSettings.kt** - Added field to track processing:
```kotlin
val lastMonthlyWinsProcessed: String? = null  // Last month we processed wins (format: "YYYY-MM")
```

**FrogDatabase.kt** - Migration 5 → 6:
- Database version updated from 5 to 6
- Added `MIGRATION_5_6` to add `lastMonthlyWinsProcessed` column to `app_settings` table

### 2. FrogViewModel Logic

**Automatic Processing** (`checkAndProcessMonthlyWins()`):
- Called on app startup in `init` block
- Checks if current month has already been processed
- Runs BEFORE `updateCurrentMonthPoints()` to capture previous month's data
- Only processes once per month

**Core Processing** (`processMonthlyWins()`):
- Finds frog with highest `currentMonthPoints`
- Awards monthly win if frog has > 0 points
- Updates frog's `monthlyWins` counter
- Updates frog's `lastMonthWinRecorded` to prevent duplicates
- Updates `AppSettings.lastMonthlyWinsProcessed` to current month

**Manual Trigger** (`manuallyProcessMonthlyWins()`):
- Public function for manual processing
- Called from Settings screen button
- Launches coroutine to run `processMonthlyWins()`

### 3. Settings Screen UI

**New Button:** "Process Monthly Wins"
- Located in Maintenance section
- Secondary color (blue)
- Shows toast: "Processing monthly wins..."
- Helper text: "Awards monthly win to frog with most points this month"

## How It Works

### Automatic Flow (Monthly)

1. **App Startup** - Each time app launches:
   ```
   init block
   ├─ Initialize/load settings
   ├─ Wait 500ms for data to load
   ├─ checkAndProcessMonthlyWins()
   │  ├─ Get current month (YYYY-MM)
   │  ├─ Check if lastMonthlyWinsProcessed != currentMonth
   │  └─ If new month: processMonthlyWins()
   └─ updateCurrentMonthPoints()
   ```

2. **Process Monthly Wins**:
   ```
   processMonthlyWins()
   ├─ Find frog with max currentMonthPoints
   ├─ If points > 0:
   │  ├─ Increment frog.monthlyWins
   │  ├─ Set frog.lastMonthWinRecorded = currentMonth
   │  └─ Set settings.lastMonthlyWinsProcessed = currentMonth
   └─ Update database
   ```

### Manual Trigger

User can manually award monthly wins by:
1. Navigate to Settings
2. Scroll to Maintenance section
3. Click "Process Monthly Wins" button
4. System awards win to current leader

### Display

**Pet Frog Icon 🐸** appears in View Frog list for:
- Frog with most monthly wins (`maxByOrNull { it.monthlyWins }`)
- Only shows if `monthlyWins > 0`

**Location**: Next to frog icon overlay (alongside 👑 crown and 😇 halo)

## Edge Cases Handled

1. **No Frogs** - Returns early if frog list is empty
2. **No Points** - Only awards win if `currentMonthPoints > 0`
3. **Duplicate Prevention**:
   - Frog level: `lastMonthWinRecorded` prevents same frog winning same month twice
   - App level: `lastMonthlyWinsProcessed` prevents processing same month multiple times
4. **Month Transition** - Automatic check on every app startup ensures wins are recorded even if app not opened for multiple months

## Testing Checklist

- [ ] Award monthly win manually via Settings button
- [ ] Verify frog's monthlyWins counter increments
- [ ] Verify pet frog 🐸 emoji appears on winner
- [ ] Verify only frog with most monthly wins gets pet frog
- [ ] Test multiple frogs - winner changes based on monthlyWins count
- [ ] Test app restart - verify automatic processing
- [ ] Test month transition - verify new month triggers processing
- [ ] Test duplicate prevention - press button multiple times, counter only increments once per month
- [ ] Test with 0 points - verify no win awarded

## Database Migration Notes

**Migration Path**: v5 → v6
- Safe migration: Only adds nullable column with DEFAULT NULL
- No data loss
- Backward compatible (old data works with new schema)
- Column will be NULL for existing records until first monthly wins processing

## Future Enhancements

Potential improvements:
1. Add notification when monthly win is awarded
2. Show monthly win history (which months each frog won)
3. Display month-by-month leaderboard
4. Reset currentMonthPoints at month start (currently just recalculated)
5. Add WorkManager for guaranteed monthly processing even if app not opened
