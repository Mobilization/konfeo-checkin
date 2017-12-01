package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.apache.commons.lang3.StringUtils

/**
 * Created by defecins on 07/11/2017.
 */

@Entity(tableName = "Attendees", primaryKeys = arrayOf("id", "event_id"))
data class Attendee(
        var id: Long,
        var first_name: String,
        var last_name: String,
        var email: String,
        var group: String,
        var number: Long?,
        var needs_update: Boolean = false,
        var checked_in: Boolean = false,
        var event_id: Long
) {
    @delegate:Ignore
    val last_name_normalized : String by lazy {
        StringUtils.stripAccents(last_name.toLowerCase())
    }

    @delegate:Ignore
    val first_name_normalized : String by lazy {
        StringUtils.stripAccents(first_name.toLowerCase())
    }

    @delegate:Ignore
    val number_string : String by lazy {
        number.toString()
    }

    @delegate:Ignore
    val id_string: String by lazy {
        id.toString()
    }


    fun matches(filter: String): Boolean {
        return last_name_normalized.startsWith(filter)
                || first_name_normalized.startsWith(filter)
                || email.contains(filter)
                || number_string.contains(filter)
                || id_string.contains(filter)
    }
}

