#!/bin/bash

echo "🐸 Frog Life - Complete Rebuild Script"
echo "======================================="
echo ""

echo "1️⃣  Stopping Gradle daemon..."
./gradlew --stop
echo "✅ Daemon stopped"
echo ""

echo "2️⃣  Cleaning build..."
./gradlew clean
echo "✅ Clean complete"
echo ""

echo "3️⃣  Building debug APK..."
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Build failed! Check errors above."
    exit 1
fi
echo "✅ Build successful"
echo ""

echo "4️⃣  Uninstalling old version..."
adb uninstall com.froglife 2>/dev/null || echo "   (No previous version found - OK)"
echo "✅ Old version removed"
echo ""

echo "5️⃣  Installing new version..."
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "❌ Install failed! Is device connected?"
    echo ""
    echo "Try manually:"
    echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
    exit 1
fi
echo "✅ App installed"
echo ""

echo "6️⃣  Launching app..."
adb shell am start -n com.froglife/.MainActivity
echo "✅ App launched"
echo ""

echo "🎉 Done!"
echo ""
echo "The app should now:"
echo "  ✅ Appear in your launcher as 'Frog Life'"
echo "  ✅ Have a green frog icon"
echo "  ✅ Open when tapped"
echo ""
echo "If you don't see the icon:"
echo "  1. Restart your device launcher (Settings → Apps → Default apps → Home)"
echo "  2. Or restart your device"
echo "  3. Clear launcher cache (Settings → Apps → [Launcher] → Clear Cache)"
echo ""
