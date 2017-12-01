package pl.mobilization.konfeo.checkin.adapters

import android.arch.persistence.room.Room
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import checkin.konfeo.com.konfeocheckin.R.layout.attendee_list_item
import org.apache.commons.lang3.StringUtils
import pl.mobilization.konfeo.checkin.entities.Attendee
import java.util.regex.Pattern

import kotlinx.android.synthetic.main.attendee_list_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import pl.mobilization.konfeo.checkin.entities.KonfeoDatabase

import org.jetbrains.anko.toast
import pl.mobilization.konfeo.checkin.KonfeoIntentService


class AttendeeHolder(val view: View) : RecyclerView.ViewHolder(view) {


    fun bind(attendee: Attendee, update : (Attendee) -> Unit) {
        with(itemView) {
            checkBoxPresent.setOnCheckedChangeListener(null)
            textViewName.text = "${attendee.first_name} ${attendee.last_name}"
            textViewEmail.text = attendee.email
            textViewGroup.text = attendee.group
            checkBoxPresent.isChecked = attendee.checked_in
            checkBoxPresent.setOnCheckedChangeListener { compoundButton, value -> attendee.checked_in = value ; update(attendee)}
        }
    }
}

class AttendeeAdapter(val context: Context) : RecyclerView.Adapter<AttendeeHolder>() {
    private val attendees = mutableMapOf<Pair<Long, Long>, Attendee>()
    private var filtered: List<Attendee> = arrayListOf<Attendee>()

    private val db: KonfeoDatabase = Room.databaseBuilder(context,
            KonfeoDatabase::class.java, "konfeo").build()

    private fun updateAttendee(attendee: Attendee) {
        attendee.needs_update = true
        launch {
            db.attendeeDAO().updateAttendees(attendee)
            delay(5000)
            KonfeoIntentService.startActionUpdate(this@AttendeeAdapter.context, AttendeeUpdateReceiver())
        }
    }

    inner internal class AttendeeUpdateReceiver() : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            launch(UI) {
                this@AttendeeAdapter.context.toast("Updated")
            }
        }
    }

    override fun onBindViewHolder(holder: AttendeeHolder, position: Int) {
        holder.bind(filtered[position], this::updateAttendee)
    }

    override fun getItemCount(): Int = filtered.count()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendeeHolder {
        val view = LayoutInflater.from(parent.context).inflate(attendee_list_item, parent, false)
        return AttendeeHolder(view)
    }

    fun add(attendees: List<Attendee>) {
        for (attendee in attendees) {
            this.attendees.put(Pair(attendee.id, attendee.event_id), attendee)
        }
        requery()
    }

    private fun requery() {
        if (filters.isEmpty()) {
            filtered = attendees.values.toList()
        } else {
            filtered = attendees.values.filter {
                matches_all(it)
            }.toList()
        }
        notifyDataSetChanged()
    }

    private fun matches_all(it: Attendee): Boolean {
        for (filter in filters) {
            if (!it.matches(filter))
                return false
        }
        return true
    }

    private var filters: List<String> = arrayListOf<String>()

    var filter: String = ""
        set(value) {
            field = StringUtils.stripAccents(value.toLowerCase())

            filters = field.split(Pattern.compile("\\s+"))
            requery()
        }
}