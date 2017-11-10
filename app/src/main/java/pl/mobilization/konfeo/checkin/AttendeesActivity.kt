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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import checkin.konfeo.com.konfeocheckin.R
import kotlinx.android.synthetic.main.activity_attendees.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import pl.mobilization.konfeo.checkin.entities.Attendee
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
                AttendeeDatabase::class.java, "attendees").build()

        setContentView(R.layout.activity_attendees)
        val event_url = intent.getStringExtra(EVENT_URL_PARAM)
        event_url?.let {
            pl.mobilization.konfeo.checkin.KonfeoIntentService.Companion.startActionUsers(this, it, AttendeesReceiver())
        }

        recyclerViewAttendees.itemAnimator = DefaultItemAnimator()
        recyclerViewAttendees.layoutManager = LinearLayoutManager(this)

        attendeeAdapter = AttendeeAdapter()
        recyclerViewAttendees.adapter = attendeeAdapter

        launch {
            val attendees = db.attendeeDAO().getAttendees()

            launch(UI) {
//                val attendee = Attendee(Long.MAX_VALUE, "Marek", "Defeciński", "marekdef@tlen.pl", "Organizer", Integer.MAX_VALUE.toLong(), false)

                attendeeAdapter.add(attendees)
            }
        }

//        handleSearch(intent)
    }

    private fun handleSearch(intent: Intent?) {
        intent?.let {
            if (Intent.ACTION_SEARCH == intent.action) {
                val query = intent.getStringExtra(SearchManager.QUERY)
                doMySearch(query)
            }
        }

    }

    private fun doMySearch(query: String) {
//        attendeeAdapter.filter = query
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//
//        setIntent(intent)
////        handleSearch(intent)
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.app_bar_search).actionView as SearchView
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.setIconifiedByDefault(false) // Do not iconify the widget; expand it by default


        return super.onCreateOptionsMenu(menu)
    }

    internal class AttendeesReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            super.onReceiveResult(resultCode, resultData)
        }
    }
}

class AttendeeHolder(val view : View) : RecyclerView.ViewHolder(view) {
    var text1: TextView
    var text2: TextView

    init {
        text1 = view.findViewById<TextView>(android.R.id.text1)
        text2 = view.findViewById<TextView>(android.R.id.text2)
    }

    fun bind(attendee: Attendee) {
        text1.text = "${attendee.first_name} ${attendee.last_name}"
        text2.text = attendee.email
    }

}

class AttendeeAdapter : RecyclerView.Adapter<AttendeeHolder>() {
    private val attendees = mutableListOf<Attendee>()

    override fun onBindViewHolder(holder: AttendeeHolder, position: Int) {
        holder.bind(attendees[position])
    }

    override fun getItemCount(): Int = attendees.count()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.two_line_list_item, parent, false)
        return AttendeeHolder(view)
    }

    fun add(attendees: List<Attendee>) {
        this.attendees.addAll(attendees)
        notifyDataSetChanged()
    }
}