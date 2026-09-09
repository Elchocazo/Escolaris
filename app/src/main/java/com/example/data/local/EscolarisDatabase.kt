package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.SchoolDao
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.ExamEntity
import com.example.data.local.entity.FeedPostEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.ParentObligationEntity
import com.example.data.local.entity.PenaltyEntity
import com.example.data.local.entity.PostCommentEntity
import com.example.data.local.entity.RedemptionEntity
import com.example.data.local.entity.RewardEntity
import com.example.data.local.entity.ScheduleEntity
import com.example.data.local.entity.SchoolEventEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TardyRecordEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        FeedPostEntity::class,
        PostCommentEntity::class,
        TaskEntity::class,
        ExamEntity::class,
        ScheduleEntity::class,
        RewardEntity::class,
        RedemptionEntity::class,
        NotificationEntity::class,
        TardyRecordEntity::class,
        BadgeEntity::class,
        SubjectEntity::class,
        SchoolEventEntity::class,
        PenaltyEntity::class,
        ParentObligationEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class EscolarisDatabase : RoomDatabase() {
    abstract fun schoolDao(): SchoolDao

    companion object {
        @Volatile
        private var INSTANCE: EscolarisDatabase? = null

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN dueDate TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE tasks ADD COLUMN completed INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {}
                try {
                    db.execSQL("ALTER TABLE tardy_records ADD COLUMN firestoreId TEXT NOT NULL DEFAULT ''")
                } catch (e: Exception) {}
            }
        }

        // Migraciones seguras para proteger datos en cualquier actualización de versión
        private val SAFE_MIGRATIONS: Array<Migration> = Array(20) { i ->
            if (i + 1 == 10) {
                MIGRATION_10_11
            } else {
                object : Migration(i + 1, i + 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Migración no destructiva
                    }
                }
            }
        }

        fun getDatabase(context: Context): EscolarisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EscolarisDatabase::class.java,
                    "escolaris_database"
                )
                .addMigrations(*SAFE_MIGRATIONS)
                .fallbackToDestructiveMigrationOnDowngrade(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
