package com.froglife.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Frog::class,
        Activity::class,
        FrogActivityCrossRef::class,
        ActivityLog::class,
        AppSettings::class,
        SpecialDate::class,
        DayComment::class,
        Reward::class,
        RewardRedemption::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FrogDatabase : RoomDatabase() {
    abstract fun frogDao(): FrogDao
    abstract fun activityDao(): ActivityDao
    abstract fun frogActivityDao(): FrogActivityDao
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun settingsDao(): SettingsDao
    abstract fun specialDateDao(): SpecialDateDao
    abstract fun dayCommentDao(): DayCommentDao
    abstract fun rewardDao(): RewardDao
    abstract fun rewardRedemptionDao(): RewardRedemptionDao

    companion object {
        @Volatile
        private var INSTANCE: FrogDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to frogs table with default values
                database.execSQL("ALTER TABLE frogs ADD COLUMN currentMonthPoints INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE frogs ADD COLUMN monthlyWins INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE frogs ADD COLUMN lastMonthWinRecorded TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create rewards table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS rewards (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        pointsCost INTEGER NOT NULL
                    )
                """)

                // Create reward_redemptions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS reward_redemptions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        frogId INTEGER NOT NULL,
                        rewardId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        pointsUsed INTEGER NOT NULL,
                        FOREIGN KEY(frogId) REFERENCES frogs(id) ON DELETE CASCADE,
                        FOREIGN KEY(rewardId) REFERENCES rewards(id) ON DELETE CASCADE
                    )
                """)

                // Create indices for reward_redemptions
                database.execSQL("CREATE INDEX IF NOT EXISTS index_reward_redemptions_frogId ON reward_redemptions(frogId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_reward_redemptions_rewardId ON reward_redemptions(rewardId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_reward_redemptions_date ON reward_redemptions(date)")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add lastMonthlyWinsProcessed column to app_settings table
                database.execSQL("ALTER TABLE app_settings ADD COLUMN lastMonthlyWinsProcessed TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): FrogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FrogDatabase::class.java,
                    "frog_database"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
