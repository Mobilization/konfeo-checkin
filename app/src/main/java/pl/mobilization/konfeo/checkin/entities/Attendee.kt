package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by defecins on 07/11/2017.
 */

@Entity(tableName = "Attendee")
data class Attendee(
        @PrimaryKey var id: Long,
        var first_name: String,
        var last_name: String,
        var email: String,
        var group: String,
        var number: Long?,
        var needs_update: Boolean = false
)