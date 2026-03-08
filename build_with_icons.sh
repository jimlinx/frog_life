#!/bin/bash

echo "🎉 Building Frog Life with Proper Launcher Icons"
echo "================================================"
echo ""

echo "✅ Launcher icon files detected:"
ls -1 app/src/main/res/mipmap-*/ic_launcher* | wc -l | xargs echo "   " files
echo ""

echo "1️⃣  Cleaning previous build..."
./gradlew clean
echo ""

echo "2️⃣  Building APK..."
./gradlew assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Build failed! Check errors above."
    exit 1
fi
echo "✅ Build successful!"
echo ""

echo "3️⃣  Uninstalling old version..."
adb uninstall com.froglife 2>/dev/null || echo "   (No previous version)"
echo ""

echo "4️⃣  Installing new version..."
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo "❌ Install failed!"
    echo ""
    echo "Manual install:"
    echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
    exit 1
fi
echo "✅ App installed!"
echo ""

echo "5️⃣  Launching app..."
sleep 2
adb shell am start -n com.froglife/.MainActivity
echo ""

echo "================================================"
echo "🎊 Done!"
echo ""
echo "Check your device:"
echo "  📱 Look in app drawer for 'Frog Life'"
echo "  🤖 Icon should show Android robot (green background)"
echo "  🐸 App should open when tapped"
echo ""
echo "If icon doesn't appear:"
echo "  1. Restart your device"
echo "  2. Check Settings → Apps for 'Frog Life'"
echo "  3. Try clearing launcher cache"
echo ""
