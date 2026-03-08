# Multi-Device Sync Implementation Options

## Overview
Enable multiple devices to use Frog Life with one master device that approves changes from slave devices before syncing to all.

---

## ✅ RECOMMENDED: Option 1 - Firebase Firestore (Easiest)

### Why This?
- **Free Tier:** 50K reads, 20K writes, 1GB storage/day (plenty for family use)
- **Real-time sync:** Instant updates across devices
- **Battle-tested:** Used by millions of apps
- **Easy implementation:** ~2-3 days of work
- **No server needed:** Google hosts everything

### Architecture

```
Slave Device A ──┐
                 ├──> Firebase Firestore ──> Master Device
Slave Device B ──┘                            (Approves/Rejects)
                                                    │
                                                    ▼
                                            Publishes to Firestore
                                                    │
                                                    ▼
                    ┌───────────────────────────────┴─────────────────┐
                    ▼                               ▼                 ▼
              Slave Device A                 Slave Device B      Slave Device C
```

### Database Structure

```
/devices
  /{device_id}
    - name: "Mom's Phone"
    - type: "master" | "slave"
    - lastSeen: timestamp

/pending_changes
  /{change_id}
    - deviceId: "slave_device_1"
    - timestamp: 1234567890
    - status: "pending" | "approved" | "rejected"
    - changeType: "frog_created" | "activity_logged" | "reward_redeemed"
    - data: { ... change payload ... }
    - conflictsWith: [array of conflicting changes]

/approved_data
  /frogs
    /{frog_id}
      - name, wealthPoints, status, etc.
      - lastModified: timestamp
      - modifiedBy: device_id

  /activities
  /activity_logs
  /rewards
  /reward_redemptions
  /special_dates
  /day_comments
```

### Implementation Steps

**1. Add Firebase to Project** (build.gradle.kts)
```kotlin
dependencies {
    implementation("com.google.firebase:firebase-bom:32.7.0")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx") // For device auth
}
```

**2. Create SyncService**
```kotlin
class FirebaseSyncService(
    private val firestore: FirebaseFirestore,
    private val localRepository: FrogRepository
) {
    private val deviceId = getDeviceId() // Unique device ID

    suspend fun submitChange(change: PendingChange) {
        if (isMasterDevice()) {
            // Master approves own changes immediately
            approveAndPublish(change)
        } else {
            // Slave submits for approval
            firestore.collection("pending_changes")
                .add(change.copy(deviceId = deviceId, status = "pending"))
        }
    }

    fun listenForApprovedChanges() {
        firestore.collection("approved_data")
            .addSnapshotListener { snapshot, error ->
                snapshot?.documentChanges?.forEach { change ->
                    when (change.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> applyChange(change.document)
                        DocumentChange.Type.REMOVED -> removeData(change.document)
                    }
                }
            }
    }
}
```

**3. Master Approval UI**
```kotlin
// Add to Settings screen or new "Pending Approvals" screen
@Composable
fun PendingApprovalsScreen(syncService: FirebaseSyncService) {
    val pendingChanges by syncService.getPendingChanges().collectAsState()

    LazyColumn {
        items(pendingChanges) { change ->
            ChangeApprovalCard(
                change = change,
                onApprove = { syncService.approveChange(change) },
                onReject = { syncService.rejectChange(change) }
            )
        }
    }
}
```

### Cost Analysis
- **Free tier:** Unlimited for typical family use (< 1000 operations/day)
- **If exceeded:** $0.18 per 100K reads, $0.18 per 100K writes
- **Realistic cost:** $0/month for personal use

### Pros
✅ No server maintenance
✅ Real-time sync
✅ Offline support
✅ Automatic conflict detection
✅ Free for personal use

### Cons
❌ Requires Google account
❌ Data hosted on Google servers
❌ Need internet connection for sync

---

## Option 2 - Supabase (Open Source Alternative)

### Why This?
- **Free Tier:** 500MB database, unlimited API requests, 2GB file storage
- **PostgreSQL:** Full SQL database (vs Firestore's NoSQL)
- **Open source:** Can self-host later
- **Real-time subscriptions:** Like Firebase
- **More generous free tier:** Never expires

### Architecture
Same as Firebase, but using Supabase PostgreSQL + Realtime

### Database Schema (PostgreSQL)

```sql
-- Devices table
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    device_type TEXT CHECK (device_type IN ('master', 'slave')),
    last_seen TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Pending changes table
CREATE TABLE pending_changes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id UUID REFERENCES devices(id),
    change_type TEXT NOT NULL,
    status TEXT CHECK (status IN ('pending', 'approved', 'rejected')),
    data JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Synced frogs (master data)
CREATE TABLE synced_frogs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    wealth_points INTEGER DEFAULT 10,
    -- ... all other frog fields
    last_modified TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    modified_by UUID REFERENCES devices(id)
);

-- Enable real-time
ALTER PUBLICATION supabase_realtime ADD TABLE pending_changes;
ALTER PUBLICATION supabase_realtime ADD TABLE synced_frogs;
```

### Implementation

**1. Add Supabase** (build.gradle.kts)
```kotlin
dependencies {
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.1.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.1.0")
}
```

**2. Supabase Client**
```kotlin
val supabase = createSupabaseClient(
    supabaseUrl = "YOUR_PROJECT_URL",
    supabaseKey = "YOUR_ANON_KEY"
) {
    install(Postgrest)
    install(Realtime)
}
```

### Pros
✅ More generous free tier
✅ PostgreSQL (familiar SQL)
✅ Can self-host
✅ Open source
✅ Real-time subscriptions

### Cons
❌ Smaller community than Firebase
❌ Less documentation
❌ Requires learning Supabase SDK

---

## Option 3 - Self-Hosted PocketBase (Most Control)

### Why This?
- **Completely free:** Self-hosted on Raspberry Pi, old laptop, or free hosting
- **Single binary:** Easy to deploy
- **Built-in admin UI:** For managing data
- **Real-time subscriptions:** WebSocket based
- **SQLite:** Lightweight, portable

### Setup

**1. Install PocketBase** (on Raspberry Pi or free hosting like Fly.io)
```bash
# Download PocketBase
wget https://github.com/pocketbase/pocketbase/releases/download/v0.20.0/pocketbase_0.20.0_linux_amd64.zip
unzip pocketbase_0.20.0_linux_amd64.zip
./pocketbase serve --http="0.0.0.0:8080"
```

**2. Define Collections** (via Admin UI at http://localhost:8080/_/)
- `devices` collection
- `pending_changes` collection
- `frogs` collection
- etc.

**3. Android Client**
```kotlin
// Add PocketBase Kotlin client
dependencies {
    implementation("io.github.agrevster:pocketbase-kotlin:0.7.0")
}

// Connect
val client = PocketBase("http://your-server-ip:8080")

// Subscribe to changes
client.realtime.subscribe("frogs") { event ->
    // Handle real-time update
}
```

### Hosting Options (Free)
1. **Raspberry Pi at home:** Port forward 8080
2. **Fly.io:** Free tier (256MB RAM, 3GB storage)
3. **Railway.app:** $5 credit/month (free tier)
4. **Old Android phone:** Use Termux to run PocketBase

### Pros
✅ Complete control
✅ No usage limits
✅ Data stays private
✅ Can run on $35 Raspberry Pi
✅ Built-in admin dashboard

### Cons
❌ Need to manage server
❌ Need static IP or dynamic DNS
❌ You're responsible for backups
❌ Need to handle security

---

## Option 4 - Git-Based (Manual but Reliable)

### Why This?
- **Free:** GitHub/GitLab free tier
- **Version control:** Full history of all changes
- **No coding:** Extend current export/import
- **Simple:** Uses existing export/import feature

### Workflow

```
Slave Device                    GitHub Repo                Master Device
────────────                    ───────────                ─────────────
Make changes
     │
     ├──> Export to JSON
     │
     ├──> Create Pull Request ──────────────────────────> Review PR
                                                               │
                                                               ├──> Approve/Reject
                                                               │
                                                          Merge PR
                                                               │
                                                          Pull latest
                                                               │
                                                          Import JSON
                                                               │
     Import JSON <───────────────────────────── All slaves pull
```

### Implementation

**1. GitHub Actions** (auto-sync)
```yaml
# .github/workflows/sync.yml
name: Sync Frog Data
on:
  push:
    branches: [main]
jobs:
  notify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/github-script@v6
        with:
          script: |
            // Trigger notification to all devices via webhook
```

**2. Add to App**
- Button: "Submit Changes for Approval" → Creates branch + PR
- Button: "Pull Latest Approved Data" → Downloads from main branch
- Notifications when new data available

### Pros
✅ Completely free
✅ Full version history
✅ No server needed
✅ Simple to understand

### Cons
❌ Manual approval via GitHub
❌ Not real-time
❌ Requires GitHub account
❌ More steps for users

---

## Comparison Table

| Feature | Firebase | Supabase | PocketBase | Git-Based |
|---------|----------|----------|------------|-----------|
| **Cost** | Free tier | Free tier | $0 (self-host) | $0 |
| **Setup Time** | 2-3 days | 3-4 days | 1-2 days | 1 day |
| **Real-time** | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| **Offline Support** | ✅ Yes | ✅ Yes | ⚠️ Limited | ✅ Yes |
| **Maintenance** | None | None | Self-managed | None |
| **Data Privacy** | Google servers | Supabase cloud | Your server | GitHub |
| **Scalability** | Excellent | Excellent | Good | Poor |
| **Complexity** | Medium | Medium | Low-Medium | Low |

---

## 🏆 Recommendation for Your Use Case

### For Family Use (2-5 devices):
**Go with Firebase Firestore**

**Why?**
1. Zero maintenance
2. Generous free tier
3. Real-time sync
4. Easy to implement
5. Battle-tested reliability

### For Privacy-First:
**Go with Self-Hosted PocketBase**

**Why?**
1. Complete control
2. Data stays private
3. One-time setup on Raspberry Pi
4. No recurring costs

### For Simplicity:
**Go with Git-Based**

**Why?**
1. Uses existing export/import
2. No new code needed
3. Simple workflow
4. Manual but reliable

---

## Implementation Priority (Firebase)

### Phase 1 - Basic Sync (Week 1)
- [ ] Add Firebase to project
- [ ] Implement device registration
- [ ] Create pending changes submission
- [ ] Build master approval UI

### Phase 2 - Real-time Updates (Week 2)
- [ ] Implement real-time listeners
- [ ] Handle conflict resolution
- [ ] Add offline support
- [ ] Test with 2 devices

### Phase 3 - Polish (Week 3)
- [ ] Add sync status indicators
- [ ] Implement batch operations
- [ ] Add change history view
- [ ] User documentation

---

## Security Considerations

### Firebase Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Only authenticated devices can read/write
    match /{document=**} {
      allow read, write: if request.auth != null;
    }

    // Only master can approve changes
    match /pending_changes/{changeId} {
      allow update: if get(/databases/$(database)/documents/devices/$(request.auth.uid)).data.device_type == 'master';
    }
  }
}
```

### Best Practices
1. **Encryption:** Encrypt sensitive data before sending
2. **Authentication:** Each device needs unique auth token
3. **Validation:** Validate all changes on server side
4. **Rate Limiting:** Prevent abuse with rate limits
5. **Backup:** Regular automated backups

---

## Estimated Costs (Monthly)

### Firebase (Family of 4)
- Operations: ~10K/day = 300K/month
- Free tier: 50K reads + 20K writes/day
- **Cost:** $0/month (well within free tier)

### Supabase
- Database: < 100MB
- API requests: ~300K/month
- **Cost:** $0/month (unlimited API requests)

### PocketBase
- Raspberry Pi 4: $35 one-time
- Electricity: ~$0.50/month
- **Cost:** $0.50/month ongoing

### Git-Based
- GitHub free tier
- **Cost:** $0/month

---

## Next Steps

1. **Choose option** based on your priorities
2. **Create proof-of-concept** with 2 devices
3. **Test approval workflow**
4. **Add UI for sync status**
5. **Deploy to family**

Would you like me to implement any of these options? I recommend starting with Firebase as it's the most balanced choice for your use case.
