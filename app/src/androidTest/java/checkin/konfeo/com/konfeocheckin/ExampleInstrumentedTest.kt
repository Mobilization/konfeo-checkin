package checkin.konfeo.com.konfeocheckin

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import pl.mobilization.konfeo.checkin.entities.Attendee
import pl.mobilization.konfeo.checkin.entities.AttendeeDatabase

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val db = Room.databaseBuilder(InstrumentationRegistry.getTargetContext(),
                AttendeeDatabase::class.java, "attendees").build()

        val attendees = db.attendeeDAO().getAttendees()
        assertNotNull(attendees)

        val attendee = Attendee(Long.MAX_VALUE, "Marek", "Defeciński", "marekdef@tlen.pl", "Organizer", Integer.MAX_VALUE.toLong(), false)

        db.attendeeDAO().insertAttendees(attendee)

        attendee.needs_update = true
        db.attendeeDAO().updateAttendees(attendee)
    }
}
