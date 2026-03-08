#!/bin/bash

echo "🧪 Testing with ABSOLUTE MINIMAL Setup"
echo "======================================="
echo ""
echo "This will test with:"
echo "  - Simplest possible manifest"
echo "  - Plain Activity (not FragmentActivity)"
echo "  - No Compose, no themes, no complexity"
echo "  - Just a TextView with text"
echo ""
read -p "Press Enter to continue..."
echo ""

# Backup current files
echo "1️⃣  Backing up current files..."
cp app/src/main/AndroidManifest.xml app/src/main/AndroidManifest.xml.backup
echo "✅ Backup created"
echo ""

# Swap to minimal
echo "2️⃣  Switching to minimal setup..."
cp app/src/main/AndroidManifest.xml app/src/main/AndroidManifest.xml.full
cp app/src/main/AndroidManifest_MINIMAL.xml app/src/main/AndroidManifest.xml
echo "✅ Using minimal manifest"
echo ""

# Build
echo "3️⃣  Building..."
./gradlew clean assembleDebug
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    # Restore
    cp app/src/main/AndroidManifest.xml.full app/src/main/AndroidManifest.xml
    exit 1
fi
echo "✅ Build successful"
echo ""

# Uninstall
echo "4️⃣  Uninstalling old version..."
adb uninstall com.froglife 2>/dev/null
echo ""

# Install
echo "5️⃣  Installing..."
adb install app/build/outputs/apk/debug/app-debug.apk
if [ $? -ne 0 ]; then
    echo "❌ Install failed!"
    # Restore
    cp app/src/main/AndroidManifest.xml.full app/src/main/AndroidManifest.xml
    exit 1
fi
echo "✅ Installed"
echo ""

# Check
echo "6️⃣  Checking installation..."
if adb shell pm list packages | grep -q "com.froglife"; then
    echo "✅ App IS installed"
else
    echo "❌ App NOT found!"
fi
echo ""

# Try to launch
echo "7️⃣  Launching..."
adb shell am start -n com.froglife/.TestActivity
echo ""

echo "======================================"
echo ""
echo "NOW CHECK YOUR DEVICE:"
echo ""
echo "✅ If app launched and shows 'Frog Life Works!':"
echo "   → The problem is in the complex MainActivity/theme/Compose setup"
echo "   → We can fix by simplifying step by step"
echo ""
echo "✅ If icon appears in launcher:"
echo "   → Icon issue is SOLVED!"
echo "   → The problem was with the complex setup"
echo ""
echo "❌ If nothing happened:"
echo "   → Check logcat for errors"
echo "   → Run: adb logcat -s AndroidRuntime:E"
echo ""
echo "❌ If icon still doesn't appear:"
echo "   → Fundamental device/launcher issue"
echo "   → Try different device or restart"
echo ""
echo "To restore full version:"
echo "  cp app/src/main/AndroidManifest.xml.full app/src/main/AndroidManifest.xml"
echo "  ./gradlew clean assembleDebug"
echo "  adb install -r app/build/outputs/apk/debug/app-debug.apk"
echo ""
