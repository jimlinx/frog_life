# Rebuild and Install Frog Life

## Changes Applied

### 1. File Picker for Export/Import
- **Export**: Tap "Export Data to JSON" → Choose where to save the file
- **Import**: Tap "Import Data from JSON" → Select a JSON file to import

### 2. Complete Calendar Views

Now displays proper calendar grids:

#### Year View
- Grid of all 12 months
- Shows activity count and total points per month
- Tap any month to drill down to month view

#### Month View
- Traditional calendar grid with days of the week
- Colored dots indicate activities on each day
- Points total shown for each day
- Tap any day to see details

#### Week View
- Shows 7 days of the current week
- Lists all activities for each day
- Shows points earned per activity
- Tap any day to drill down

#### Day View
- Detailed view of single day
- All activities with full descriptions
- Time logged for each activity
- Total points summary at top

Navigation:
- Use ◀ ▶ arrows to navigate between periods
- Tap dates to drill down to more detailed views

## Build Instructions

### In Android Studio:

1. Open the project in Android Studio

2. **File → Sync Project with Gradle Files**
   - Wait for sync to complete (watch bottom status bar)

3. **Build → Rebuild Project**
   - Check "Build" tab for "BUILD SUCCESSFUL"

4. **Run → Run 'app'** (or click green ▶️ button)
   - Select your device
   - App will install and launch

## OR: Manual Install via Terminal

Once Android Studio completes one successful sync:

```bash
cd /Users/jimlin/Repos/frog_life

# Build
./gradlew assembleDebug

# Install
~/Library/Android/sdk/platform-tools/adb uninstall com.froglife
~/Library/Android/sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
~/Library/Android/sdk/platform-tools/adb shell am start -n com.froglife/.MainActivity
```

## Test the New Features

1. **Calendar Views**:
   - Go to Calendar
   - Select a frog
   - Try switching between Day/Week/Month/Year views
   - Navigate using arrow buttons
   - Tap dates to drill down

2. **Export Data**:
   - Go to Settings
   - Tap "Export Data to JSON"
   - Choose a location (e.g., Downloads folder)
   - File will be saved

3. **Import Data**:
   - Go to Settings
   - Tap "Import Data from JSON"
   - Select a previously exported file
   - Data will be imported

## Notes

- Export includes: frogs, activities, settings (activity logs coming soon)
- Calendar colors match the activity colors
- Points shown in green (positive) or red (negative)
- Leader frog shown with 👑 crown icon
- Selected frog highlighted in calendar
