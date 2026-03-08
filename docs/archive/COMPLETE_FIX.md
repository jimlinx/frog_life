# Complete Fix for App Not Showing in Launcher

## Changes Made

### 1. Fixed Theme Compatibility ✅
**Problem**: `FragmentActivity` requires AppCompat theme, not Material theme
**Fix**: Changed `themes.xml`
```xml
<style name="Theme.FrogLife" parent="Theme.AppCompat.Light.NoActionBar">
```

### 2. Added AppCompat Dependencies ✅
**Added to `build.gradle.kts`**:
```kotlin
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("androidx.fragment:fragment-ktx:1.6.2")
```

### 3. Fixed Activity Declaration ✅
**Updated `AndroidManifest.xml`**:
- Used fully qualified activity name: `com.froglife.MainActivity`
- Added explicit label
- Kept drawable icon reference

## Build and Install Steps

### Step 1: Clean Everything
```bash
cd /Users/jimlin/Repos/frog_life

# Stop Gradle daemon
./gradlew --stop

# Clean build
./gradlew clean
```

### Step 2: Sync in Android Studio
1. Open project in Android Studio
2. **File** → **Sync Project with Gradle Files**
3. Wait for sync to complete (will download AppCompat dependencies)

### Step 3: Rebuild
```bash
# Build the app
./gradlew assembleDebug
```

### Step 4: Completely Uninstall Old Version
```bash
# Method 1: Via ADB (if available)
adb uninstall com.froglife

# Method 2: Manually on device
# Go to Settings → Apps → Frog Life → Uninstall
```

### Step 5: Install Fresh
```bash
./gradlew installDebug
```

### Step 6: Restart Launcher
On your device:
1. Go to device Settings
2. Apps → Default apps → Home app
3. Switch to a different launcher, then back
4. Or simply restart your device

## If Still Not Showing

### Check 1: Verify Build Success
```bash
# Should see SUCCESS
./gradlew assembleDebug
```

### Check 2: Verify Installation
```bash
# Should show com.froglife
adb shell pm list packages | grep froglife
```

### Check 3: Try Launching Directly
```bash
# Force launch the app
adb shell am start -n com.froglife/.MainActivity
```

If this works but icon doesn't show, it's a launcher cache issue.

### Check 4: Clear Launcher Cache
On device:
1. Settings → Apps → [Your Launcher App]
2. Storage → Clear Cache
3. Restart launcher

### Check 5: Use Android Studio
1. **Run** → **Run 'app'** (▶️)
2. Select your device
3. Watch logcat for errors

## Common Issues and Solutions

### Issue: "Theme not found"
**Solution**: Sync Gradle to download AppCompat
```bash
./gradlew --refresh-dependencies
```

### Issue: "Activity class not found"
**Solution**: Clean and rebuild
```bash
./gradlew clean build
```

### Issue: Icon shows as Android robot
**Solution**: This is expected for now - drawable icons work but may look generic. Use Image Asset Studio to create proper PNG icons.

### Issue: App crashes on start
**Solution**: Check logcat:
```bash
adb logcat -s AndroidRuntime:E
```

## Generate Proper Launcher Icons

The app will work with current setup, but for professional icons:

### In Android Studio:
1. **Right-click `res` folder**
2. **New** → **Image Asset**
3. **Asset Type**: Launcher Icons (Adaptive and Legacy)
4. **Icon Configuration**:
   - **Foreground**: Use Clip Art → search "frog" or "emoji"
   - **Background**: Color → `#66BB6A`
5. **Next** → **Finish**

This generates all required PNG files automatically.

Then update `AndroidManifest.xml`:
```xml
android:icon="@mipmap/ic_launcher"
android:roundIcon="@mipmap/ic_launcher_round"
```

## Verification Checklist

After rebuild and install:

- [ ] App builds without errors
- [ ] App installs successfully
- [ ] Icon appears in launcher
- [ ] App opens when tapped
- [ ] No crashes on startup
- [ ] Main screen loads

## Quick Rebuild Script

Save as `rebuild.sh`:
```bash
#!/bin/bash
./gradlew --stop
./gradlew clean
./gradlew assembleDebug
adb uninstall com.froglife 2>/dev/null
./gradlew installDebug
echo "Done! Check your launcher for Frog Life"
```

Make executable and run:
```bash
chmod +x rebuild.sh
./rebuild.sh
```

## What Changed vs Before

| Before | After |
|--------|-------|
| Material theme | AppCompat theme ✅ |
| No AppCompat dependency | AppCompat added ✅ |
| Relative activity name | Fully qualified name ✅ |
| May crash on FragmentActivity | Works properly ✅ |

## Expected Result

After following all steps:
1. ✅ App appears in launcher as "Frog Life"
2. ✅ Icon visible (may be generic until PNG icons generated)
3. ✅ App opens without crash
4. ✅ Main screen shows with navigation buttons

The main fixes were:
- **AppCompat theme** for FragmentActivity compatibility
- **AppCompat dependencies** added
- **Fully qualified activity name** in manifest

Try the rebuild now! 🐸

---

Updated: Feb 27, 2026
