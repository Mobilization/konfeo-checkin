package checkin.konfeo.com.konfeocheckin

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import pl.mobilization.konfeo.checkin.adapters.AttendeeAdapter
import pl.mobilization.konfeo.checkin.entities.Attendee

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class AttendeeAdapterUnitTest {
    private val attendeAdapter = AttendeeAdapter(RuntimeEnvironment.application)

    private val MAREK = Attendee(
            first_name = "Marek",
            last_name = "Defeciński",
            email = "marekdef@tlen.pl",
            id = 1,
            number = 1,
            needs_update = false,
            group = "Organizer",
            event_id = 0
    )

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
    fun has_one_attendee() {
        attendeAdapter.add(arrayListOf(MAREK))

        assertEquals(1, attendeAdapter.itemCount)
    }

    @Test
    fun has_two_attendees() {
        attendeAdapter.add(arrayListOf(MAREK, ANDRZEJ))

        assertEquals(2, attendeAdapter.itemCount)
    }

    @Test
    fun does_not_filter_out_marek() {
        attendeAdapter.add(arrayListOf(MAREK))

        attendeAdapter.filter = "marek"

        assertEquals(1, attendeAdapter.itemCount)
    }

    @Test
    fun filters_out_marek() {
        attendeAdapter.add(arrayListOf(MAREK))

        attendeAdapter.filter = "piotr"

        assertEquals(0, attendeAdapter.itemCount)
    }

    @Test
    fun filters_marek_from2() {
        attendeAdapter.add(arrayListOf(MAREK, ANDRZEJ))

        attendeAdapter.filter = "Marek"

        assertEquals(1, attendeAdapter.itemCount)
    }

    @Test
    fun filters_andrzej_from2() {
        attendeAdapter.add(arrayListOf(MAREK, ANDRZEJ))

        attendeAdapter.filter = "Jóźwiak"

        assertEquals(1, attendeAdapter.itemCount)
    }
}