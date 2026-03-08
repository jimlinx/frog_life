# New Features Added

## 1. View Frog - Frog Selection Screen

**What changed:**
- View Frog page now shows a **list of all frogs** first
- Users can select which frog to view details for
- Leader frog is shown with 👑 crown icon

**How it works:**
1. Tap "View Frog" from main menu
2. See list of all frogs with:
   - Profile picture with colored status ring
   - Name and status
   - Wealth points
   - Crown icon for leader
3. Tap any frog to view their details
4. Tap back arrow to return to frog selection

## 2. Calendar Day View - Activity Tracking & Management

**What changed:**
- Calendar day view now allows **tracking (adding) new activities**
- Can **edit existing activity logs**
- Can **delete activity logs**
- Points automatically update when activities are added/edited/deleted

**How it works:**

### Track New Activity:
1. Go to Calendar → Select frog → Day view
2. Tap **"+ Track Activity"** button
3. Select which activity to track
4. Enter value:
   - **Boolean activities**: Check/uncheck checkbox
   - **Integer activities**: Enter a number
5. See points preview
6. Tap "Save"

### Edit Activity:
1. Find the activity log in day view
2. Tap the **✏️ (edit)** button
3. Change the value
4. Tap "Save"
5. Points automatically recalculated

### Delete Activity:
1. Find the activity log in day view
2. Tap the **🗑️ (delete)** button
3. Confirm deletion
4. Points automatically deducted from frog's wealth

### Points Calculation:
- **Boolean (true)**: Adds/subtracts the activity's wealth amount
- **Boolean (false)**: No points
- **Integer**: Value × wealth amount
- Points shown in green (positive) or red (negative)

## Key Features:

✅ **Automatic wealth updates** - Frog wealth points update immediately when logging activities

✅ **Edit protection** - Confirmation dialog before deleting

✅ **Visual feedback** - Activity cards show points in green/red, color-coded by activity type

✅ **Smart filtering** - Only shows activities attached to the selected frog

✅ **Same-day handling** - Multiple logs per day allowed (one per activity per day)

## Build Instructions

1. **Open Android Studio**
2. **File → Sync Project with Gradle Files**
3. **Build → Rebuild Project**
4. **Run → Run 'app'**

Or via command line (after first Android Studio sync):
```bash
cd /Users/jimlin/Repos/frog_life
./gradlew assembleDebug
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Testing the Features:

### Test View Frog Selection:
1. Make sure you have multiple frogs created
2. Go to "View Frog"
3. You should see a list of all frogs
4. Tap any frog to view details
5. Tap back to return to frog list

### Test Activity Tracking:
1. Create a frog and attach some activities to it
2. Go to Calendar → Select the frog → Day view
3. Tap "+ Track Activity"
4. Select an activity and enter a value
5. Save and verify points were added to frog
6. Try editing the activity (✏️ button)
7. Try deleting the activity (🗑️ button)
8. Verify wealth points update correctly each time

## Technical Changes:

**Files Modified:**
- `ViewFrogScreen.kt` - Added frog selection list and details separation
- `CalendarScreen.kt` - Added activity tracking dialog and management in DayView
- `CalendarViewModel.kt` - Added delete support to `updateActivityLog()`

**New Components:**
- `FrogSelectionScreen` - List of frogs to choose from
- `FrogSelectionCard` - Individual frog card in list
- `FrogDetailsScreen` - Existing frog details view (separated)
- `ActivityLogCard` - Enhanced log card with edit/delete buttons
- `ActivityLogDialog` - Add/edit activity dialog
- `calculatePoints()` - Helper function for point calculation
