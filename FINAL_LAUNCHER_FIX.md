## 🐸 Final Launcher Fix - Complete Checklist

The app is not appearing in the launcher after install. Let's systematically fix this.

### Option 1: Run Diagnostic (If you have ADB access)

```bash
cd /Users/jimlin/Repos/frog_life
chmod +x diagnose.sh
./diagnose.sh
```

This will tell us if:
- App is installed
- App can launch manually
- Any crashes are occurring

### Option 2: Use Android Studio Method (Recommended)

#### Step 1: Generate Proper Launcher Icons

1. In Android Studio, **right-click on `res` folder**
2. **New** → **Image Asset**
3. **Asset Type**: "Launcher Icons (Adaptive and Legacy)"
4. **Name**: `ic_launcher`
5. **Foreground Layer**:
   - Source Asset: **Clip Art**
   - Click icon button, search for "android" or "star"
   - Or choose **Text** and type "🐸"
6. **Background Layer**:
   - Color: `#66BB6A` (green)
7. Click **Next** → **Finish**

This generates all the PNG files needed!

#### Step 2: Update Manifest to Use Generated Icons

Edit `AndroidManifest.xml`:

```xml
<application
    ...
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    ...>
```

#### Step 3: Clean and Rebuild

1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. Uninstall from device (Settings → Apps → Frog Life → Uninstall)
4. **Run** → **Run 'app'** (▶️)

### Option 3: Test with Minimal App First

If the above doesn't work, let's test with a minimal version:

```bash
cd /Users/jimlin/Repos/frog_life
chmod +x test_simple.sh
./test_simple.sh
```

This switches to a super simple MainActivity to isolate the problem.

### Common Issues Checklist

#### ✅ Check 1: App Name in strings.xml

File: `app/src/main/res/values/strings.xml`

```xml
<string name="app_name">Frog Life</string>
```

Should be simple, no special characters.

#### ✅ Check 2: Theme Parent

File: `app/src/main/res/values/themes.xml`

```xml
<style name="Theme.FrogLife" parent="Theme.AppCompat.Light.NoActionBar">
```

Must use AppCompat for FragmentActivity!

#### ✅ Check 3: Activity in Manifest

```xml
<activity
    android:name="com.froglife.MainActivity"
    android:exported="true"
    android:label="@string/app_name">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

Must have `android:exported="true"` and correct intent-filter!

#### ✅ Check 4: Launcher Icons Exist

Run this in terminal:

```bash
ls -la /Users/jimlin/Repos/frog_life/app/src/main/res/mipmap-*/ic_launcher.*
```

You should see PNG files in multiple folders. If not, generate icons (Step 1 above).

### Manual Icon Creation (If Android Studio method fails)

Create these files manually with solid green color:

```bash
cd /Users/jimlin/Repos/frog_life/app/src/main/res

# Create simple colored square PNGs (using ImageMagick if available)
convert -size 48x48 xc:#66BB6A mipmap-mdpi/ic_launcher.png
convert -size 72x72 xc:#66BB6A mipmap-hdpi/ic_launcher.png
convert -size 96x96 xc:#66BB6A mipmap-xhdpi/ic_launcher.png
convert -size 144x144 xc:#66BB6A mipmap-xxhdpi/ic_launcher.png
convert -size 192x192 xc:#66BB6A mipmap-xxxhdpi/ic_launcher.png
```

Or use an online PNG generator to create solid green squares at these sizes.

### If Still Not Working: Check Device Settings

1. **Restart Device** - This clears launcher cache
2. **Check App is Installed**:
   - Settings → Apps → Look for "Frog Life"
   - If it's there, the issue is launcher cache
3. **Change Launcher**:
   - Settings → Apps → Default apps → Home app
   - Switch to different launcher, then back
4. **Clear Launcher Data**:
   - Settings → Apps → [Your Launcher]
   - Storage → Clear Data (WARNING: Resets home screen layout)

### Nuclear Option: Simplify Everything

If nothing works, we'll create the absolute minimal app:

1. Remove all complex features
2. Single screen with just text
3. Basic theme
4. Simple PNG icon

This will tell us if it's a fundamental issue or just complexity.

### Expected Behavior After Fix

✅ App appears in launcher as "Frog Life"
✅ Icon shows (even if just a green square)
✅ Tapping icon opens the app
✅ No crashes

### What to Try Next

**Option A**: Use Android Studio Image Asset (Most reliable)
**Option B**: Run diagnose.sh to see actual errors
**Option C**: Test with simple MainActivity
**Option D**: Manually create PNG icons

Let me know which option you'd like to try!

