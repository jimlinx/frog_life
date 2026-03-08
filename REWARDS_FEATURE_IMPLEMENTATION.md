# Rewards Feature Implementation

## Summary
This document outlines the implementation of the Rewards management feature for the Frog Life app. The feature allows users to create rewards that frogs can redeem using their wealth points.

## ✅ Completed Components

### 1. Database Layer
- **Created Files:**
  - `app/src/main/java/com/froglife/data/Reward.kt` - Reward entity with name, description, color, and points cost
  - `app/src/main/java/com/froglife/data/RewardRedemption.kt` - Redemption tracking entity with foreign keys to Frog and Reward
  - `app/src/main/java/com/froglife/data/RewardDao.kt` - DAO for reward operations
  - `app/src/main/java/com/froglife/data/RewardRedemptionDao.kt` - DAO for redemption operations

- **Updated Files:**
  - `app/src/main/java/com/froglife/data/FrogDatabase.kt` - Added rewards and redemptions tables, created migration from v4 to v5
  - `app/src/main/java/com/froglife/data/FrogRepository.kt` - Added reward and redemption repository methods

### 2. ViewModel Layer
- **Created Files:**
  - `app/src/main/java/com/froglife/viewmodel/RewardViewModel.kt` - Manages rewards and redemptions state

- **Updated Files:**
  - `app/src/main/java/com/froglife/viewmodel/FrogViewModel.kt` - Added `calculateRewardBalance()` and `getRewardBalanceFlow()` functions

### 3. UI Screens
- **Created Files:**
  - `app/src/main/java/com/froglife/ui/screens/ManageRewardsScreen.kt` - Screen to view and manage all rewards
  - `app/src/main/java/com/froglife/ui/screens/AddEditRewardScreen.kt` - Screen to create/edit rewards with color picker
  - `app/src/main/java/com/froglife/ui/screens/RewardHistoryScreen.kt` - Screen showing all reward redemptions with add/edit/delete capabilities

- **Updated Files:**
  - `app/src/main/java/com/froglife/ui/screens/MainScreen.kt` - Added "Manage Rewards" and "Reward History" buttons
  - `app/src/main/java/com/froglife/ui/screens/AddEditFrogScreen.kt` - Added duplicate "Update Frog" button at top of form

### 4. Navigation
- **Updated Files:**
  - `app/src/main/java/com/froglife/ui/Navigation.kt` - Added routes for ManageRewards, RewardHistory, and AddEditReward
  - `app/src/main/java/com/froglife/MainActivity.kt` - Added RewardViewModel initialization and navigation composables

### 5. Import/Export
- **Updated Files:**
  - `app/src/main/java/com/froglife/utils/DataExporter.kt` - Added rewards and redemptions to ExportData
  - `app/src/main/java/com/froglife/ui/screens/SettingsScreen.kt` - Updated export/import functions to include rewards and redemptions with ID mapping

## 🔄 Remaining Tasks

### 1. Display Reward Balance
Need to update the following screens to display reward balance (Total Wealth Points - Total Redemption Points):

**ManageFrogsScreen** (`app/src/main/java/com/froglife/ui/screens/ManageFrogsScreen.kt`):
- Add RewardViewModel parameter
- Calculate and display reward balance for each frog in the list
- Update MainActivity to pass RewardViewModel

**ViewFrogScreen** (`app/src/main/java/com/froglife/ui/screens/ViewFrogScreen.kt`):
- Add RewardViewModel parameter to ViewFrogScreen and FrogDetailsScreen functions
- Add Reward Balance card after Total Wealth Points card (around line 381)
- Use `frogViewModel.getRewardBalanceFlow(frogId)` to display the balance
- Update MainActivity to pass RewardViewModel

**Implementation Example for ViewFrogScreen:**
```kotlin
// Add after Total Wealth Points card (line 381)
val rewardBalance by frogViewModel.getRewardBalanceFlow(currentFrog.id).collectAsState(initial = 0)

Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Reward Balance", style = MaterialTheme.typography.titleMedium)
        Text(
            rewardBalance.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}
```

### 2. Move Special Dates Section in ViewFrogScreen
In `app/src/main/java/com/froglife/ui/screens/ViewFrogScreen.kt`:
- Move the SpecialDatesSection call (currently at line 542-546) to before the Admin Override Section (line 470)
- Optionally split the section to keep "Past" dates after Admin Override

**Current structure:**
```
Admin Override Section (line 470-540)
Special Dates Section (line 542-546)
  - Upcoming dates
  - Past dates
```

**Desired structure:**
```
Special Dates Section
  - Upcoming dates
Admin Override Section
(Optionally: Past Special Dates)
```

### 3. Add "Redeem Reward" Button to Calendar Screen
In `app/src/main/java/com/froglife/ui/screens/CalendarScreen.kt`:

**For "Today" view (CalendarView.DAY):**
- Add a "Redeem Reward" button in the day view
- Button should open a dialog to select from available rewards
- On redemption, create a RewardRedemption record with:
  - frogId: selected frog
  - rewardId: selected reward
  - date: current date
  - pointsUsed: reward.pointsCost
- Pass RewardViewModel to CalendarScreen
- Update MainActivity to pass RewardViewModel

**For Week/Month/Year views:**
- Display redemption indicators on days where rewards were redeemed
- Similar to how activity logs are shown

### 4. Update Recalculate Stats
The "Recalculate All Frog Stats" button in SettingsScreen currently recalculates wealth points and status. No changes needed as reward balance is calculated dynamically from wealth points and redemptions.

## How the Reward System Works

1. **Creating Rewards:**
   - Navigate to Main > Manage Rewards
   - Click + button to create a new reward
   - Specify name, description, color, and points cost

2. **Redeeming Rewards:**
   - In Calendar screen (today view), click "Redeem Reward" button (to be implemented)
   - Or in Reward History screen, click + button to manually add a redemption
   - Select frog, reward, date, and points used

3. **Reward Balance Calculation:**
   - Formula: `Reward Balance = Wealth Points - Sum(All Redemption Points)`
   - Displayed in View Frog screen and Manage Frogs screen
   - Updated automatically as redemptions are added/removed

4. **Viewing History:**
   - Navigate to Main > Reward History
   - Shows all redemptions sorted by date (newest first)
   - Can edit or delete any redemption
   - Can manually add redemptions with custom date

## Database Schema

### rewards table
```sql
CREATE TABLE rewards (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    color INTEGER NOT NULL,
    pointsCost INTEGER NOT NULL
)
```

### reward_redemptions table
```sql
CREATE TABLE reward_redemptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    frogId INTEGER NOT NULL,
    rewardId INTEGER NOT NULL,
    date INTEGER NOT NULL,
    pointsUsed INTEGER NOT NULL,
    FOREIGN KEY(frogId) REFERENCES frogs(id) ON DELETE CASCADE,
    FOREIGN KEY(rewardId) REFERENCES rewards(id) ON DELETE CASCADE
)
```

## Testing Checklist

Once remaining tasks are complete, test the following:

- [ ] Create a new reward
- [ ] Edit an existing reward
- [ ] Delete a reward (verify redemptions are also deleted)
- [ ] Add a manual redemption in Reward History
- [ ] Edit a redemption
- [ ] Delete a redemption
- [ ] Verify reward balance updates correctly
- [ ] Export data and verify rewards/redemptions are included
- [ ] Import data and verify rewards/redemptions are restored with correct ID mapping
- [ ] Redeem a reward from Calendar screen (once implemented)
- [ ] Verify reward balance appears in View Frog page
- [ ] Verify reward balance appears in Manage Frogs page (once implemented)

## Notes

- The database migration from v4 to v5 will run automatically on app startup
- All reward-related data is included in export/import functionality
- Reward redemptions cascade delete when parent reward or frog is deleted
- Reward balance is calculated dynamically and not stored in the database
