package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.*

/**
 * Created by defecins on 26/11/2017.
 */


@Dao
interface EventsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertEvents(events: List<Event>)

    @Update
    fun updateEvents(vararg  events: Event)

    @Query("SELECT * from Events")
    fun getEvents() : List<Event>

    @Query("SELECT * from Events WHERE enabled = 1")
    fun getEnabledEvents() : List<Event>

    @Query("SELECT COUNT(*) FROM Events WHERE enabled = 1")
    fun countEnabledEvents() : Long

    @Query("UPDATE Events SET enabled = :checked WHERE id = :id")
    fun checkEvent(id: Long, checked: Boolean)
}
