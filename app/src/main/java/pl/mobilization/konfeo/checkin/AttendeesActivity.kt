package pl.mobilization.konfeo.checkin

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
import pl.mobilization.konfeo.checkin.entities.KonfeoDatabase

import pl.mobilization.konfeo.checkin.adapters.AttendeeAdapter

import android.widget.SearchView


class AttendeesActivity : AppCompatActivity() {

    lateinit var db: KonfeoDatabase

    lateinit var attendeeAdapter: AttendeeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(applicationContext,
                KonfeoDatabase::class.java, "konfeo").build()

        setContentView(R.layout.activity_attendees)

        setSupportActionBar(toolbarAttendees)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.trim().length in 1..2) {
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

        recyclerViewAttendees.itemAnimator = DefaultItemAnimator()
        recyclerViewAttendees.layoutManager = LinearLayoutManager(this)

        attendeeAdapter = AttendeeAdapter(this)
        recyclerViewAttendees.adapter = attendeeAdapter

        resetAttendees()
    }

    internal inner class AttendeesReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            resetAttendees()
        }
    }

    private fun resetAttendees() {
        launch {
            val eventsId = db.eventsDAO().getEnabledEventIds()
            if (eventsId.isEmpty())
                return@launch

            val attendees = db.attendeeDAO().getAttendees(eventsId)

            launch(UI) {
                attendeeAdapter.add(attendees)
            }
        }
    }

}

