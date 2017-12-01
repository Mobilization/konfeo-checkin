package pl.mobilization.konfeo.checkin

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import org.apache.commons.csv.CSVFormat
import org.jsoup.Jsoup
import android.arch.persistence.room.Room
import android.util.Log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import okhttp3.*
import org.apache.commons.csv.CSVRecord
import org.jetbrains.anko.toast
import org.jsoup.select.Elements
import pl.mobilization.konfeo.checkin.entities.Attendee
import pl.mobilization.konfeo.checkin.entities.Event
import pl.mobilization.konfeo.checkin.entities.KonfeoDatabase


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 */
val ACTION_LOGIN = "pl.mobilization.konfeo.checkin.action.login"
val ACTION_EVENTS = "pl.mobilization.konfeo.checkin.action.EVENTS"
val ACTION_USERS = "pl.mobilization.konfeo.checkin.action.USERS"
val ACTION_UPDATE = "pl.mobilization.konfeo.checkin.action.UPDATE"

val LOGIN_PARAM = "pl.mobilization.konfeo.checkin.extra.LOGIN"
val PASSWORD_PARAM = "pl.mobilization.konfeo.checkin.extra.PASSWORD"
val RECEIVER_PARAM = "pl.mobilization.konfeo.checkin.extra.RECEIVER"
val EVENT_URL_PARAM = "pl.mobilization.konfeo.checkin.extra.EVENT_URL"
val INCLUDE_FINISHED = "pl.mobilization.konfeo.checkin.extra.INCLUDE_FINISHED"


val KONFEO_LOGIN_URL = "https://admin.konfeo.com/pl/login"

val RESULT_LOGIN_SUCCESSFUL = 1
val RESULT_LOGIN_FAILED = 2

val RESULT_EVENT_PAID = 4
val RESULT_EVENT_FREE = 8

val RESULT_ATTENDEES_PARSED = 16

val RESULT_UPDATED = 32

val RESULT_PARAM_REASON = "pl.mobilization.konfeo.checkin.result.REASON"
val RESULT_EVENT_NAME = "pl.mobilization.konfeo.checkin.result.NAME"
val RESULT_EVENT_URL = "pl.mobilization.konfeo.checkin.result.URL"

val TAG = KonfeoIntentService::class.java.simpleName

class KonfeoIntentService : IntentService("KonfeoIntentService") {
    lateinit var resultReceiver: ResultReceiver

    private lateinit var okHttpClient: OkHttpClient

    private lateinit var db: KonfeoDatabase

    private lateinit var sharedPrefsCookiePersistor: SharedPrefsCookiePersistor

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        sharedPrefsCookiePersistor = SharedPrefsCookiePersistor(this)
        okHttpClient = OkHttpClient.Builder()
                .cookieJar(MyCookieJar(SetCookieCache(), sharedPrefsCookiePersistor))
                .build()

        db = Room.databaseBuilder(applicationContext,
                KonfeoDatabase::class.java, "konfeo").build()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            resultReceiver = intent.getParcelableExtra(RECEIVER_PARAM)
            if (ACTION_LOGIN.equals(action)) {
                val param1 = intent.getStringExtra(LOGIN_PARAM)
                val param2 = intent.getStringExtra(PASSWORD_PARAM)
                handleActionLogin(param1, param2)
            } else if (ACTION_EVENTS.equals(action)) {
                handleActionFindEvents()
            } else if (ACTION_USERS.equals(action)) {
                handleActionImport()
            } else if (ACTION_UPDATE.equals(action)) {
                handleActionUpdate()
            }
        }
    }

    private fun handleActionUpdate() {
        val enabledEvents = db.eventsDAO().getEnabledEvents()
        val attendeesToUpdate = db.attendeeDAO().getAttendeesToUpdate(enabledEvents.map { it.id })

        if(attendeesToUpdate.isEmpty())
            return

        attendeesToUpdate.groupBy {
            it.checked_in
        }.forEach {
            when (it.key) {
                true -> checkIn(it.value)
                false -> checkOut(it.value)
            }
        }

        launch(UI) {
            toast("Updated ${attendeesToUpdate.size} attendees")
        }
    }

    private fun checkOut(attendees: List<Attendee>) {
        for (attendee in attendees) {
            try {
                getChangeFormResponse(attendee, "accepted");
                resultReceiver.send(RESULT_UPDATED, Bundle())
                db.attendeeDAO().updateAttendees(attendee.copy(needs_update = false))
            } catch (e: Exception) {
                Log.e(TAG, "Cannot update attendee $attendee", e)
            }
        }
    }

    private fun checkIn(attendees: List<Attendee>) {
        for (attendee in attendees) {
            try {
                getChangeFormResponse(attendee, "arrived");
                db.attendeeDAO().updateAttendees(attendee.copy(needs_update = false))
            } catch (e: MismatchedStateException) {
                db.attendeeDAO().updateAttendees(attendee.copy(needs_update = false))
            } catch (e: Exception) {
                Log.e(TAG, "Cannot update attendee $attendee", e)
            }
        }
    }

    private fun getChangeFormResponse(attendee: Attendee, newState: String) {
        val getResponse = okHttpClient.newCall(Request.Builder().url("https://admin.konfeo.com/events/${attendee.event_id}/attendees/${attendee.id}/change_state/edit?new_state=$newState").build()).execute()

        val url = getResponse.request().url()
        val newStateValues = url.queryParameterValues("new_state")

        if (!newStateValues.contains(newState))
            throw MismatchedStateException(newState, newStateValues, url.toString())

        getResponse.body()?.let {
            try {
                val html = it.string()
                val document = Jsoup.parse(html)
                val newForm = document.getElementById("new_form") ?: throw HtmlParseException("new_form", html)

                val authenticityTokens = newForm.getElementsByAttributeValue("name", "authenticity_token")
                val action = newForm.attr("action")

                if (authenticityTokens.size == 0)
                    throw HtmlParseException("authenticity_token", html)

                if (action.isEmpty())
                    throw HtmlParseException("action", html)

                val authenticityToken = authenticityTokens[0]
                val body = FormBody.Builder()
                        .add("form[new_state]", newState)
                        .add("authenticity_token", authenticityToken.`val`())
                        .add("utf8", "\uE29C93")
                        .add("_method", "patch")
                        .add("commit", "Zapisz")
                        .build()

                val postResponse = okHttpClient.newCall(Request.Builder().url("https://admin.konfeo.com$action").post(body).build()).execute()
                Log.d(TAG, "Post response received new url is ${postResponse.request().url()}")
                postResponse.close()
            } finally {
                it.close()
            }

        }
    }

    private fun handleActionImport() {
        val enabledEvents = db.eventsDAO().getEnabledEvents()

        if (enabledEvents.isEmpty()) {
            launch(UI) {
                toast("Enable at least one event")
            }
            startActivity(Intent(this, EventListActivity::class.java))
            return
        }

        val attendeeIdsToUpdate = db.attendeeDAO().getAttendeeIdsToUpdate(enabledEvents.map { it.id })

        enabledEvents.forEach {
            val eventId = it.id
            val response = okHttpClient.newCall(Request.Builder().url("https://admin.konfeo.com/events/$eventId/attendees.csv?format=csv").build()).execute()

            response.body()?.let {
                val attendees = mutableListOf<Attendee>()

                val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(it.charStream())

                records.filter { !attendeeIdsToUpdate.contains(it.get(0).toLong()) }.forEach({
                    importAttendee(it, attendees, eventId)
                })

                db.attendeeDAO().insertAttendees(attendees)

                it.close()
            }
        }

        resultReceiver.send(RESULT_ATTENDEES_PARSED, Bundle())
    }

    /**
     * update needed| outgoing present | incoming present | import
     * 1 | 0 | 0
     * 1 | 1 | 0 -> skip
     * 1 | 0 | 1 -> import
     * 1 | 1 | 0 -> skip
     */
    private fun importAttendee(csvRecord: CSVRecord, attendees: MutableList<Attendee>, eventId: Long) {
        val id = csvRecord.get(0).toLong()

        val firstName = csvRecord.get("Imię")
        val lastName = csvRecord.get("Nazwisko")
        val email = csvRecord.get("E-mail")
        val group = csvRecord.get("Grupa")
        val ticketNumber = csvRecord.get("Number biletu")
        val checkedIn = csvRecord.get("Status").equals("Obecny", ignoreCase = true)

        attendees.add(Attendee(
                id = id,
                first_name = firstName,
                last_name = lastName,
                email = email,
                group = group,
                number = ticketNumber.toLongOrNull(),
                checked_in = checkedIn,
                event_id = eventId
        )
        )
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFindEvents() {
        val response = okHttpClient.newCall(Request.Builder().url("https://admin.konfeo.com/events").build()).execute()
        val events = parseEvents(response)

        db.eventsDAO().insertEvents(events)

        val responseClosed = okHttpClient.newCall(Request.Builder().url("https://admin.konfeo.com/events/closed").build()).execute()
        val closedEvents = parseEvents(responseClosed)

        db.eventsDAO().insertEvents(closedEvents)

        val bundle = Bundle()
        resultReceiver.send(RESULT_EVENT_PAID, bundle)
    }

    private fun parseEvents(response: Response): List<Event> {
        val events = mutableListOf<Event>()
        response.body()?.let {
            val html = it.string()
            val document = Jsoup.parse(html)
            val eventFullViewTable = document.getElementById("event-full-view-table")

            if (eventFullViewTable != null) {
                val elementsByEventPayClass = eventFullViewTable.getElementsByClass("event-pay")
                parseEvent(elementsByEventPayClass, events)

                val elementsByEventFreeClass = eventFullViewTable.getElementsByClass("event-pay")
                parseEvent(elementsByEventFreeClass, events)
            }

            it.close()
        }
        return events
    }

    private fun parseEvent(elementsByEventPayClass: Elements, events: MutableList<Event>) {
        for (eventPay in elementsByEventPayClass) {
            val href = eventPay.attr("href")
            if (href.endsWith("/dashboard")) {
                val id = href.replace(Regex("[^0-9]*"), "")

                events.add(Event(id.toLong(), eventPay.text()))
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionLogin(login: String, password: String) {
        try {
            val getResponse = okHttpClient.newCall(Request.Builder().url(KONFEO_LOGIN_URL).get().build()).execute()

            if (!getResponse.isSuccessful) {
                return sendNotOK("Get response ${getResponse.code()}")
            }

            val url = getResponse.request().url()

            if (url.equals(HttpUrl.Builder().scheme("https").host("admin.konfeo.com").addPathSegment("dashboard").build())) {
                return sendOK("Already logged in")
            }

            getResponse.body()?.let {
                val elementsByAttributeValue = Jsoup.parse(it.string()).getElementsByAttributeValue("name", "authenticity_token")
                val authenticityToken = elementsByAttributeValue.`val`()

                val body = FormBody.Builder()
                        .add("email", login)
                        .add("password", password)
                        .add("authenticity_token", authenticityToken)
                        .build()

                val postResponse = okHttpClient.newCall(Request.Builder().url(KONFEO_LOGIN_URL).post(body).build()).execute()

                val code = postResponse.code()
                if (code !in intArrayOf(200, 302)) {
                    return sendNotOK("Error: Http response code ${code}")
                }

                postResponse.body()?.let {
                    val document = Jsoup.parse(it.string())

                    val elementsByAlertDangerClass = document.getElementsByClass("alert alert-danger")

                    if (elementsByAlertDangerClass.size > 0) {
                        return sendNotOK(elementsByAlertDangerClass[0].text())
                    }

                    val elementsByAlertInfoClass = document.getElementsByClass("alert alert-info")

                    if (elementsByAlertInfoClass.size > 0) {
                        return sendOK(elementsByAlertInfoClass[0].text())
                    }
                    return sendOK(document.title())
                }
            }
            sendNotOK("Parsing page failed")
        } catch (e: Exception) {
            sendNotOK(e.toString())
        }
    }

    private fun sendOK(reason: String) {
        val bundle = Bundle()
        bundle.putString(RESULT_PARAM_REASON, reason)
        resultReceiver.send(RESULT_LOGIN_SUCCESSFUL, bundle)
    }

    private fun sendNotOK(reason: String) {
        val bundle = Bundle()
        bundle.putString(RESULT_PARAM_REASON, reason)
        resultReceiver.send(RESULT_LOGIN_FAILED, bundle)
    }


    companion object {
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionLogin(context: Context, login: String, password: String, receiver: ResultReceiver) {
            val intent = Intent(context, KonfeoIntentService::class.java)
            intent.action = ACTION_LOGIN
            intent.putExtra(LOGIN_PARAM, login)
            intent.putExtra(PASSWORD_PARAM, password)
            intent.putExtra(RECEIVER_PARAM, receiver)
            context.startService(intent)
        }

        fun startActionEvents(context: Context, includeFinished: Boolean, receiver: ResultReceiver) {
            val intent = Intent(context, KonfeoIntentService::class.java)
            intent.action = ACTION_EVENTS
            intent.putExtra(INCLUDE_FINISHED, includeFinished)
            intent.putExtra(RECEIVER_PARAM, receiver)

            context.startService(intent)
        }

        fun startActionImport(context: Context, receiver: ResultReceiver) {
            val intent = Intent(context, KonfeoIntentService::class.java)
            intent.action = ACTION_USERS
            intent.putExtra(RECEIVER_PARAM, receiver)

            context.startService(intent)
        }

        fun startActionUpdate(context: Context, receiver: ResultReceiver) {
            val intent = Intent(context, KonfeoIntentService::class.java)
            intent.action = ACTION_UPDATE
            intent.putExtra(RECEIVER_PARAM, receiver)

            context.startService(intent)
        }
    }
}

class HtmlParseException(tag: String, html: String) : Exception("Failed to extract $tag from $html") {

}

class MismatchedStateException(expected: String, newStateValues: List<String>, url: String) : Exception("Expected state $expected but it was $newStateValues in $url") {

}
