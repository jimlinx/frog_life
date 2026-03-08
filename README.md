# 🐸 Frog Life - Anime-Style Activity Tracker

A Japanese anime-themed Android app for tracking activities and managing frogs (users) with a gamified wealth points system.

## Features

### 1. **Settings**
- Configure wealth point thresholds for each frog status:
  - 🪨 Rock (default: 10 points)
  - 🟤 Copper (default: 50 points)
  - 🥉 Bronze (default: 100 points)
  - 🥈 Silver (default: 200 points)
  - 🥇 Gold (default: 400 points)
  - 💎 Diamond (default: 800 points)
- Export/Import data to JSON format

### 2. **Manage Frogs**
- Add, edit, and delete frogs (users)
- Each frog has:
  - Name
  - Wealth points (starting at 10)
  - Status with corresponding icon (based on wealth points)
  - Profile picture (choose from 5 preset anime frog icons or upload custom)
  - Description
  - Attached activities

### 3. **Manage Activities**
- Define activities with:
  - Name
  - Optional description
  - Color (simple color picker)
  - Type: Boolean (✓/✗) or Integer (number)
  - Default value
  - Wealth amount (positive or negative points)

### 4. **Calendar**
- View frog profiles at the top with colored status rings
- Leader frog wears a crown 👑
- Switch between Day/Week/Month/Year views
- Track activities with different colors
- Display check/cross or numbers based on activity type
- Click on dates to edit activity values
- Automatic wealth point calculation

### 5. **View Frog**
- Display frog profile with status ring
- Show name, description, wealth points, and status
- Admin override section with biometric authentication
- Add bonus or apply punishment to wealth points

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Database**: Room
- **Architecture**: MVVM with ViewModels and StateFlow
- **Navigation**: Jetpack Navigation Compose
- **Authentication**: Biometric API
- **Serialization**: Gson for JSON export/import
- **Image Loading**: Coil
- **Privacy**: Photo Picker (no storage permissions needed!) 🔒

## Privacy & Permissions 🔒

**No Storage Permissions Required!**

This app respects your privacy by using the Android Photo Picker and Storage Access Framework for custom profile pictures. This means:
- ✅ No permission dialogs for selecting images
- ✅ You explicitly choose which images the app can access
- ✅ Google Play compliant and privacy-preserving
- ✅ Only biometric permission required (for admin features)

See `NO_PERMISSIONS_NEEDED.md` for details on how this works.

## Build Requirements

- Android Studio Hedgehog (2023.1.1) or newer
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 34 (Android 14)
- Kotlin 1.9.22+
- Gradle 9.0.0+

## Getting Started

### 1. Clone the Repository
```bash
cd frog_life
```

### 2. Open in Android Studio
- Open Android Studio
- Click "Open an Existing Project"
- Navigate to the `frog_life` folder
- Wait for Gradle sync to complete

### 3. Build and Run
- Connect an Android device or start an emulator
- Click the "Run" button (▶️) or press Shift+F10
- Select your device and wait for the app to install

## Project Structure

```
frog_life/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/froglife/
│   │       │   ├── data/           # Data models, DAOs, Database
│   │       │   ├── ui/             # UI screens and components
│   │       │   │   ├── screens/    # Individual screen composables
│   │       │   │   └── theme/      # App theme and colors
│   │       │   ├── viewmodel/      # ViewModels
│   │       │   ├── utils/          # Utility classes
│   │       │   └── MainActivity.kt
│   │       ├── res/                # Resources
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Key Features Implementation

### Wealth Points System
- Activities automatically add/subtract wealth points
- Boolean activities: points added if checked (true)
- Integer activities: value × wealth amount
- Status automatically updates based on configured thresholds

### Biometric Authentication
- Required for admin overrides (bonus/punishment)
- Uses device fingerprint or face recognition
- Secure and quick authentication

### Japanese Anime Theme
- Cute frog icons 🐸🐊🦎🐢🦖
- Colorful, vibrant UI with Material 3
- Status-based colored rings around profiles
- Crown for leader frog

## Database Schema

### Entities
1. **Frog**: id, name, wealthPoints, status, profilePicturePath, description, presetIconId
2. **Activity**: id, name, description, color, type, defaultValue, wealthAmount
3. **FrogActivityCrossRef**: frogId, activityId (many-to-many relationship)
4. **ActivityLog**: id, frogId, activityId, date, value, pointsEarned
5. **AppSettings**: id, rockThreshold, copperThreshold, bronzeThreshold, silverThreshold, goldThreshold, diamondThreshold

## Future Enhancements

- [ ] Custom profile pictures via Photo Picker (no permissions needed!)
- [ ] Enhanced calendar UI with grid view
- [ ] Activity statistics and charts
- [ ] Push notifications for daily activity reminders
- [ ] Dark mode toggle
- [ ] Customizable anime fonts
- [ ] Achievement badges
- [ ] Social features (compare frogs with friends)
- [ ] Export activity history as PDF report

## License

This project is created for personal use. Feel free to modify and distribute as needed.

## Support

For issues or questions, please create an issue in the repository.

---

Made with 💚 and 🐸
