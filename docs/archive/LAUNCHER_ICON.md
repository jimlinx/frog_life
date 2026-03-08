# 🐸 Frog Launcher Icon

## Created Icons

I've created **two adorable anime-style frog icons** for you:

### 1. **Crowned Frog** (Currently Active) 👑
**File**: `ic_launcher_foreground.xml`

Features:
- ✅ Cute frog with big anime eyes
- ✅ Golden crown with pink jewels
- ✅ Smiling face
- ✅ Green body with lighter belly
- ✅ Four legs
- ✅ White eye highlights (anime style)
- ✅ Lily pad green background

**Theme**: Represents the "leader frog" and gamification aspect of the app!

### 2. **Simple Frog** (Alternative)
**File**: `ic_launcher_simple.xml`

Same design but **without the crown** for a cleaner look.

## How to Switch Between Icons

If you want to use the simple frog instead of the crowned one:

### Option 1: Via Android Studio
1. Rename `ic_launcher_foreground.xml` to `ic_launcher_foreground_crown.xml`
2. Rename `ic_launcher_simple.xml` to `ic_launcher_foreground.xml`
3. Sync project

### Option 2: Via Command Line
```bash
cd app/src/main/res/drawable/
mv ic_launcher_foreground.xml ic_launcher_foreground_crown.xml
mv ic_launcher_simple.xml ic_launcher_foreground.xml
```

### Option 3: Edit the File
Replace the contents of `ic_launcher_foreground.xml` with the contents of `ic_launcher_simple.xml`

## Icon Colors

The frog uses your app's theme colors:

| Element | Color | Hex |
|---------|-------|-----|
| Background | Lily Pad Green | #66BB6A |
| Body | Frog Green | #4CAF50 |
| Belly | Light Green | #81C784 |
| Eyes (dark) | Dark Green | #2E7D32 |
| Crown | Gold | #FFD700 |
| Jewels | Sakura Pink | #FF6B9D |

## What It Looks Like

### On Different Backgrounds

The adaptive icon will show:
- **Round devices**: Circular frog
- **Square devices**: Rounded square frog
- **Squircle devices**: Squircle frog

The green lily pad background ensures the frog looks good everywhere!

## Customizing the Icon

Want to modify the frog? Here's what you can change:

### Change Crown Color
Find this line in `ic_launcher_foreground.xml`:
```xml
<path
    android:fillColor="#FFD700"  <!-- Change this hex color -->
    ...
```

### Remove Crown Entirely
Delete these sections from `ic_launcher_foreground.xml`:
```xml
<!-- Crown (optional - for fun) -->
<!-- Crown jewels -->
```

### Change Background Color
Find the first path:
```xml
<path
    android:fillColor="#66BB6A"  <!-- Change this -->
    android:pathData="M0,0h108v108h-108z"/>
```

### Make Eyes Bigger/Smaller
Adjust the eye paths (look for "Left eye background" and "Right eye background")

## Testing the Icon

### In Android Studio
1. Build the app
2. Look at the app icon in the emulator/device app drawer
3. Check notification shade for app icon
4. Check settings → apps list

### Preview in IDE
1. Open `res/mipmap-anydpi-v26/ic_launcher.xml`
2. Click the preview panel on the right
3. See the adaptive icon preview

## Icon Specifications

- **Size**: 108x108dp (adaptive icon safe zone: 72x72dp)
- **Format**: Vector drawable (scales perfectly to any size)
- **Android versions**: Works on Android 8.0+ (adaptive), fallback for older versions
- **File size**: ~3KB (very small!)

## Fun Facts

🎨 **Anime Style**: Big eyes with white highlights create the classic anime look
👑 **Crown**: Symbolizes the "leader frog" feature (most wealth points)
💚 **Green Theme**: Matches the app's Material 3 color scheme
🐸 **Smiling**: Friendly and welcoming design
✨ **Jewels**: Pink sakura-themed decorations

## Future Enhancements

Want to make it even better?

- [ ] Add subtle shadow under the frog
- [ ] Add more detail to legs (toes!)
- [ ] Create seasonal variants (Christmas hat, party hat, etc.)
- [ ] Animated adaptive icon (Android 13+)
- [ ] Different expressions (happy, surprised, etc.)

## Current Icon

**Active**: Crowned Frog 👑🐸

The icon is ready to use and will appear when you build the app!

---

Made with 💚 and vector magic!
