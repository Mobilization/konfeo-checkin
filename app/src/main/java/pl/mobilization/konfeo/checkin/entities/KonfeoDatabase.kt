package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration



/**
 * Created by defecins on 07/11/2017.
 */

@Database(entities = arrayOf(Attendee::class, Event::class), version = 4)
abstract class KonfeoDatabase : RoomDatabase() {
    abstract fun attendeeDAO() : AttendeeDAO

    abstract fun eventsDAO() : EventsDAO

    companion object {
    }
}