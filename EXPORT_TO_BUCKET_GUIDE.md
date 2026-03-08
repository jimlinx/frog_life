# Export to Bucket - Quick Guide

## Overview
The **Export to Bucket** feature allows you to manually backup your current data to Google Cloud Storage with a single click.

---

## When to Use

### 1. **Manual Backup**
Create a snapshot of your current data before making risky changes.

```
Before risky operation → Export to Bucket → Make changes → Restore if needed
```

### 2. **Initial Setup (Master Device)**
Master device creating the first official backup in the bucket.

```
Fresh install → Add some frogs → Export to Bucket → Other devices can now import
```

### 3. **Personal Backup (Slave Device)**
Slave device wants an independent backup without affecting master data.

```
Slave makes local changes → Export to Bucket → Personal backup saved
```

### 4. **Force Sync (Master Device)**
Master device wants to push current state as new official data.

```
Master has latest data → Export to Bucket → New master data available
```

---

## How to Use

### Step 1: Open Settings
1. Open **Frog Life** app
2. Navigate to **Settings**
3. Scroll to **Data Management** section

### Step 2: Click Export to Bucket
1. Tap **Export to Bucket** button
2. See "Exporting to bucket..." toast message
3. Wait a few seconds

### Step 3: Success!
You'll see a confirmation message:
```
Exported to: approved/master_data.json
```
or
```
Exported to: manual_backups/1234567890_abc12345.json
```

---

## Behavior by Device Type

### Master Device Export

**What Happens:**
1. Current data is collected (frogs, activities, logs, rewards, etc.)
2. Uploaded to `approved/master_data.json`
3. Replaces existing master data (becomes new official version)
4. Versioned backup created automatically (e.g., `approved/master_data_1709568123.json`)
5. All devices can now pull this new data

**File Structure:**
```
approved/
├── master_data.json                    ← Your new export (LATEST)
├── master_data_1709568123.json         ← Versioned backup (auto-created)
└── master_data_1709554567.json         ← Previous version
```

**Use Cases:**
- Creating initial bucket data
- Pushing current state as new master
- Manual backup before risky operation
- Force all devices to sync to your current state

### Slave Device Export

**What Happens:**
1. Current data is collected
2. Uploaded to `manual_backups/{timestamp}_{deviceId}.json`
3. Does NOT affect master data
4. Personal backup that only you can import
5. Other devices won't see this backup (unless they browse manual_backups)

**File Structure:**
```
manual_backups/
├── 1709568123_abc12345.json            ← Your export
├── 1709554567_abc12345.json            ← Your previous export
└── 1709541234_xyz67890.json            ← Another slave's export
```

**Use Cases:**
- Personal backup before making changes
- Independent snapshot of your local data
- Backup before submitting for approval
- Restore point if you mess something up locally

---

## Differences from Other Export Methods

| Feature | Export to Bucket | Export to JSON | Submit for Approval |
|---------|------------------|----------------|---------------------|
| **Destination** | GCS bucket | Local file | GCS pending/ |
| **Master behavior** | Creates new master data | Saves locally | N/A (master auto-approves) |
| **Slave behavior** | Personal backup | Saves locally | Awaits approval |
| **Affects other devices** | Master: Yes, Slave: No | No | Yes (if approved) |
| **Creates version** | Master: Yes, Slave: No | No | No |
| **Requires approval** | No | No | Yes (for slaves) |

**Recommendation:**
- **Export to Bucket**: Manual backup to cloud, master can update official data
- **Export to JSON**: Local file backup, for offline storage
- **Submit for Approval**: Normal workflow for slave devices to sync changes

---

## Examples

### Example 1: Master Creating Initial Backup
```
Scenario: Fresh Frog Life installation on master device. Want to set up cloud sync.

Steps:
1. Add frogs and activities
2. Settings → Export to Bucket
3. Wait for success message
4. Other devices can now "Import from Bucket" and get your data
```

### Example 2: Slave Personal Backup
```
Scenario: Slave device wants backup before making risky changes.

Steps:
1. Settings → Export to Bucket
2. See: "Exported to: manual_backups/1709568123_abc12345.json"
3. Make risky changes
4. If something goes wrong: Import from Bucket → Select your manual backup
5. Data restored to pre-change state
```

### Example 3: Master Force Sync
```
Scenario: Master has updated data locally but hasn't gone through approval workflow. Want to push as new official data.

Steps:
1. Master makes changes locally (frogs, activities, etc.)
2. Settings → Export to Bucket
3. See: "Exported to: approved/master_data.json"
4. All devices can now "Pull Latest Data" to sync
```

---

## What Gets Exported

The export includes ALL your Frog Life data:

✅ **Included:**
- All frogs (names, points, status, etc.)
- All activities (definitions and settings)
- All activity logs (tracked activities)
- All rewards (definitions and costs)
- All reward redemptions (redemption history)
- All special dates
- All day comments
- App settings (thresholds, etc.)
- Frog-activity assignments

❌ **Not Included:**
- Device-specific settings (device type, device ID)
- Sync metadata (last sync time)
- App preferences (UI settings)

---

## Safety Features

### No Accidental Overwrites
- Slave exports go to separate folder (manual_backups/)
- Master exports create versioned backups automatically
- Previous versions remain accessible

### Confirmation Feedback
Toast messages confirm:
- Export started: "Exporting to bucket..."
- Export succeeded: "Exported to: [filename]"
- Export failed: "Export failed: [error message]"

### Error Handling
If export fails:
- Local data is NOT affected
- Error message explains what went wrong
- You can retry immediately

---

## Troubleshooting

### "Export failed: Credentials not found"
**Problem**: GCS credentials missing

**Solution**:
1. Verify `frog-sync-key.json` exists in `app/src/main/assets/`
2. Rebuild the app
3. Retry export

### "Export failed: 403 Forbidden"
**Problem**: Service account lacks write permissions

**Solution**:
```bash
gsutil iam ch serviceAccount:frog-sync@PROJECT_ID.iam.gserviceaccount.com:objectAdmin gs://frog_life
```

### "Export failed: Network error"
**Problem**: No internet connection

**Solution**:
1. Check internet connection
2. Retry when connected

### Export Takes Forever
**Problem**: Large data or slow connection

**Solution**:
- Wait a bit longer (typical exports < 5 seconds)
- Check internet speed
- Large datasets (100+ frogs) may take 10-20 seconds

---

## Best Practices

### 1. Export Before Risky Operations
```
Before bulk delete → Export to Bucket → Perform delete → Safe!
```

### 2. Master: Export After Major Changes
```
Added 10 new frogs → Export to Bucket → Other devices can sync
```

### 3. Slave: Personal Backup Before Submission
```
Make changes → Export to Bucket (personal backup) → Submit for Approval
```

### 4. Regular Backups
Create periodic backups:
- Master: Weekly export keeps official data fresh
- Slave: Before each submission for rollback capability

### 5. Verify After Export
After exporting:
1. Check "Import from Bucket" to verify file appears
2. Verify timestamp is recent
3. For master: Check "LATEST" badge appears

---

## Advanced Usage

### Managing Old Manual Backups

Slave devices create many manual backups over time. To clean up:

**Option 1: Via GCS Console**
1. Go to console.cloud.google.com
2. Storage → Buckets → frog_life → manual_backups/
3. Delete old files manually

**Option 2: Via gsutil**
```bash
# List your manual backups
gsutil ls gs://frog_life/manual_backups/

# Delete specific backup
gsutil rm gs://frog_life/manual_backups/1709541234_abc12345.json

# Delete all backups older than 30 days (use with caution!)
gsutil -m rm gs://frog_life/manual_backups/$(gsutil ls -l gs://frog_life/manual_backups/ | awk '$2 < systime() - 2592000 {print $3}')
```

### Master Replacing Official Data

To replace master data with slave's backup:

1. Slave exports to bucket (creates manual backup)
2. Slave tells filename to master
3. Master: Import from Bucket → Select slave's manual backup
4. Master: Export to Bucket (now becomes new official master data)
5. All devices: Pull Latest Data

---

## FAQ

**Q: Does export replace local data?**
A: No, export only uploads to bucket. Local data unchanged.

**Q: Can I export without internet?**
A: No, export requires internet connection to upload to GCS.

**Q: How much does each export cost?**
A: Very cheap! ~$0.00001 per export (1 write operation). Essentially free.

**Q: Can multiple devices export simultaneously?**
A: Yes, but for master devices, last export wins (becomes new master_data.json).

**Q: Do I need to export manually every time?**
A: No. Master's approved changes auto-export. Manual export is for specific situations.

**Q: Can I schedule automatic exports?**
A: Not currently. Use manual export when needed.

**Q: What if I export by mistake?**
A: Master: Previous version still available as versioned backup. Slave: Just creates extra file, no harm.

**Q: How do I know export succeeded?**
A: Toast message shows: "Exported to: [filename]"

**Q: Can I export to different bucket?**
A: No, bucket name is configured in code (`frog_life`).

---

## Summary

**Export to Bucket** is your quick backup tool:

✅ **Master Device:**
- Creates new official master data
- Auto-creates versioned backup
- All devices can pull new data
- Use for: Initial setup, force sync, manual backup

✅ **Slave Device:**
- Creates personal backup
- Doesn't affect master data
- Only you can import it
- Use for: Personal backup, before risky changes

✅ **For Everyone:**
- One-click operation
- Instant cloud backup
- No local data affected
- Restore anytime via "Import from Bucket"

**Use it whenever you want a cloud backup!** 🐸☁️
