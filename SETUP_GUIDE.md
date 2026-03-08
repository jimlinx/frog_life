# Frog Life - Setup Guide

## Prerequisites Checklist

Before building the app, ensure you have:

- [ ] Android Studio Hedgehog (2023.1.1) or newer installed
- [ ] JDK 17 or newer
- [ ] Android SDK installed (API level 34)
- [ ] An Android device or emulator for testing

## Step-by-Step Setup

### 1. Install Android Studio
Download and install from: https://developer.android.com/studio

### 2. Configure SDK
1. Open Android Studio
2. Go to `Tools` → `SDK Manager`
3. Install:
   - Android SDK Platform 34
   - Android SDK Build-Tools 34.0.0
   - Android Emulator

### 3. Create Launcher Icons

The project is missing launcher icons. You can create them using Android Studio's Asset Studio:

1. In Android Studio, right-click on `res` folder
2. Select `New` → `Image Asset`
3. Choose "Launcher Icons (Adaptive and Legacy)"
4. Upload a frog-themed icon image or use clipart
5. Click `Next` then `Finish`

Alternatively, use the default Android icons temporarily:
- The app will use system default icons until you add custom ones
- Icon files should be placed in:
  - `app/src/main/res/mipmap-hdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-mdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
  - `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`

### 4. Gradle Sync

After opening the project in Android Studio:
1. Wait for initial Gradle sync to complete (this may take several minutes)
2. If sync fails, click `Try Again` or use `File` → `Sync Project with Gradle Files`
3. Check for any error messages in the Build window

### 5. Resolve Dependencies

If you encounter dependency issues:
```bash
./gradlew clean
./gradlew build
```

Or in Android Studio: `Build` → `Clean Project` then `Build` → `Rebuild Project`

### 6. Setup Emulator (Optional)

If you don't have a physical device:
1. Go to `Tools` → `Device Manager`
2. Click `Create Device`
3. Select a phone (e.g., Pixel 6)
4. Choose a system image (API 34 recommended)
5. Click `Finish`
6. Start the emulator

### 7. Enable Biometric Authentication (For Testing)

**On Physical Device:**
- Ensure fingerprint or face unlock is set up in device settings

**On Emulator:**
1. Extended Controls → Fingerprint
2. Click "Touch the sensor" to simulate fingerprint

### 8. Run the App

1. Click the green Run button (▶️) or press `Shift + F10`
2. Select your device/emulator
3. Wait for the app to build and install
4. The app should launch automatically

## Common Issues and Solutions

### Issue: "SDK location not found"
**Solution**: Create a `local.properties` file in the project root:
```properties
sdk.dir=/path/to/your/Android/sdk
```

For macOS: Usually `~/Library/Android/sdk`
For Windows: Usually `C:\\Users\\YourName\\AppData\\Local\\Android\\sdk`
For Linux: Usually `~/Android/Sdk`

### Issue: Gradle sync fails
**Solution**:
1. Check internet connection
2. Try `File` → `Invalidate Caches` → `Invalidate and Restart`
3. Delete `.gradle` folder and sync again

### Issue: App crashes on launch
**Solution**:
1. Check Logcat for error messages
2. Ensure minimum SDK version 26 is met
3. Verify all dependencies are properly installed

### Issue: Biometric authentication not working
**Solution**:
- Physical device: Set up fingerprint/face unlock in Settings
- Emulator: Use Extended Controls to simulate fingerprint

### Issue: Database errors
**Solution**:
1. Uninstall the app from device/emulator
2. Clean and rebuild the project
3. Reinstall the app

## Building APK for Distribution

### Debug APK:
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (requires signing):
1. Generate a keystore:
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-alias
   ```

2. Add to `app/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("path/to/my-release-key.jks")
               storePassword = "your-store-password"
               keyAlias = "my-alias"
               keyPassword = "your-key-password"
           }
       }
       buildTypes {
           getByName("release") {
               signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```

3. Build:
   ```bash
   ./gradlew assembleRelease
   ```

## Testing Checklist

After setup, test these features:

- [ ] App launches without crashes
- [ ] Navigate to all 5 main screens
- [ ] Add a new frog
- [ ] Add a new activity
- [ ] Attach activity to frog
- [ ] View calendar (should show empty initially)
- [ ] Add activity logs
- [ ] Test biometric authentication on View Frog screen
- [ ] Export data to JSON
- [ ] Import data from JSON
- [ ] Modify settings thresholds

## Next Steps

Once the app is running:
1. Create your first frog
2. Define some activities (e.g., "Exercise", "Study", "Sleep")
3. Attach activities to your frogs
4. Start tracking daily activities in the Calendar
5. Watch your frogs gain wealth and level up!

## Additional Resources

- [Android Documentation](https://developer.android.com/docs)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Material Design 3](https://m3.material.io/)

## Need Help?

If you encounter issues not covered here:
1. Check the Android Studio Build log
2. Review Logcat for runtime errors
3. Ensure all dependencies are up to date
4. Try Stack Overflow for specific error messages

Happy Frog Tracking! 🐸✨
