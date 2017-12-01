package pl.mobilization.konfeo.checkin

import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import checkin.konfeo.com.konfeocheckin.R
import kotlinx.android.synthetic.main.activity_event_list.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.toast
import pl.mobilization.konfeo.checkin.adapters.EventsAdapter
import pl.mobilization.konfeo.checkin.entities.KonfeoDatabase

class EventListActivity : AppCompatActivity(), EventCheckedListener {
    val eventAdapter = EventsAdapter(this)

    lateinit var db: KonfeoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        recyclerViewEvents.adapter = eventAdapter
        recyclerViewEvents.layoutManager = LinearLayoutManager(this)
        recyclerViewEvents.itemAnimator = DefaultItemAnimator()

        db = Room.databaseBuilder(applicationContext,
                KonfeoDatabase::class.java, "konfeo").build()

        resetAdapter { }


        buttonAttendees.setOnClickListener {
            launchActivityIfHasEvents(AttendeesActivity::class.java)
        }

        buttonImport.setOnClickListener {
            launchActivityIfHasEvents(ImportActivity::class.java)
        }

        pl.mobilization.konfeo.checkin.KonfeoIntentService.Companion.startActionEvents(this, true, EventResultReceiver())
    }

    private fun launchActivityIfHasEvents(clazz: Class<out Activity>) {
        launch {
            val enabledCount = db.eventsDAO().countEnabledEvents();

            if (enabledCount == 0L) {
                launch(UI) {
                    toast("Enable at least one event")
                }
                return@launch
            }

            startActivity(Intent(this@EventListActivity, clazz))
        }
    }

    override fun onEventChecked(id: Long, checked: Boolean) {
        resetAdapter({ db.eventsDAO().checkEvent(id, checked) })
    }

    internal inner class EventResultReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            Log.d(EventResultReceiver::class.java.simpleName, "onReceiveResult($resultCode, $resultData)")
            when (resultCode) {
                RESULT_EVENT_PAID -> {
                    resetAdapter { }
                }
            }
        }

    }

    private fun resetAdapter(function: () -> Unit) {
        launch {
            function()
            val events = db.eventsDAO().getEvents()
            launch(UI) {
                eventAdapter.clearAndAdd(events)
            }
        }
    }
}

interface EventCheckedListener {
    fun onEventChecked(id: Long, checked: Boolean)
}
