package pl.mobilization.konfeo.checkin

import android.app.SearchManager
import android.arch.persistence.room.Room
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import checkin.konfeo.com.konfeocheckin.R
import kotlinx.android.synthetic.main.activity_attendees.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import pl.mobilization.konfeo.checkin.entities.AttendeeDatabase

import android.content.Context
import android.content.Intent
import android.support.v7.widget.SearchView


class AttendeesActivity : AppCompatActivity() {

    lateinit var db: AttendeeDatabase

    lateinit var attendeeAdapter: AttendeeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext,
                AttendeeDatabase::class.java, "attendees")
                .addMigrations(AttendeeDatabase.MIGRATION_1_2, AttendeeDatabase.MIGRATION_2_3).build()

        setContentView(R.layout.activity_attendees)

        recyclerViewAttendees.itemAnimator = DefaultItemAnimator()
        recyclerViewAttendees.layoutManager = LinearLayoutManager(this)

        attendeeAdapter = AttendeeAdapter(this)
        recyclerViewAttendees.adapter = attendeeAdapter

        val event_url = intent.getStringExtra(EVENT_URL_PARAM)
        event_url?.let {
            pl.mobilization.konfeo.checkin.KonfeoIntentService.Companion.startActionUsers(this, it, AttendeesReceiver())
        }
    }

    private fun filter_attendees(query: String) {
        attendeeAdapter.filter = query
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate( R.menu.menu, menu)

        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        searchView.setIconifiedByDefault(false) // Do not iconify the widget; expand it by default
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.trim().length < 3) {
                    return false
                }

                attendeeAdapter.filter = newText
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                attendeeAdapter.filter = query
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    internal inner class AttendeesReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            launch {
                val attendees = db.attendeeDAO().getAttendees()

                launch(UI) {
                    attendeeAdapter.add(attendees)
                }
            }
        }
    }

}

