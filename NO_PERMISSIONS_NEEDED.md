# ✅ No Photo Permissions Required!

## Google Play Compliance

This app follows Google Play's privacy requirements by **NOT** requesting persistent photo access permissions.

## What Changed

### ❌ Removed (Not Compliant):
```xml
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

### ✅ Using Instead (Compliant):
- **Android Photo Picker** (`PickVisualMedia`) - No permissions needed!
- **Storage Access Framework** (`GetContent`) - No permissions needed!

## Why This is Better

### Google Play Requirements
> "To protect user privacy and security, Google Play requires apps that request READ_MEDIA_IMAGES or READ_MEDIA_VIDEO permissions to show strong, legitimate core use cases for **persistent or frequent** access to photos and videos."

### Our Use Case
- **What we need**: Occasional profile picture selection
- **Frequency**: Infrequent (only when adding/editing frog profile)
- **Best approach**: Photo Picker / GetContent (no permissions)

### Benefits
✅ **No permissions to request** - Better user experience
✅ **Google Play compliant** - No risk of rejection
✅ **More private** - User explicitly selects each image
✅ **Works on all Android versions** - Automatic fallback
✅ **No permission denial handling** - Simpler code

## How to Implement Custom Profile Pictures

When you're ready to add custom image selection to the app:

### Step 1: Add to AddEditFrogScreen.kt

```kotlin
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage

@Composable
fun AddEditFrogScreen(
    navController: NavController,
    frogId: Long?,
    frogViewModel: FrogViewModel,
    activityViewModel: ActivityViewModel
) {
    // ... existing state ...
    var customImageUri by remember { mutableStateOf<Uri?>(null) }

    // Photo Picker (Android 11+, backported to older versions)
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        customImageUri = uri
        // Save URI to frog profile
    }

    // Fallback for older Android versions
    val legacyPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        customImageUri = uri
        // Save URI to frog profile
    }

    // ... in your UI ...
    Column {
        // Show custom image if selected
        customImageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Custom profile picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        // Button to select custom image
        OutlinedButton(
            onClick = {
                try {
                    // Try Photo Picker first
                    photoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                } catch (e: Exception) {
                    // Fallback for very old devices
                    legacyPicker.launch("image/*")
                }
            }
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Custom Image")
        }

        // Or use preset icons
        Text("Or select preset icon:")
        // ... existing preset icon selector ...
    }
}
```

### Step 2: Persist the URI

The Photo Picker returns a content URI that persists. You need to take persistable permission:

```kotlin
val context = LocalContext.current

val photoPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    uri?.let {
        // Take persistable permission
        context.contentResolver.takePersistableUriPermission(
            it,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        // Now save the URI string to database
        customImageUri = it.toString()
    }
}
```

### Step 3: Display Custom Image with Coil

```kotlin
// In your UI where you show the frog image
if (frog.isPresetIcon) {
    // Show preset emoji
    Text(getFrogIcon(frog.presetIconId), fontSize = 80.sp)
} else {
    // Show custom image from URI
    AsyncImage(
        model = frog.profilePicturePath, // The URI string
        contentDescription = "Profile picture",
        modifier = Modifier.size(80.dp),
        error = painterResource(R.drawable.ic_launcher_foreground) // Fallback
    )
}
```

## Complete Example

Here's a ready-to-use component:

```kotlin
@Composable
fun ProfileImageSelector(
    currentImageUri: String?,
    isPresetIcon: Boolean,
    presetIconId: Int,
    onPresetSelected: (Int) -> Unit,
    onCustomImageSelected: (String) -> Unit
) {
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                onCustomImageSelected(it.toString())
            } catch (e: SecurityException) {
                // Permission not available, but image still works for this session
                onCustomImageSelected(it.toString())
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Show current selection
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            if (isPresetIcon) {
                Text(getFrogIcon(presetIconId), fontSize = 60.sp)
            } else if (currentImageUri != null) {
                AsyncImage(
                    model = currentImageUri,
                    contentDescription = "Profile picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Custom image button
        OutlinedButton(
            onClick = {
                photoPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Custom Image")
        }

        Text("Or select preset icon:", style = MaterialTheme.typography.labelMedium)

        // Preset icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0..4) {
                IconButton(
                    onClick = { onPresetSelected(i) },
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = if (isPresetIcon && presetIconId == i) 2.dp else 0.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Text(getFrogIcon(i), fontSize = 30.sp)
                }
            }
        }
    }
}
```

## How Photo Picker Works

### Android 13+ (API 33+)
- Native Photo Picker with modern UI
- Built into the OS
- No permissions required

### Android 11-12 (API 30-32)
- Backported Photo Picker via Google Play Services
- Same functionality
- No permissions required

### Android 10 and below (API 29-)
- Falls back to `GetContent()` (document picker)
- Still no permissions required
- User picks file via system UI

## Testing

### Test Photo Picker:
1. Run app on Android 11+ device/emulator
2. Click "Choose Custom Image"
3. Photo Picker UI appears (modern grid view)
4. Select an image
5. Image displays in app
6. **No permission dialogs!** ✅

### Test Legacy Picker:
1. Run app on Android 10 or below
2. Click "Choose Custom Image"
3. File picker appears
4. Select an image
5. Image displays in app
6. **No permission dialogs!** ✅

## What About Saving/Downloading Images?

If you later need to **save** images (not just display):

### For Profile Pictures:
- You don't need to save - just store the URI
- The URI persists and gives you read access

### For Export/Share Features:
- Use `MediaStore` API for saving to Pictures folder
- Or use `createDocument()` to let user choose save location
- Both approaches don't need permissions for saving

## Summary

| Approach | Permissions Needed | Google Play Compliant | Privacy | UX |
|----------|-------------------|---------------------|---------|-----|
| **READ_MEDIA_IMAGES** | ✋ Required | ❌ Only for core use cases | ⚠️ Broad access | 😐 Permission dialog |
| **Photo Picker** ✅ | ✅ None | ✅ Yes | ✅ Selective | 😊 No dialog needed |
| **GetContent** ✅ | ✅ None | ✅ Yes | ✅ Selective | 😊 No dialog needed |

## Current App Status

✅ **Manifest**: Clean, no photo permissions
✅ **Helper**: `ImagePickerHelper.kt` with examples
✅ **Compliance**: Fully Google Play compliant
✅ **Privacy**: Users explicitly select each image
✅ **Ready**: Easy to add custom images when needed

The app follows Android and Google Play best practices for privacy and permissions!

---

**No permissions = Better privacy = Happier users = Easier Google Play approval** 🎉

Updated: Feb 27, 2026
