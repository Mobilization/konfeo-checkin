package pl.mobilization.konfeo.checkin.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import org.apache.commons.lang3.StringUtils

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
) {
    @Ignore
    private var _last_name_normalized: String = ""
    val last_name_normalized : String
    get() {
        if(_last_name_normalized == null)
            _last_name_normalized = StringUtils.stripAccents(last_name)
        return _last_name_normalized
    }

    @Ignore
    private var _first_name_normalized: String = ""
    val first_name_normalized : String
        get() {
            if(_first_name_normalized == null)
                _first_name_normalized = StringUtils.stripAccents(last_name)
            return _first_name_normalized
        }
}