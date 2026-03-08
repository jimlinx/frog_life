package com.froglife.utils

/**
 * Image Picker Helper - No Permissions Required!
 *
 * This app uses the Android Photo Picker and Storage Access Framework,
 * which provide one-time access to user-selected images without requiring
 * any storage permissions.
 *
 * This is the privacy-preserving approach recommended by Google Play.
 *
 * Example usage:
 * Use ActivityResultContracts.PickVisualMedia() for photo selection
 * Use ActivityResultContracts.GetContent() as fallback for older devices
 *
 * Why no permissions needed?
 * - Photo Picker: System UI handles selection, no permission required
 * - GetContent: User explicitly selects file, one-time access granted
 * - More privacy-friendly and Google Play compliant
 *
 * See NO_PERMISSIONS_NEEDED.md for complete code examples
 */
object ImagePickerHelper {
    // This is a documentation class - no implementation needed
    // Use ActivityResultContracts.PickVisualMedia() or GetContent() directly in your screens
}
