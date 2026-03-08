#!/bin/bash

echo "🔍 Frog Life - Diagnostic Check"
echo "================================"
echo ""

echo "1️⃣  Checking if app is installed..."
if adb shell pm list packages | grep -q "com.froglife"; then
    echo "✅ App IS installed"
    adb shell pm list packages | grep froglife
else
    echo "❌ App is NOT installed!"
    echo "Please run: ./gradlew installDebug"
    exit 1
fi
echo ""

echo "2️⃣  Checking app info..."
adb shell dumpsys package com.froglife | grep -A 5 "Activity"
echo ""

echo "3️⃣  Attempting to launch app directly..."
adb shell am start -n com.froglife/.MainActivity
RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo "✅ Launch command sent"
else
    echo "❌ Launch failed"
fi
echo ""

echo "4️⃣  Checking for crash logs (last 50 lines)..."
adb logcat -d -s AndroidRuntime:E | tail -50
echo ""

echo "5️⃣  Checking app processes..."
adb shell ps | grep froglife
echo ""

echo "======================================"
echo "If app launched, check your device screen now!"
echo "If it crashed, see errors above."
echo ""
