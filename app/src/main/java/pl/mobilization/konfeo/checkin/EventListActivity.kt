package pl.mobilization.konfeo.checkin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import checkin.konfeo.com.konfeocheckin.R
import kotlinx.android.synthetic.main.activity_event_list.*
import kotlinx.android.synthetic.main.list_item.view.*

class EventListActivity : AppCompatActivity() {
    val eventAdapter = EventAdapter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        recyclerViewEvents.adapter = eventAdapter
        recyclerViewEvents.layoutManager = LinearLayoutManager(this)
        recyclerViewEvents.itemAnimator = DefaultItemAnimator()

        pl.mobilization.konfeo.checkin.LoginIntentService.Companion.startActionEvents(this,true , EventResultReceiver())
    }

    override fun onResume() {
        super.onResume()
    }

    internal inner class EventResultReceiver : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            Log.d(EventResultReceiver::class.java.simpleName, "onReceiveResult($resultCode, $resultData)")
            when(resultCode) {
                RESULT_EVENT_PAID -> {
                    resultData?.let {
                        eventAdapter.addEvent(Event(it.getString(RESULT_EVENT_NAME) , it.getString(RESULT_EVENT_URL)))
                    }
                }
            }
        }

    }
}



data class Event(val text: String, val url: String)

class EventHolder(private val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
    val TAG = EventHolder::class.java.simpleName.toString()
    private val textView : TextView
    init {
        textView = view.textViewEvent
        view.setOnClickListener(this)
    }
    fun setText(text: String) {
        textView.text = text
    }

    override fun onClick(view: View) {
        Log.d(TAG, "onClick")
    }

    var url: String = ""
}

class EventAdapter : RecyclerView.Adapter<EventHolder>() {
    private val events = ArrayList<Event>()

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        val event = events[position]
        holder.setText(event.text)
        holder.url = event.url
    }

    override fun getItemCount() = events.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {

        val textView = LayoutInflater.from(parent.context).inflate(R.layout.list_item, null)
        return EventHolder(textView)
    }

    fun addEvent(event: Event) {
        events.add(event)
        notifyDataSetChanged()
    }

}
