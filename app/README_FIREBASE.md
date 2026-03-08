# Firebase Configuration File Required

## 🔴 Missing: google-services.json

This file is required for Firebase Cloud Messaging (push notifications) to work.

---

## How to Get It

### 1. Go to Firebase Console
https://console.firebase.google.com/

### 2. Create Project (if not exists)
- Click **Add Project**
- Name: "Frog Life"
- Disable Analytics (optional)
- Click **Create Project**

### 3. Add Android App
- Click **Add App** → **Android**
- Package name: `com.froglife`
- Click **Register App**

### 4. Download File
- Click **Download google-services.json**
- Move to this directory:
  ```bash
  mv ~/Downloads/google-services.json app/google-services.json
  ```

---

## File Location

The file should be placed here:
```
app/
├── build.gradle.kts
├── google-services.json  ← Place here
└── src/
```

---

## Security

⚠️ **DO NOT commit this file to git!**

It's already in `.gitignore`:
```
app/google-services.json
```

---

## Example File

See `google-services.json.example` for file structure.

---

## Build Without Firebase

If you don't want to use Firebase (no push notifications), you can:

1. Remove Firebase plugin from `app/build.gradle.kts`:
   ```kotlin
   // Comment out this line:
   // id("com.google.gms.google-services")
   ```

2. App will still work, but:
   - ❌ No instant push notifications
   - ❌ No auto-sync on approval
   - ✅ Manual sync still works (Pull Latest button)

---

## Questions?

See: **FCM_SETUP_GUIDE.md** in project root
