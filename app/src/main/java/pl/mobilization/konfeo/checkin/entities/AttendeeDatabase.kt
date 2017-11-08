package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by defecins on 07/11/2017.
 */

@Database(entities = arrayOf(Attendee::class), version = 1)
abstract class AttendeeDatabase : RoomDatabase() {
    abstract fun attendeeDAO() : AttendeeDAO
}