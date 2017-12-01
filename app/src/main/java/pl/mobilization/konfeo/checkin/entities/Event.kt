package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by defecins on 26/11/2017.
 */

@Entity(tableName = "Events")
data class Event(
        @PrimaryKey val id: Long,
        val name: String,
        val enabled: Boolean = true)