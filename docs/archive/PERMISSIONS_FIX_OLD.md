# Android 13+ Permissions Fix

## Issue Fixed
The deprecated `READ_EXTERNAL_STORAGE` permission has been properly configured for Android 13+ compatibility.

## Changes Made

### 1. Updated AndroidManifest.xml ✅
```xml
<!-- For Android 13+ (API 33+): Use granular media permissions -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />

<!-- For Android 12 and below: Legacy permission -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

**How it works:**
- Android 13+ (API 33+): Uses `READ_MEDIA_IMAGES` only
- Android 12 and below (API 32-): Uses `READ_EXTERNAL_STORAGE` only
- No deprecation warnings!

### 2. Created PermissionHelper.kt ✅
A utility class that handles permission checks across Android versions.

**Key Features:**
- Automatically selects correct permission based on Android version
- Easy permission checking: `hasImagePermission(context)`
- Ready for runtime permission requests

## How to Use (For Future Custom Image Picker)

### In a Composable Screen:

```kotlin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.froglife.utils.PermissionHelper

@Composable
fun AddEditFrogScreen(...) {
    val context = LocalContext.current
    var showImagePicker by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showImagePicker = true
        } else {
            // Show "Permission denied" message
            Toast.makeText(
                context,
                "Permission needed to select images",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Save the image URI to frog profile
            profileImageUri = it.toString()
        }
    }

    // Button to select image
    Button(onClick = {
        if (PermissionHelper.hasImagePermission(context)) {
            // Permission already granted, open picker
            imagePickerLauncher.launch("image/*")
        } else {
            // Request permission first
            permissionLauncher.launch(PermissionHelper.getImagePermission())
        }
    }) {
        Text("Select Custom Image")
    }

    // Launch image picker after permission granted
    LaunchedEffect(showImagePicker) {
        if (showImagePicker) {
            imagePickerLauncher.launch("image/*")
            showImagePicker = false
        }
    }
}
```

## Permission Behavior by Android Version

| Android Version | API Level | Permission Used | Notes |
|----------------|-----------|-----------------|-------|
| Android 13+ | 33+ | `READ_MEDIA_IMAGES` | Granular permission for images only |
| Android 12L | 32 | `READ_EXTERNAL_STORAGE` | Legacy permission |
| Android 12 | 31 | `READ_EXTERNAL_STORAGE` | Legacy permission |
| Android 11 | 30 | `READ_EXTERNAL_STORAGE` | Legacy permission |
| Android 10 | 29 | `READ_EXTERNAL_STORAGE` | Scoped storage introduced |
| Android 9 | 28 | `READ_EXTERNAL_STORAGE` | Legacy permission |
| Android 8 | 26-27 | `READ_EXTERNAL_STORAGE` | App's minSdk |

## Why This Approach?

### Before (❌ Deprecated):
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
- Works on older Android, but deprecated on Android 13+
- Too broad (grants access to ALL files)
- Shows warning in Android Studio

### After (✅ Modern):
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```
- Uses appropriate permission for each Android version
- More privacy-friendly (only image access)
- No warnings, follows best practices
- Future-proof

## Testing the Permission

### Test on Android 13+ (Emulator or Device):
1. Build and install the app
2. Try to select a custom image
3. Permission dialog should ask for "Photos and videos" access
4. Grant permission
5. Image picker should open

### Test on Android 12 and Below:
1. Same flow
2. Permission dialog asks for "Storage" access
3. Grant permission
4. Image picker should open

## Current App Status

### ✅ What Works Now:
- Preset frog icons (🐸🐊🦎🐢🦖)
- All core features
- No runtime permission needed (preset icons only)

### 🔧 Future Enhancement (When Adding Custom Images):
1. Update `AddEditFrogScreen.kt` to include image picker button
2. Use `PermissionHelper` to check/request permission
3. Use `ActivityResultContracts.GetContent()` to pick image
4. Save image URI to frog profile
5. Display custom image with Coil library

## Example: Complete Custom Image Implementation

Here's a complete example for when you're ready to add custom image support:

```kotlin
@Composable
fun CustomImagePicker(
    currentImageUri: String?,
    onImageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var showPermissionDenied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            showPermissionDenied = true
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it.toString()) }
    }

    Column {
        // Show current image
        if (currentImageUri != null) {
            AsyncImage(
                model = currentImageUri,
                contentDescription = "Selected image",
                modifier = Modifier.size(100.dp)
            )
        }

        Button(onClick = {
            when {
                PermissionHelper.hasImagePermission(context) -> {
                    imagePickerLauncher.launch("image/*")
                }
                else -> {
                    permissionLauncher.launch(
                        PermissionHelper.getImagePermission()
                    )
                }
            }
        }) {
            Text("Select Custom Image")
        }

        if (showPermissionDenied) {
            Text(
                "Permission denied. Please enable in Settings.",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
```

## Additional Notes

### Photo Picker (Android 13+)
Android 13+ also introduced the Photo Picker, which doesn't require any permissions:

```kotlin
val photoPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    uri?.let { onImageSelected(it.toString()) }
}

// Use it
photoPickerLauncher.launch(
    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
)
```

This is even better for privacy! Consider using this for Android 13+ devices.

### Storage Access Framework
For maximum compatibility and privacy, you can also use Storage Access Framework:
- No permissions required
- Works on all Android versions
- User explicitly picks files through system UI

## Summary

✅ **Fixed**: Deprecated permission warning
✅ **Added**: Version-aware permission handling
✅ **Created**: `PermissionHelper` utility
✅ **Ready**: For future custom image picker implementation
✅ **Compatible**: Android 8.0 (API 26) to Android 14+ (API 34+)

The app now follows Android 13+ best practices while maintaining backward compatibility!

---

Updated: Feb 27, 2026
