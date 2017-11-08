package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.*

/**
 * Created by defecins on 07/11/2017.
 */

@Dao
interface AttendeeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendees(vararg  attendees: Attendee)

    @Update
    fun updateAttendees(vararg attendees: Attendee)

    @Query("SELECT COUNT(*) FROM attendee")
    fun countAttendees() : Long

    @Query("SELECT * FROM attendee")
    fun getAttendees() : List<Attendee>

    @Query("SELECT * FROM attendee WHERE first_name LIKE :filter OR last_name LIKE :filter OR email LIKE :filter")
    fun getAttendees(filter: String) : List<Attendee>
}