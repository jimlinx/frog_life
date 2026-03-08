# Import from Bucket - Quick Guide

## Overview
The **Import from Bucket** feature allows you to browse all available backups in your GCS bucket and restore from any version.

---

## When to Use

### 1. **Initial Device Setup**
Setting up a new device? Import existing data from the bucket instead of starting from scratch.

```
New Device → Settings → Device Sync → Import from Bucket → Select "LATEST"
```

### 2. **Restore from Backup**
Need to restore data from a specific point in time? Choose any versioned backup.

```
Settings → Device Sync → Import from Bucket → Select backup by date
```

### 3. **Data Recovery**
Accidentally deleted important data? Restore from a recent backup.

```
Settings → Device Sync → Import from Bucket → Select recent backup
```

### 4. **Rollback Changes**
Made a mistake and want to undo? Roll back to a previous version.

```
Settings → Device Sync → Import from Bucket → Select backup before mistake
```

---

## How to Use

### Step 1: Open Import Screen
1. Open **Frog Life** app
2. Navigate to **Settings**
3. Scroll to **Device Sync** section
4. Tap **Import from Bucket**

### Step 2: Browse Backups
You'll see a list of all available backups, showing:
- **File Name**: `master_data` or `master_data_1234567890`
- **Timestamp**: When the backup was created
- **Size**: File size (typically a few KB)
- **"LATEST" Badge**: Indicates the current approved data

Backups are sorted by date (newest first).

### Step 3: Select Backup
Tap on any backup to select it for import.

### Step 4: Confirm Import
A confirmation dialog will appear:
```
⚠️ This will replace ALL local data!
Are you sure you want to import this backup?

File: master_data_1234567890
```

Tap **Import** to proceed or **Cancel** to go back.

### Step 5: Wait for Import
The app will:
1. Download the backup from GCS
2. Clear all local data
3. Import the backup data
4. Show success message

**Note**: This may take a few seconds depending on data size.

---

## Understanding Backup Files

### master_data.json
- **What**: Latest approved data
- **When**: Updated every time master approves changes
- **Badge**: Shows "LATEST"
- **Use**: For most up-to-date data

### master_data_1234567890.json
- **What**: Versioned backup (timestamp in filename)
- **When**: Created automatically on every approval
- **Badge**: None
- **Use**: For restoring from specific point in time

### Reading Timestamps
Timestamps are in Unix epoch format (milliseconds):
- `1709568123456` = Mar 04, 2024 at 14:15:23

The app automatically formats these as:
```
Updated: Mar 04, 2024 14:15
```

---

## Differences from "Pull Latest Data"

| Feature | Pull Latest Data | Import from Bucket |
|---------|------------------|-------------------|
| **When to use** | Regular sync | Restore/Recovery |
| **Checks if newer** | Yes, only syncs if newer | No, always imports |
| **Choose version** | No, always latest | Yes, select any backup |
| **Speed** | Fast (skips if up-to-date) | Slower (always downloads) |
| **Data replacement** | Only if newer | Always replaces |
| **Use case** | Daily sync | Initial setup, recovery |

**Recommendation**: Use "Pull Latest Data" for regular syncing, use "Import from Bucket" when you need to restore from a specific backup.

---

## Safety Features

### Warning Dialog
Before importing, you'll see:
```
⚠️ This will replace ALL local data!
```

This ensures you don't accidentally lose local changes.

### Confirmation Required
You must explicitly tap "Import" to proceed. Tapping anywhere else cancels the import.

### Progress Feedback
Toast messages keep you informed:
- "Downloading from bucket..."
- "Import successful!"
- "Import failed: [error message]"

---

## Troubleshooting

### No Backups Found
**Problem**: "No backup files found in bucket"

**Solutions**:
1. Make sure master device has submitted data
2. Verify bucket name is correct (`frog_life`)
3. Check GCS credentials are valid
4. Confirm service account has read permissions

### Import Failed
**Problem**: "Import failed: [error message]"

**Solutions**:
1. Check internet connection
2. Verify file still exists in bucket
3. Confirm service account permissions
4. Try downloading a different backup

### "Downloading from bucket..." Hangs
**Problem**: Import seems stuck

**Solutions**:
1. Check internet connection speed
2. Large files may take longer (wait a bit)
3. Force close and retry
4. Check if bucket is accessible via gsutil CLI

---

## Best Practices

### 1. Export Before Import
Before importing, export your current data as a backup:
```
Settings → Export Data to JSON
```

This gives you a local backup in case something goes wrong.

### 2. Use "LATEST" for Regular Sync
For day-to-day syncing, always import the "LATEST" backup (master_data.json).

### 3. Keep Versioned Backups
Don't delete old backups from GCS. They're useful for:
- Recovering from mistakes
- Auditing changes over time
- Rolling back problematic updates

### 4. Verify After Import
After importing, quickly check:
- Frogs are correct
- Recent activities are present
- Reward history is accurate

### 5. Initial Setup Flow
For new devices:
```
1. Install app
2. Settings → Device Sync → Select device type
3. Import from Bucket → Select "LATEST"
4. Verify data looks correct
5. Start using the app
```

---

## Examples

### Example 1: Setting Up Second Device
```
Scenario: You have Frog Life on your phone, now want to add it to your tablet.

Steps:
1. Install Frog Life on tablet
2. Copy frog-sync-key.json to tablet's assets folder
3. Build and install
4. Settings → Device Sync → Set as "Slave Device"
5. Import from Bucket → Select "master_data" (LATEST)
6. Wait for import
7. Done! Tablet now has same data as phone
```

### Example 2: Recovering Deleted Frog
```
Scenario: Accidentally deleted a frog on master device. Want to restore.

Steps:
1. Check when frog was deleted (e.g., today at 2 PM)
2. Import from Bucket
3. Look for backup from before 2 PM (e.g., "master_data_1709561234567" from 1 PM)
4. Select that backup
5. Confirm import
6. Deleted frog is restored!
```

### Example 3: Undoing Bad Changes
```
Scenario: Made many changes yesterday that you want to undo.

Steps:
1. Export current data (safety backup)
2. Import from Bucket
3. Select backup from before yesterday
4. Confirm import
5. Changes are undone, data reverted to that point
```

---

## Technical Details

### How It Works
1. App connects to GCS bucket using service account credentials
2. Lists all files in `approved/` folder using `storage.list()`
3. Retrieves metadata (name, timestamp, size) for each file
4. Displays in UI sorted by timestamp (newest first)
5. On selection, downloads file content using `storage.get()`
6. Parses JSON and imports using existing import logic
7. Updates local database with imported data

### Network Usage
- **List backups**: ~1-2 KB (one-time per screen open)
- **Download backup**: Varies by data size (typically 10-100 KB)
- **Total**: Minimal, well within free tier

### Storage Impact
Importing does NOT duplicate data:
- Old data is cleared before import
- New data replaces it completely
- App size remains constant

---

## FAQ

**Q: Will this delete data from the bucket?**
A: No, import only reads from the bucket. Your backups remain safe.

**Q: Can I import on multiple devices simultaneously?**
A: Yes, each device can import independently without affecting others.

**Q: How many backups are kept?**
A: All backups are kept unless you manually delete them from GCS.

**Q: Can I delete old backups?**
A: Yes, use gsutil or GCS Console to delete old versioned backups if needed.

**Q: What if I import while offline?**
A: Import requires internet connection. It will fail if offline.

**Q: Can I import non-approved data?**
A: No, only approved data from `approved/` folder is shown.

**Q: Is there a size limit?**
A: GCS supports up to 5 TB files, but typical Frog Life data is < 1 MB.

**Q: Can I schedule automatic imports?**
A: Not currently. Use "Pull Latest Data" for automatic sync checking.

---

## Summary

**Import from Bucket** is your safety net:
- ✅ Restore from any backup version
- ✅ Set up new devices easily
- ✅ Recover from mistakes
- ✅ Roll back problematic changes
- ✅ Safe with confirmation dialogs
- ✅ Fast and reliable

**Use it for**: Initial setup, data recovery, version rollback
**Don't use it for**: Regular syncing (use "Pull Latest Data" instead)

---

Happy syncing! 🐸
