# 🐸 Frog Life - Quick Start Guide

## What You Have

A complete Japanese anime-themed Android app for tracking activities and managing frogs with a gamified points system!

## Files Created: 46+ files
- 32 Kotlin source files
- 7 XML resource files
- 4 Build configuration files
- 3 Documentation files

## 3-Step Quick Start

### Step 1: Open in Android Studio
```bash
cd /Users/jimlin/Repos/frog_life
# Then: Open this folder in Android Studio
```

### Step 2: Wait for Gradle Sync
- Android Studio will automatically sync Gradle
- This downloads dependencies (~5 minutes first time)
- Watch the bottom status bar for progress

### Step 3: Run the App
- Click the green Run button (▶️)
- Or press: `Shift + F10` (Windows/Linux) or `Control + R` (Mac)
- Select your device/emulator
- App launches automatically!

## First Time Setup (If Needed)

### If you see "SDK not found":
Create `local.properties` in project root:
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

### If Gradle sync fails:
```bash
cd /Users/jimlin/Repos/frog_life
./gradlew clean
./gradlew build
```

## Using the App

### 1. Create Your First Frog
1. Main Menu → "Manage Frogs"
2. Click the + button
3. Enter name (e.g., "Froggy")
4. Choose an icon (5 anime frogs available)
5. Add description
6. Save

### 2. Create Activities
1. Main Menu → "Manage Activities"
2. Click the + button
3. Enter activity name (e.g., "Exercise")
4. Choose a color
5. Select type: Boolean (✓/✗) or Number
6. Set wealth points (e.g., +10 for good habits, -5 for bad)
7. Save

### 3. Attach Activities to Frog
1. Edit your frog
2. Scroll to "Attached Activities"
3. Select activities to track
4. Save

### 4. Track Daily Activities
1. Main Menu → "Calendar"
2. Select your frog from the top
3. Choose view (Day/Week/Month/Year)
4. Click on a date
5. Mark activities as complete
6. Watch wealth points grow!

### 5. View Frog Progress
1. Main Menu → "View Frog"
2. See total wealth points
3. See current status (Rock → Copper → Bronze → Silver → Gold → Diamond)
4. Use admin controls (requires biometric auth) to adjust points

## App Features at a Glance

| Screen | What It Does |
|--------|-------------|
| **Main** | Navigation hub |
| **Settings** | Configure status thresholds, export/import data |
| **Manage Frogs** | Add/edit/delete frogs |
| **Manage Activities** | Define activities with colors and point values |
| **Calendar** | Track daily activities, view logs |
| **View Frog** | See frog details, admin override |

## Status Levels (Default Thresholds)

| Status | Icon | Points Required |
|--------|------|-----------------|
| Rock | 🪨 | 10 (starting) |
| Copper | 🟤 | 50 |
| Bronze | 🥉 | 100 |
| Silver | 🥈 | 200 |
| Gold | 🥇 | 400 |
| Diamond | 💎 | 800 |

## Troubleshooting

### App won't build?
1. File → Invalidate Caches → Restart
2. Build → Clean Project
3. Build → Rebuild Project

### Biometric auth not working?
- Physical device: Enable fingerprint in Settings
- Emulator: Use Extended Controls → Fingerprint

### Database errors?
1. Uninstall app from device
2. Rebuild and reinstall

## Project Structure

```
frog_life/
├── app/
│   ├── src/main/
│   │   ├── java/com/froglife/
│   │   │   ├── data/          # Database, models, DAOs
│   │   │   ├── ui/            # Screens, theme
│   │   │   ├── viewmodel/     # State management
│   │   │   └── utils/         # Helper functions
│   │   └── res/               # UI resources
│   └── build.gradle.kts
├── README.md                   # Full documentation
├── SETUP_GUIDE.md             # Detailed setup
├── PROJECT_SUMMARY.md         # Complete feature list
└── QUICKSTART.md              # This file!
```

## Key Technologies

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Database**: Room (SQLite)
- **Architecture**: MVVM
- **Auth**: Biometric API
- **Privacy**: Photo Picker (no storage permissions!) 🔒

## Next Steps

1. ✅ Build and run the app
2. ✅ Create sample frogs and activities
3. ✅ Track some activities
4. ✅ Watch frogs level up!
5. 📚 Read `README.md` for detailed features
6. 🔧 Read `SETUP_GUIDE.md` for advanced setup
7. 📊 Read `PROJECT_SUMMARY.md` for technical details

## Need Help?

1. Check `SETUP_GUIDE.md` for detailed instructions
2. Review build errors in Android Studio's Build tab
3. Check Logcat for runtime errors
4. All documentation is in the project root

## Sample Data Ideas

### Frogs
- Personal: Your name
- Family: Family members
- Friends: Friend group
- Teams: Work teams

### Activities
**Good Habits** (positive points):
- Exercise (+10)
- Study (+15)
- Meditation (+5)
- Reading (+8)

**Bad Habits** (negative points):
- Junk Food (-5)
- Late Sleep (-10)
- Skip Workout (-8)

**Tracked Activities** (neutral/informational):
- Water Intake (number × 1)
- Steps (number × 0.01)
- Hours Studied (number × 5)

## Fun Tips

- 👑 The frog with most points wears a crown!
- 🎨 Choose bright colors for activities to match the anime theme
- 💎 Try to reach Diamond status (800 points)!
- 📊 Export your data regularly as backup
- 🎯 Set daily goals using activities

---

**That's it! You're ready to go! 🐸✨**

Open the project in Android Studio and start building!

Happy Frog Life! 🎮🌸
