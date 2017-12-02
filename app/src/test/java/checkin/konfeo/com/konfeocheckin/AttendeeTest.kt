package checkin.konfeo.com.konfeocheckin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.mobilization.konfeo.checkin.entities.Attendee

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class AttendeeTest {
    private val ANDRZEJ = Attendee(
            first_name = "Andrzej",
            last_name = "Jóźwiak",
            email = "andrzej.jozwiak@tlen.pl",
            id = 2,
            number = 2,
            needs_update = false,
            group = "Organizer",
            event_id = 0
    )

    @Test
    fun attende_normalizer() {
        assertEquals("jozwiak", ANDRZEJ.last_name_normalized  )
        assertEquals("andrzej", ANDRZEJ.first_name_normalized  )
    }

    @Test
    fun attende_matches() {
        assertTrue(ANDRZEJ.matches("andrzej") )
        assertTrue(ANDRZEJ.matches("jozwiak") )
    }
}