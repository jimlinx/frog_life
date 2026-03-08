#!/bin/bash

echo "🐸 Frog Life - Fix and Install Script"
echo "======================================"
echo ""

echo "Step 1: Uninstalling old version..."
adb uninstall com.froglife 2>/dev/null
echo "✅ Old version uninstalled (if it existed)"
echo ""

echo "Step 2: Cleaning build..."
./gradlew clean
echo "✅ Clean complete"
echo ""

echo "Step 3: Building app..."
./gradlew assembleDebug
echo "✅ Build complete"
echo ""

echo "Step 4: Installing app..."
./gradlew installDebug
echo "✅ App installed"
echo ""

echo "🎉 Done! Check your app launcher for 'Frog Life' with the frog icon 🐸"
echo ""
echo "If you don't see it:"
echo "1. Restart your device launcher (long press home)"
echo "2. Check All Apps drawer"
echo "3. Search for 'Frog Life' in launcher search"
echo ""
