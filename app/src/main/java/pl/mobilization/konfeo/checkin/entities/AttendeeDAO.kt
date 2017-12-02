package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.*

/**
 * Created by defecins on 07/11/2017.
 */

@Dao
interface AttendeeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAttendees(attendees: List<Attendee>)

    @Update
    fun updateAttendees(vararg attendees: Attendee)

    @Query("SELECT COUNT(*) FROM attendees")
    fun countAttendees() : Long

    @Query("SELECT * FROM attendees WHERE event_id IN (:eventIds)")
    fun getAttendees(eventIds: List<Long>) : List<Attendee>

    @Query("SELECT * FROM attendees WHERE needs_update = 1 AND event_id IN (:eventIds)")
    fun getAttendeesToUpdate(eventIds: List<Long>) : List<Attendee>

    @Query("SELECT id FROM attendees WHERE needs_update = 1 AND event_id IN (:eventIds)")
    fun getAttendeeIdsToUpdate(eventIds: List<Long>) : List<Long>
}