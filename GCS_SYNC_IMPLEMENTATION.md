# Google Cloud Storage Sync Implementation

## Overview
Use Google Cloud Storage bucket for master-slave device synchronization. This is **simpler** than Firebase and leverages your existing export/import functionality.

---

## ✅ Why Google Cloud Storage?

### Advantages
- ✅ **Simple:** Just file uploads/downloads
- ✅ **Free tier:** 5GB storage, 5K writes, 50K reads/month
- ✅ **Uses existing code:** Extends current export/import
- ✅ **No database setup:** Plain JSON files
- ✅ **Easy to debug:** Can view files in console
- ✅ **Already have access:** You mentioned having a bucket

### Free Tier Details
- **Storage:** 5GB (plenty for JSON - even 1000 frogs = ~1MB)
- **Class A ops (writes):** 5,000/month = ~166/day
- **Class B ops (reads):** 50,000/month = ~1,666/day
- **Network egress:** 1GB/month
- **Cost if exceeded:** ~$0.026/GB storage, $0.05/10K operations

**For family of 4 devices checking hourly:**
- Reads: 4 devices × 24 checks/day × 30 days = 2,880/month ✅
- Writes: ~100/month for changes ✅
- **Total cost:** $0/month (well within free tier)

---

## Architecture

```
┌─────────────┐
│ Slave Device│ ──┐
└─────────────┘   │
                  │  Upload change JSON
┌─────────────┐   │  to pending/
│ Slave Device│ ──┤
└─────────────┘   │
                  ▼
            ┌──────────────────────┐
            │  GCS Bucket          │
            │  ├─ devices/         │
            │  ├─ pending/         │ ◄──── Master lists pending changes
            │  ├─ approved/        │
            │  └─ rejected/        │
            └──────────────────────┘
                  │
                  │  Master approves & uploads
                  │  merged data to approved/
                  ▼
            ┌──────────────────────┐
            │ approved/            │
            │  └─ master_data.json │
            └──────────────────────┘
                  │
                  │  All devices download
                  ▼
    ┌─────────────────────────────────┐
    │  All Devices Import & Sync      │
    └─────────────────────────────────┘
```

---

## Bucket Structure

```
your-frog-sync-bucket/
├── devices/
│   ├── device_abc123.json          # Device registration
│   └── device_xyz789.json
│
├── pending/                         # Changes awaiting approval
│   ├── 2024-03-04_123456_abc123.json
│   ├── 2024-03-04_123789_xyz789.json
│   └── ...
│
├── approved/                        # Approved master data
│   ├── master_data.json            # Latest approved data
│   └── master_data_v2.json         # Versioned backups
│
├── rejected/                        # Rejected changes (for audit)
│   └── 2024-03-04_123456_abc123.json
│
└── metadata/
    ├── sync_status.json            # Last sync timestamps
    └── version.json                # Current data version
```

---

## Setup Guide

### Step 1: Create GCS Bucket

**Google Cloud Console:**
```
1. Go to console.cloud.google.com
2. Storage → Buckets → CREATE
3. Name: frog-life-sync (globally unique)
4. Location: Choose nearest region
5. Storage class: Standard
6. Access control: Fine-grained
7. CREATE
```

**Set Permissions:**
```
Bucket → Permissions → ADD PRINCIPAL
- Principal: allUsers (or specific service account)
- Role: Storage Object Viewer (read) + Storage Object Creator (write)
OR better: Create service account with limited access
```

### Step 2: Create Service Account

```bash
# Create service account
gcloud iam service-accounts create frog-sync \
    --display-name="Frog Life Sync"

# Grant bucket access
gsutil iam ch serviceAccount:frog-sync@PROJECT_ID.iam.gserviceaccount.com:objectAdmin \
    gs://frog-life-sync

# Create key file
gcloud iam service-accounts keys create frog-sync-key.json \
    --iam-account=frog-sync@PROJECT_ID.iam.gserviceaccount.com
```

**Important:** Download `frog-sync-key.json` - this authenticates your app

### Step 3: Add to Android Project

**build.gradle.kts (app level):**
```kotlin
dependencies {
    // Google Cloud Storage
    implementation("com.google.cloud:google-cloud-storage:2.30.0")

    // Or simpler: Use Google Drive API (lighter weight)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
}
```

---

## Implementation

### 1. Device Registration

**Device.kt**
```kotlin
data class Device(
    val deviceId: String = UUID.randomUUID().toString(),
    val name: String,
    val type: DeviceType,
    val lastSeen: Long = System.currentTimeMillis()
)

enum class DeviceType {
    MASTER, SLAVE
}
```

**DeviceManager.kt**
```kotlin
class DeviceManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)

    fun getDeviceId(): String {
        return prefs.getString("device_id", null) ?: run {
            val newId = UUID.randomUUID().toString()
            prefs.edit().putString("device_id", newId).apply()
            newId
        }
    }

    fun getDeviceType(): DeviceType {
        val type = prefs.getString("device_type", "SLAVE")
        return DeviceType.valueOf(type!!)
    }

    fun setDeviceType(type: DeviceType) {
        prefs.edit().putString("device_type", type.name).apply()
    }
}
```

### 2. GCS Sync Service

**GCSSyncService.kt**
```kotlin
class GCSSyncService(
    private val context: Context,
    private val repository: FrogRepository
) {
    private val bucketName = "frog-life-sync"
    private val deviceManager = DeviceManager(context)

    // Initialize GCS client
    private val storage: Storage by lazy {
        val credentials = ServiceAccountCredentials.fromStream(
            context.assets.open("frog-sync-key.json")
        )
        StorageOptions.newBuilder()
            .setCredentials(credentials)
            .build()
            .service
    }

    // Submit change from slave device
    suspend fun submitPendingChange(exportData: ExportData) = withContext(Dispatchers.IO) {
        if (deviceManager.getDeviceType() == DeviceType.MASTER) {
            // Master approves own changes immediately
            uploadApprovedData(exportData)
        } else {
            // Slave submits for approval
            val fileName = "pending/${System.currentTimeMillis()}_${deviceManager.getDeviceId()}.json"
            val json = Gson().toJson(exportData)

            val blobId = BlobId.of(bucketName, fileName)
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/json")
                .build()

            storage.create(blobInfo, json.toByteArray())
        }
    }

    // List pending changes (master only)
    suspend fun listPendingChanges(): List<PendingChange> = withContext(Dispatchers.IO) {
        val blobs = storage.list(
            bucketName,
            Storage.BlobListOption.prefix("pending/")
        )

        blobs.iterateAll().map { blob ->
            val json = String(blob.getContent())
            val data = Gson().fromJson(json, ExportData::class.java)
            PendingChange(
                fileName = blob.name,
                deviceId = blob.name.split("_").last().removeSuffix(".json"),
                timestamp = blob.name.split("_")[0].split("/")[1].toLong(),
                data = data
            )
        }
    }

    // Approve and publish (master only)
    suspend fun approveChange(pendingChange: PendingChange) = withContext(Dispatchers.IO) {
        // Get current master data
        val currentData = downloadApprovedData()

        // Merge changes
        val mergedData = mergeData(currentData, pendingChange.data)

        // Upload merged data
        uploadApprovedData(mergedData)

        // Move pending to rejected folder
        moveToApproved(pendingChange.fileName)
    }

    // Reject change (master only)
    suspend fun rejectChange(pendingChange: PendingChange) = withContext(Dispatchers.IO) {
        moveToRejected(pendingChange.fileName)
    }

    // Download latest approved data
    suspend fun downloadApprovedData(): ExportData? = withContext(Dispatchers.IO) {
        try {
            val blob = storage.get(BlobId.of(bucketName, "approved/master_data.json"))
            val json = String(blob.getContent())
            Gson().fromJson(json, ExportData::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Upload approved data (master only)
    private suspend fun uploadApprovedData(data: ExportData) = withContext(Dispatchers.IO) {
        val json = Gson().toJson(data)

        // Upload current version
        val blobId = BlobId.of(bucketName, "approved/master_data.json")
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("application/json")
            .build()
        storage.create(blobInfo, json.toByteArray())

        // Create versioned backup
        val versionedId = BlobId.of(
            bucketName,
            "approved/master_data_${System.currentTimeMillis()}.json"
        )
        val versionedInfo = BlobInfo.newBuilder(versionedId)
            .setContentType("application/json")
            .build()
        storage.create(versionedInfo, json.toByteArray())

        // Update metadata
        updateSyncMetadata()
    }

    // Check if new data available
    suspend fun hasNewData(): Boolean = withContext(Dispatchers.IO) {
        try {
            val blob = storage.get(BlobId.of(bucketName, "approved/master_data.json"))
            val remoteModified = blob.updateTime

            val lastSync = getLastSyncTime()
            remoteModified > lastSync
        } catch (e: Exception) {
            false
        }
    }

    // Merge data logic
    private fun mergeData(current: ExportData?, new: ExportData): ExportData {
        if (current == null) return new

        // Merge frogs (newer wins based on timestamp)
        val mergedFrogs = (current.frogs + new.frogs)
            .distinctBy { it.id }

        // Merge activities
        val mergedActivities = (current.activities + new.activities)
            .distinctBy { it.id }

        // Merge logs (append)
        val mergedLogs = (current.activityLogs + new.activityLogs)
            .distinctBy { "${it.frogId}_${it.activityId}_${it.date}" }

        // Merge rewards
        val mergedRewards = (current.rewards + new.rewards)
            .distinctBy { it.id }

        // Merge redemptions
        val mergedRedemptions = (current.rewardRedemptions + new.rewardRedemptions)
            .distinctBy { it.id }

        return ExportData(
            frogs = mergedFrogs,
            activities = mergedActivities,
            frogActivityRefs = (current.frogActivityRefs + new.frogActivityRefs).distinct(),
            activityLogs = mergedLogs,
            specialDates = (current.specialDates + new.specialDates).distinctBy { it.id },
            dayComments = (current.dayComments + new.dayComments).distinctBy { it.id },
            rewards = mergedRewards,
            rewardRedemptions = mergedRedemptions,
            settings = new.settings ?: current.settings
        )
    }

    // Helper: Move file
    private fun moveToApproved(fileName: String) {
        val source = BlobId.of(bucketName, fileName)
        val dest = BlobId.of(bucketName, fileName.replace("pending/", "approved/"))
        storage.copy(Storage.CopyRequest.of(source, dest))
        storage.delete(source)
    }

    private fun moveToRejected(fileName: String) {
        val source = BlobId.of(bucketName, fileName)
        val dest = BlobId.of(bucketName, fileName.replace("pending/", "rejected/"))
        storage.copy(Storage.CopyRequest.of(source, dest))
        storage.delete(source)
    }

    // Metadata helpers
    private fun getLastSyncTime(): Long {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("last_sync", 0)
    }

    private fun updateSyncMetadata() {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_sync", System.currentTimeMillis()).apply()
    }
}

data class PendingChange(
    val fileName: String,
    val deviceId: String,
    val timestamp: Long,
    val data: ExportData
)
```

### 3. UI - Settings Integration

**Add to SettingsScreen.kt:**

```kotlin
// Add sync section
Spacer(modifier = Modifier.height(16.dp))

Text("Device Sync", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

// Device type selection
var isDeviceTypeMaster by remember { mutableStateOf(deviceManager.getDeviceType() == DeviceType.MASTER) }

Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
) {
    FilterChip(
        selected = isDeviceTypeMaster,
        onClick = {
            isDeviceTypeMaster = true
            deviceManager.setDeviceType(DeviceType.MASTER)
        },
        label = { Text("Master Device") },
        modifier = Modifier.weight(1f)
    )
    FilterChip(
        selected = !isDeviceTypeMaster,
        onClick = {
            isDeviceTypeMaster = false
            deviceManager.setDeviceType(DeviceType.SLAVE)
        },
        label = { Text("Slave Device") },
        modifier = Modifier.weight(1f)
    )
}

// Submit changes (slave)
if (!isDeviceTypeMaster) {
    Button(
        onClick = {
            scope.launch {
                val exportData = collectExportData() // Use existing export logic
                syncService.submitPendingChange(exportData)
                Toast.makeText(context, "Changes submitted for approval", Toast.LENGTH_SHORT).show()
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Submit Changes for Approval")
    }
}

// Review pending (master)
if (isDeviceTypeMaster) {
    val pendingCount by syncService.pendingChangesCount.collectAsState()

    Button(
        onClick = { navController.navigate("pending_approvals") },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Review Pending Changes ($pendingCount)")
    }
}

// Sync from master
Button(
    onClick = {
        scope.launch {
            if (syncService.hasNewData()) {
                val data = syncService.downloadApprovedData()
                data?.let {
                    importData(context, frogViewModel, activityViewModel, rewardViewModel, it)
                    Toast.makeText(context, "Synced from master!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Already up to date", Toast.LENGTH_SHORT).show()
            }
        }
    },
    modifier = Modifier.fillMaxWidth()
) {
    Text("Pull Latest Data")
}

// Auto-sync toggle
var autoSync by remember { mutableStateOf(false) }
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text("Auto-sync every hour")
    Switch(
        checked = autoSync,
        onCheckedChange = { autoSync = it }
    )
}
```

### 4. Pending Approvals Screen (Master Only)

**PendingApprovalsScreen.kt:**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalsScreen(
    navController: NavController,
    syncService: GCSSyncService
) {
    val pendingChanges by syncService.getPendingChanges().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Approvals") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pendingChanges) { change ->
                PendingChangeCard(
                    change = change,
                    onApprove = { syncService.approveChange(change) },
                    onReject = { syncService.rejectChange(change) }
                )
            }
        }
    }
}

@Composable
fun PendingChangeCard(
    change: PendingChange,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Device: ${change.deviceId}", fontWeight = FontWeight.Bold)
            Text("Time: ${SimpleDateFormat("MMM dd, HH:mm").format(Date(change.timestamp))}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Changes:")
            Text("• ${change.data.frogs.size} frogs")
            Text("• ${change.data.activityLogs.size} activity logs")
            Text("• ${change.data.rewardRedemptions.size} redemptions")

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Approve")
                }
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reject")
                }
            }
        }
    }
}
```

---

## Security Best Practices

### 1. Service Account Permissions

```bash
# Limit permissions - only grant what's needed
gsutil iam ch \
  serviceAccount:frog-sync@PROJECT_ID.iam.gserviceaccount.com:roles/storage.objectAdmin \
  gs://frog-life-sync
```

### 2. Store Credentials Securely

**Don't:**
❌ Hardcode in app
❌ Commit key file to git

**Do:**
✅ Store in assets/ and add to .gitignore
✅ Or use Android Keystore
✅ Or fetch from secure endpoint on first launch

**assets/frog-sync-key.json** (add to .gitignore)

### 3. Bucket CORS Configuration

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

Set via:
```bash
gsutil cors set cors.json gs://frog-life-sync
```

---

## Auto-Sync with WorkManager

**SyncWorker.kt:**
```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val syncService = GCSSyncService(applicationContext, repository)

        return try {
            if (syncService.hasNewData()) {
                val data = syncService.downloadApprovedData()
                // Import data
                Result.success()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

// Schedule periodic sync
val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
    .setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    )
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "frog_sync",
    ExistingPeriodicWorkPolicy.KEEP,
    syncRequest
)
```

---

## Testing Checklist

- [ ] Create GCS bucket
- [ ] Set up service account
- [ ] Test device registration
- [ ] Test slave submitting change
- [ ] Test master listing pending changes
- [ ] Test master approving change
- [ ] Test master rejecting change
- [ ] Test all devices pulling latest
- [ ] Test conflict resolution
- [ ] Test auto-sync
- [ ] Test offline handling
- [ ] Monitor GCS usage in console

---

## Troubleshooting

### "403 Forbidden"
**Fix:** Check service account has Storage Object Admin role

### "File not found"
**Fix:** Ensure bucket name is correct and file exists

### "Quota exceeded"
**Fix:** Check quota in console, upgrade if needed (unlikely)

### Slow uploads
**Fix:** Use `setChunkSize()` for large files, or compress JSON

---

## Cost Monitoring

**Google Cloud Console → Storage → Dashboard**

Monitor:
- Storage used
- Class A operations (writes)
- Class B operations (reads)
- Network egress

Set up billing alerts:
```
Billing → Budgets & alerts → Create budget
Set limit: $1/month
Alert at 50%, 90%, 100%
```

---

## Comparison vs Firebase

| Feature | GCS | Firebase |
|---------|-----|----------|
| **Free tier** | 5GB, 50K reads | 50K reads, 20K writes |
| **Real-time** | No (poll) | Yes |
| **Complexity** | Lower | Higher |
| **Queries** | List files | Complex queries |
| **Learning curve** | Easier | Steeper |
| **Your use case** | ✅ Perfect | Overkill |

---

## Next Steps

1. **Create bucket** in Google Cloud Console
2. **Set up service account** and download key
3. **Add key to app** (assets/frog-sync-key.json)
4. **Implement GCSSyncService**
5. **Add UI** to Settings screen
6. **Test with 2 devices**
7. **Enable auto-sync**

Would you like me to implement this GCS sync solution? It's simpler than Firebase and perfect for your use case since you already have GCS access!
