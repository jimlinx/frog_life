# Fix: App Not Showing in Launcher

## Problem Identified ✅
The app was missing PNG launcher icon files. Only XML adaptive icons existed, which aren't sufficient for all Android versions.

## Quick Fix Applied
Updated `AndroidManifest.xml` to temporarily use the drawable icon:
```xml
android:icon="@drawable/ic_launcher_foreground"
android:roundIcon="@drawable/ic_launcher_foreground"
```

This should make the app appear in the launcher now!

## Proper Solution: Generate Launcher Icons in Android Studio

For a production-ready app with proper launcher icons:

### Method 1: Use Android Studio Image Asset Studio (Recommended)

1. **Right-click on `res` folder** in Android Studio
2. **Select**: `New` → `Image Asset`
3. **Configure**:
   - Asset Type: `Launcher Icons (Adaptive and Legacy)`
   - Name: `ic_launcher`
   - Foreground Layer:
     - Source Asset: Choose `Clip Art`
     - Click the icon button and search for "frog" or use any frog emoji 🐸
     - Or use `Image` and upload a frog image
   - Background Layer:
     - Color: `#66BB6A` (our lily pad green)
4. **Click Next** → **Finish**

This will generate:
- PNG files for all densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- Adaptive icon XML files
- Proper launcher icons for all Android versions

### Method 2: Use the Cute Frog Icon We Created

The project already has a nice frog icon in `ic_launcher_foreground.xml`! To make it work properly:

1. **In Android Studio**: `Tools` → `Android` → `Image Asset`
2. **Asset Type**: Launcher Icons (Adaptive and Legacy)
3. **Foreground Layer**:
   - Source Asset: `Image`
   - Path: Select any frog image (or use the frog emoji 🐸)
4. **Background Layer**:
   - Color: `#66BB6A`
5. **Generate** → All density PNG files will be created

### Method 3: Quick Command Line Fix (Temporary)

Create a simple placeholder icon:
```bash
cd /Users/jimlin/Repos/frog_life/app/src/main/res

# Create a simple text file as placeholder (Android Studio will handle it)
# This is just to get the app showing - use Method 1 for proper icons
```

## Why This Happened

Android launcher icons require:
1. ✅ **Adaptive Icons** (API 26+): XML files in `mipmap-anydpi-v26/` - We have these
2. ❌ **Legacy PNG Icons**: PNG files in `mipmap-{density}/` - We were missing these

Without the PNG files, devices running Android 7.1 and below won't show the app icon, and some launchers won't display it properly.

## After Generating Icons

Once you generate proper icons in Android Studio:

1. **Update AndroidManifest.xml** back to use mipmap:
```xml
android:icon="@mipmap/ic_launcher"
android:roundIcon="@mipmap/ic_launcher_round"
```

2. **Uninstall the app** from your device:
```bash
adb uninstall com.froglife
```

3. **Rebuild and install**:
```bash
./gradlew clean
./gradlew installDebug
```

## Current Status

✅ **App should now appear** in launcher (using drawable icon as fallback)
⚠️ **Icon may look basic** - use Image Asset Studio to make it look professional
📱 **Works on all Android versions** - but proper PNG icons recommended

## Testing

After applying the fix:

1. **Uninstall old version**:
   ```bash
   adb uninstall com.froglife
   ```

2. **Clean and rebuild**:
   ```bash
   ./gradlew clean
   ./gradlew installDebug
   ```

3. **Check launcher** - app should now appear with icon

4. **Open app** - should launch to main screen

## Expected Icon Appearance

With the current fix:
- 🐸 You'll see the frog drawing we created
- 🟢 Green lily pad background
- 👑 Crown on top (leader frog theme)

With proper Image Asset generation:
- 🎨 Professional-looking adaptive icon
- 📱 Proper sizing for all devices
- ✨ Smooth edges and correct dimensions

## Summary

**Immediate Fix**: ✅ Changed manifest to use drawable icon
**Long-term Fix**: Use Android Studio Image Asset Studio to generate proper PNG icons

The app should now appear in your launcher! 🎉

---

Updated: Feb 27, 2026
