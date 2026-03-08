#!/bin/bash

echo "🧪 Testing with Simple MainActivity"
echo "===================================="
echo ""

# Backup current MainActivity
echo "1️⃣  Backing up current MainActivity..."
cp app/src/main/java/com/froglife/MainActivity.kt app/src/main/java/com/froglife/MainActivity.kt.backup
echo "✅ Backup created"
echo ""

# Rename current to old
echo "2️⃣  Switching to simple version..."
mv app/src/main/java/com/froglife/MainActivity.kt app/src/main/java/com/froglife/MainActivity.kt.complex
mv app/src/main/java/com/froglife/MainActivitySimple.kt app/src/main/java/com/froglife/MainActivity.kt
echo "✅ Switched to simple MainActivity"
echo ""

echo "3️⃣  Building and installing..."
./gradlew clean assembleDebug
adb uninstall com.froglife 2>/dev/null
./gradlew installDebug
echo ""

echo "4️⃣  Launching app..."
adb shell am start -n com.froglife/.MainActivity
echo ""

echo "======================================"
echo "Check your device:"
echo "  ✅ If you see 'Frog Life' with a frog emoji - SUCCESS!"
echo "  ❌ If it crashes or doesn't appear - theme/manifest issue"
echo ""
echo "To restore original MainActivity:"
echo "  mv app/src/main/java/com/froglife/MainActivity.kt app/src/main/java/com/froglife/MainActivitySimple.kt"
echo "  mv app/src/main/java/com/froglife/MainActivity.kt.complex app/src/main/java/com/froglife/MainActivity.kt"
echo ""
