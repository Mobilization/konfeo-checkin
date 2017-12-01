package pl.mobilization.konfeo.checkin.adapters

import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import checkin.konfeo.com.konfeocheckin.R.layout.event_list_item
import pl.mobilization.konfeo.checkin.entities.Event

import kotlinx.android.synthetic.main.event_list_item.textViewEvent
import kotlinx.android.synthetic.main.event_list_item.checkBoxEnabled
import kotlinx.android.synthetic.main.event_list_item.view.*
import pl.mobilization.konfeo.checkin.AttendeesActivity
import pl.mobilization.konfeo.checkin.EVENT_URL_PARAM
import pl.mobilization.konfeo.checkin.EventCheckedListener
import pl.mobilization.konfeo.checkin.EventListActivity
import pl.mobilization.konfeo.checkin.entities.KonfeoDatabase

class EventHolder(val eventsAdapter: EventsAdapter, itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(event: Event) {
        with(itemView) {
            textViewEvent.text = event.name
            checkBoxEnabled.isChecked = event.enabled
            checkBoxEnabled.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener {
                compoundButton, checked -> eventsAdapter.onItemChecked(event.id, checked) })
        }
    }
}

class EventsAdapter(val eventsCheckedListener: EventCheckedListener) : RecyclerView.Adapter<EventHolder>() {
    val events: MutableList<Event> = mutableListOf()

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder = EventHolder(this, LayoutInflater.from(parent.context).inflate(event_list_item, parent, false))

    fun clearAndAdd(eventList: List<Event>) {
        this.events.clear()
        this.events.addAll(eventList)
        notifyDataSetChanged()
    }

    fun onItemChecked(id: Long, checked: Boolean) {
        eventsCheckedListener.onEventChecked(id, checked)
    }
}