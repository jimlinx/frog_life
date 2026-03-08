#!/bin/bash

echo "🐸 Frog Life - FINAL Installation Attempt"
echo "=========================================="
echo ""
echo "This uses the FULL app with ALL fixes applied:"
echo "  ✅ Package declaration in manifest"
echo "  ✅ Proper icon references (@mipmap)"
echo "  ✅ AppCompat theme"
echo "  ✅ Activity name shorthand (.MainActivity)"
echo "  ✅ All launcher icons generated"
echo ""

# Step 1: Clean everything
echo "1️⃣  Deep cleaning..."
./gradlew clean
rm -rf app/build
rm -rf build
rm -rf .gradle/build-cache
echo "✅ Cleaned"
echo ""

# Step 2: Build
echo "2️⃣  Building APK..."
./gradlew assembleDebug --no-daemon
BUILD_RESULT=$?
if [ $BUILD_RESULT -ne 0 ]; then
    echo ""
    echo "❌ BUILD FAILED!"
    echo "Check errors above."
    exit 1
fi
echo "✅ Build successful!"
echo ""

# Step 3: Verify APK
if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "❌ APK not found!"
    exit 1
fi
APK_SIZE=$(ls -lh app/build/outputs/apk/debug/app-debug.apk | awk '{print $5}')
echo "📦 APK created: $APK_SIZE"
echo ""

# Step 4: Completely uninstall
echo "3️⃣  Removing ALL traces of old app..."
adb uninstall com.froglife 2>/dev/null
adb shell pm clear com.froglife 2>/dev/null
adb shell rm -rf /data/data/com.froglife 2>/dev/null
echo "✅ Old app removed"
echo ""

# Step 5: Install fresh
echo "4️⃣  Installing fresh copy..."
adb install app/build/outputs/apk/debug/app-debug.apk
INSTALL_RESULT=$?
if [ $INSTALL_RESULT -ne 0 ]; then
    echo ""
    echo "❌ INSTALL FAILED!"
    echo ""
    echo "Trying alternative install method..."
    adb install -r -t app/build/outputs/apk/debug/app-debug.apk
    INSTALL_RESULT=$?
fi

if [ $INSTALL_RESULT -ne 0 ]; then
    echo "❌ Installation failed with all methods!"
    exit 1
fi
echo "✅ Installed successfully!"
echo ""

# Step 6: Verify installation
echo "5️⃣  Verifying installation..."
if adb shell pm list packages | grep -q "com.froglife"; then
    echo "✅ Package verified: com.froglife"
else
    echo "❌ Package NOT found!"
    exit 1
fi
echo ""

# Step 7: Check launcher activity
echo "6️⃣  Checking launcher activity..."
LAUNCHER_INFO=$(adb shell cmd package resolve-activity --brief com.froglife | grep com.froglife)
if [ ! -z "$LAUNCHER_INFO" ]; then
    echo "✅ Launcher activity found:"
    echo "   $LAUNCHER_INFO"
else
    echo "⚠️  Warning: Launcher activity not resolved"
fi
echo ""

# Step 8: Grant any needed permissions
echo "7️⃣  Granting permissions..."
adb shell pm grant com.froglife android.permission.USE_BIOMETRIC 2>/dev/null || echo "   (Biometric permission not needed for basic function)"
echo "✅ Permissions set"
echo ""

# Step 9: Launch
echo "8️⃣  Launching app..."
sleep 1
adb shell am start -n com.froglife/.MainActivity
LAUNCH_RESULT=$?
echo ""

# Results
echo "=========================================="
echo ""
if [ $LAUNCH_RESULT -eq 0 ]; then
    echo "✅ Launch command executed successfully!"
    echo ""
    echo "NOW CHECK YOUR DEVICE:"
    echo ""
    echo "1. Did the app open on screen?"
    echo "   ✅ YES → App works! Check launcher for icon."
    echo "   ❌ NO  → Run: adb logcat -s AndroidRuntime:E"
    echo ""
    echo "2. Do you see the icon in app drawer?"
    echo "   ✅ YES → SUCCESS! Everything works!"
    echo "   ❌ NO  → Try these:"
    echo "      - Restart device"
    echo "      - Check Settings → Apps for 'Frog Life'"
    echo "      - Clear launcher cache"
    echo ""
else
    echo "❌ Launch command failed!"
    echo ""
    echo "Check logcat for errors:"
    echo "  adb logcat -s AndroidRuntime:E ActivityManager:I"
    echo ""
fi

echo "=========================================="
echo ""
