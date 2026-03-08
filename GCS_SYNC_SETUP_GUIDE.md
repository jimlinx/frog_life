# Google Cloud Storage Sync - Setup Guide

## Overview
This guide walks you through setting up Google Cloud Storage (GCS) sync for Frog Life to enable multi-device synchronization with a master-slave approval workflow.

---

## Prerequisites
- Google Cloud account
- GCS bucket named `frog_life` (already created)
- Android Studio with Frog Life project

---

## Step 1: Create Service Account

### Option A: Using Google Cloud Console

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project (or create one)
3. Navigate to **IAM & Admin → Service Accounts**
4. Click **Create Service Account**
   - Name: `frog-sync`
   - Description: `Service account for Frog Life sync`
   - Click **Create and Continue**
5. Grant permissions:
   - Role: **Storage Object Admin** (for full read/write access to bucket)
   - Click **Continue**
6. Click **Done**

### Option B: Using gcloud CLI

```bash
# Create service account
gcloud iam service-accounts create frog-sync \
    --display-name="Frog Life Sync"

# Grant bucket access (replace PROJECT_ID with your project ID)
gsutil iam ch serviceAccount:frog-sync@PROJECT_ID.iam.gserviceaccount.com:objectAdmin \
    gs://frog_life
```

---

## Step 2: Generate and Download Key

### Using Google Cloud Console

1. In **Service Accounts**, find `frog-sync`
2. Click the three dots (⋮) → **Manage keys**
3. Click **Add Key → Create new key**
4. Select **JSON** format
5. Click **Create**
6. Save the downloaded file as `frog-sync-key.json`

### Using gcloud CLI

```bash
gcloud iam service-accounts keys create frog-sync-key.json \
    --iam-account=frog-sync@PROJECT_ID.iam.gserviceaccount.com
```

---

## Step 3: Add Key to Android Project

1. In Android Studio, navigate to your project
2. Create the `assets` folder if it doesn't exist:
   - Right-click `app/src/main` → New → Folder → Assets Folder
3. Copy `frog-sync-key.json` to `app/src/main/assets/`
4. **IMPORTANT**: Add to `.gitignore` to prevent committing credentials:

```bash
echo "app/src/main/assets/frog-sync-key.json" >> .gitignore
```

---

## Step 4: Verify Bucket Structure

The GCS bucket `frog_life` will automatically create the following structure when you use the app:

```
frog_life/
├── devices/              # Device registrations
├── pending/              # Changes awaiting approval from slave devices
├── approved/             # Approved master data (official backups)
├── rejected/             # Rejected changes (for audit trail)
└── manual_backups/       # Manual exports from slave devices
```

You can verify the bucket exists:

```bash
gsutil ls gs://frog_life
```

---

## Step 5: Configure CORS (Optional but Recommended)

If you encounter CORS errors, configure CORS for the bucket:

**cors.json:**
```json
[
  {
    "origin": ["*"],
    "method": ["GET", "POST", "PUT", "DELETE"],
    "responseHeader": ["Content-Type"],
    "maxAgeSeconds": 3600
  }
]
```

Apply CORS configuration:
```bash
gsutil cors set cors.json gs://frog_life
```

---

## Step 6: Build and Install App

1. Sync Gradle files in Android Studio
2. Build the app (Build → Make Project)
3. Install on your devices

---

## How to Use Sync

### Initial Setup on Each Device

1. Open **Settings** in Frog Life
2. Scroll to **Device Sync** section
3. Select device type:
   - **Master Device**: One device that approves all changes
   - **Slave Device**: Devices that submit changes for approval

### Master Device Workflow

1. Make changes directly on master device (they're auto-approved)
2. Check **Review Pending Changes** to see submissions from slave devices
3. Review each change:
   - See what frogs, activities, and rewards were modified
   - Click **Approve** to merge and publish to all devices
   - Click **Reject** to deny the change
4. Other devices can pull approved changes using **Pull Latest Data**

### Slave Device Workflow

1. Make changes locally (tracked activities, redemptions, etc.)
2. Click **Submit Changes for Approval**
3. Wait for master device to approve
4. Click **Pull Latest Data** to sync approved changes

### Syncing Across All Devices

- After master approves changes, all devices (including slaves) should click **Pull Latest Data**
- This downloads the latest merged data from the cloud
- All devices will be in sync

### Export to Bucket

The **Export to Bucket** feature allows you to manually backup your current data to GCS:

**Master Device Export:**
1. Go to **Settings → Data Management**
2. Click **Export to Bucket**
3. Data is uploaded to `approved/master_data.json` (becomes new official master data)
4. Versioned backup created automatically (e.g., `approved/master_data_1234567890.json`)
5. All devices can now pull this new data

**Slave Device Export:**
1. Go to **Settings → Data Management**
2. Click **Export to Bucket**
3. Data is uploaded to `manual_backups/` folder with timestamp and device ID
4. Personal backup that doesn't affect master data
5. Can be imported later if needed

**When to Use:**
- **Manual Backup**: Before making risky changes
- **Initial Setup**: Master device creating first bucket data
- **Personal Backup**: Slave device wants independent backup
- **Force Sync**: Master manually pushing current state to bucket

### Import from Bucket (Advanced)

The **Import from Bucket** feature allows you to browse and restore from any available backup:

**Use Cases:**
- **Initial Setup**: New device can import existing data from bucket
- **Restore from Backup**: Recover data from a previous version
- **Data Recovery**: Restore after accidental deletion

**How to Use:**
1. Go to **Settings → Device Sync**
2. Click **Import from Bucket**
3. Browse available backups (sorted by date, newest first)
   - **Approved Backups:**
     - `master_data` - Latest approved data (marked with "LATEST" badge)
     - `master_data_1234567890` - Versioned backups with timestamps
   - **Manual Backups:**
     - `1234567890_abc12345` - Manual exports from slave devices (marked with "MANUAL" badge)
4. Select a backup to restore
5. Confirm the import (⚠️ This replaces ALL local data)
6. Wait for download and import to complete

**Tips:**
- Each backup shows timestamp, file size, and source (LATEST/MANUAL)
- Versioned backups are created every time master approves changes
- Manual backups are created when users click "Export to Bucket"
- You can restore from any point in time
- Use "LATEST" for most up-to-date approved data
- Use "MANUAL" backups to restore your personal backup
- Use older versions to restore from a specific point in time

**Difference from "Pull Latest Data":**
- **Pull Latest Data**: Quick sync, only downloads if data is newer
- **Import from Bucket**: Full restore, choose which backup to import, always replaces local data

---

## Troubleshooting

### "403 Forbidden" Error

**Cause**: Service account doesn't have permission to access bucket

**Fix**:
```bash
gsutil iam ch serviceAccount:frog-sync@PROJECT_ID.iam.gserviceaccount.com:objectAdmin gs://frog_life
```

### "File not found" Error

**Cause**: First time syncing, no master data exists yet

**Fix**:
- On master device, make a small change and it will auto-create `approved/master_data.json`
- OR manually export data and the master will create the initial approved data

### "Credentials not found" Error

**Cause**: `frog-sync-key.json` not in `app/src/main/assets/`

**Fix**:
- Verify file exists in correct location
- Rebuild project
- Clean and rebuild: Build → Clean Project, then Build → Rebuild Project

### Sync Service Not Appearing

**Cause**: Credentials file missing or invalid

**Check**:
1. File exists at `app/src/main/assets/frog-sync-key.json`
2. File is valid JSON
3. Service account has correct permissions
4. Rebuild the app

---

## Security Best Practices

### ✅ Do This:
- Keep `frog-sync-key.json` in `.gitignore`
- Use service account with minimal permissions (Storage Object Admin on single bucket)
- Regularly rotate service account keys
- Monitor GCS access logs

### ❌ Don't Do This:
- Don't commit `frog-sync-key.json` to git
- Don't share the key file
- Don't use personal credentials
- Don't grant broader permissions than needed

---

## Cost Monitoring

### Free Tier Limits (GCS)
- **Storage**: 5GB/month
- **Class A operations (writes)**: 5,000/month
- **Class B operations (reads)**: 50,000/month
- **Network egress**: 1GB/month

### Expected Usage (Family of 4 devices)
- **Storage**: < 10MB (~0.2% of free tier)
- **Reads**: ~2,880/month (5.8% of free tier)
- **Writes**: ~100/month (2% of free tier)
- **Total cost**: $0/month ✅

### Monitor Usage
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Navigate to **Storage → Buckets → frog_life**
3. Click **Monitoring** tab
4. View storage usage and operations

### Set Up Billing Alerts
1. Go to **Billing → Budgets & alerts**
2. Click **Create budget**
3. Set budget: $1/month
4. Alert thresholds: 50%, 90%, 100%

---

## Advanced Configuration

### Enable Auto-Sync (Coming Soon)

The app will support automatic hourly sync using WorkManager. This will:
- Check for new approved data every hour
- Auto-download and merge changes
- Work even when app is closed
- Respect battery optimization settings

To enable (once implemented):
1. Go to Settings → Device Sync
2. Toggle "Auto-sync every hour"

### Manual Backup

You can manually backup the GCS data:

```bash
# Download entire bucket
gsutil -m cp -r gs://frog_life ./frog_life_backup

# Download just approved data
gsutil cp gs://frog_life/approved/master_data.json ./backup.json
```

### Restore from Backup

```bash
# Upload backup to approved folder
gsutil cp ./backup.json gs://frog_life/approved/master_data.json
```

---

## FAQ

**Q: What happens if two devices submit changes at the same time?**
A: Master reviews them sequentially. Each approval merges with the current state, so both changes are preserved.

**Q: Can I have multiple master devices?**
A: Not recommended. Stick to one master to avoid approval conflicts. If needed, you can change which device is master anytime.

**Q: What if I lose my master device?**
A: Simply designate a different device as master in Settings. The data is safe in GCS.

**Q: Can I use this without internet?**
A: Local changes work offline. Sync requires internet connection.

**Q: Is my data secure?**
A: Data is stored in your private GCS bucket with service account authentication. Only devices with the key file can access it.

**Q: Can I self-host this?**
A: GCS is already cloud-hosted. For complete self-hosting, consider the PocketBase option in `SYNC_IMPLEMENTATION_OPTIONS.md`.

---

## Support

If you encounter issues:

1. Check [Troubleshooting](#troubleshooting) section above
2. Verify service account permissions
3. Check GCS bucket exists and is accessible
4. Review Android Studio logcat for error messages
5. Test with `gsutil` CLI to verify bucket access

---

## Next Steps

1. ✅ Set up service account
2. ✅ Download key file
3. ✅ Add key to app assets
4. ✅ Build and install app
5. ✅ Configure master device
6. ✅ Test sync with another device
7. 🔜 Enable auto-sync (future feature)

Enjoy seamless multi-device sync with Frog Life! 🐸
