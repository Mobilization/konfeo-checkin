package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration



/**
 * Created by defecins on 07/11/2017.
 */

@Database(entities = arrayOf(Attendee::class), version = 3)
abstract class AttendeeDatabase : RoomDatabase() {
    abstract fun attendeeDAO() : AttendeeDAO

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Attendee ADD COLUMN checked_in INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_2_3: Migration = object : Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Attendee ADD COLUMN event_id TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}