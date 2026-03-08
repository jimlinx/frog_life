# Changelog

All notable changes to Frog Life will be documented in this file.

## [1.8] - 2026-03-04

### Added
- **Multi-Device Sync:** Complete Google Cloud Storage (GCS) based synchronization system
  - Master-slave approval workflow for multi-device use
  - Device type selection (Master or Slave) in Settings
  - "Submit Changes for Approval" button for slave devices
  - "Review Pending Changes" screen for master device approval/rejection
  - "Pull Latest Data" button to sync approved changes across all devices
  - **"Export to Bucket" feature** to manually backup data to GCS
    - Master device exports to approved/ folder (becomes new master data)
    - Slave device exports to manual_backups/ folder (personal backups)
    - One-click backup with progress feedback
    - Automatic versioned backups for master exports
  - **"Import from Bucket" feature** to browse and import from available backups
    - Lists all backup files from both approved/ and manual_backups/ folders
    - Shows "LATEST" badge on current master data
    - Shows "MANUAL" badge on manual backups from slave devices
    - Select any backup version to restore
    - Confirmation dialog to prevent accidental imports
  - Smart data merging with conflict resolution
  - Automatic versioned backups in GCS
  - Audit trail for rejected changes
  - Secure service account authentication
  - Device Manager for ID and type persistence
- **Documentation:** Complete setup guide (GCS_SYNC_SETUP_GUIDE.md) and implementation summary

### Changed
- Settings screen now includes Device Sync section (only visible when GCS credentials configured)
- Import/Export functionality refactored for better reuse with sync features
- Navigation updated with Pending Approvals route

### Technical
- Added Google Cloud Storage SDK dependency (2.30.0)
- Added WorkManager dependency (2.9.0) for future auto-sync feature
- Created sync package with GCSSyncService and DeviceManager
- Added PendingApprovalsScreen for master device
- GCS credentials excluded from git (.gitignore updated)

## [1.7] - 2026-03-04

### Added
- **Monthly Wins Tracking:** Automatic tracking of monthly winners based on "This Month" points
  - Awards monthly win to frog with highest points each month
  - Pet frog emoji 🐸 displayed on frog with most monthly wins
  - Automatic processing on app startup when new month detected
  - Manual "Process Monthly Wins" button added to Settings (Maintenance section)
  - Duplicate prevention ensures each month only processed once
- **Settings Page:** "Process Monthly Wins" button in Maintenance section
- **Main Page:** Visual dividers between menu sections for better organization
  - Divider between Calendar and Reward History
  - Divider between Manage Rewards and Settings

### Changed
- **Database:** Schema updated to version 6 (added lastMonthlyWinsProcessed to AppSettings)
- **Settings Page:** Reorganized with Status Thresholds moved to bottom
- **Settings Page:** Status Thresholds section now uses compact 2x3 grid layout (50% less space)
- **Main Page:** Calendar button background changed from teal to yellow
- **Settings Page:** Button text changed from "Save Settings" to "Save Thresholds"

### Improved
- Better visual grouping on main page with color-coded sections
- More efficient use of screen space in Settings page
- Monthly wins feature now fully functional (was implemented but never executed)

## [1.6] - 2026-03-04

### Changed
- **UI Improvements:** Reward History entries now more compact (reduced padding, shorter date format, combined layout)
- **UI Improvements:** View Frog points section redesigned as compact 2x2 grid (Month/Total in row 1, Balance/Wins in row 2)
- **UI Improvements:** Main page buttons now left-aligned with larger emojis (32sp) for better visual hierarchy
- **Main Page:** Reordered menu items - View Frog and Calendar now at top, Settings moved to bottom
- **Main Page:** View Frog button changed to green with 🔍 icon (was blue with 👀)
- **Main Page:** Calendar button changed to teal color
- **Layout:** Admin Override section moved below Special Dates section in View Frog detail page

### Improved
- More efficient use of screen space in View Frog detail page
- Better visual scanning of menu options with color coding and larger icons
- More compact reward redemption history display

## [1.5] - 2026-03-04

### Added
- **Reward Balance Display:** Reward balance (Total Wealth Points - Total Redemptions) now displayed in:
  - View Frog list page (shown below total points for each frog)
  - View Frog detail page (prominent card between Total Wealth Points and Monthly Wins)
  - Manage Frogs page (shown below monthly/total points line)
- Real-time reward balance calculation that updates automatically when redemptions change

### Changed
- View Frog detail page now shows reward balance in a dedicated card with secondary color scheme
- All frog list views now include reward balance information for better visibility

## [1.4] - 2026-03-04

### Added
- **Rewards System:** Complete rewards management feature
  - "Manage Rewards" screen to create, edit, and delete rewards with custom names, descriptions, colors, and point costs
  - "Reward History" screen showing all reward redemptions with add/edit/delete capabilities
  - Reward redemptions tracked by date, frog, reward, and points used
  - Reward balance calculation (Total Wealth Points - Total Redemption Points)
  - Date picker for selecting redemption dates
  - Auto-population of points when selecting a reward (points are read-only based on reward cost)
  - Reward dropdown displays points cost before reward name (e.g., "50 pts - Ice Cream")
- Database migration from version 4 to version 5 with new `rewards` and `reward_redemptions` tables
- Import/Export functionality now includes rewards and redemptions with proper ID mapping
- Duplicate "Update Frog" button at top of Edit Frog screen for convenience

### Changed
- Database schema updated to version 5
- Enhanced export data structure to include rewards and redemptions

## [1.3] - 2026-03-01

### Changed
- **UX Improvement:** Removed checkbox for boolean activities - they are now automatically marked as completed when tracked
- Boolean activities show "✓ Will be marked as completed" message instead of checkbox

### Removed
- Checkbox UI for boolean activity tracking (redundant step removed)

## [1.2] - 2026-03-01

### Fixed
- **Critical:** `updateFrog` now properly recalculates total wealth points and current month points from activity logs instead of using potentially incorrect cached values
- Status is now guaranteed to be calculated from total wealth points, not monthly points
- Improved app initialization with error handling and proper timing
- Editing a frog (e.g., changing name) now triggers full recalculation of all stats

### Changed
- More robust initialization on app startup with error handling

## [1.1] - 2026-03-01

### Fixed
- Database migration now preserves existing data when upgrading from version 3 to 4
- Status calculation now correctly based on total wealth points
- Current month points now correctly calculated from activity logs
- Added proper database migration script to avoid data loss

### Added
- "Recalculate All Frog Stats" button in Settings screen to manually fix status and points
- Proper database migration from version 3 to version 4

## [1.0] - 2026-03-01

### Added
- Initial release
- Version display on main screen (bottom right corner)
- Current month points tracking for each frog
- Monthly wins counter to track how many months a frog has been the winner
- Multiple achievement icons:
  - Crown (👑) for current month leader
  - Angel halo (😇) for total points leader
  - Tiny pet frog (🐸) for most monthly wins
- Ranking system based on current month points in View Frog list

### Fixed
- Status not being reflected correctly - now recalculates from total wealth points
- Status updates automatically when activity logs are added/modified

### Changed
- View Frog list now ranked by current month points instead of total points
- View Frog list displays both current month and total points
- Manage Frogs screen shows both current month and total points
- Database schema updated to version 4
