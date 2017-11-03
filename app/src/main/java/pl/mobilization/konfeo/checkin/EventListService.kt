package pl.mobilization.konfeo.checkin

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import org.jsoup.Connection
import org.jsoup.Jsoup


val ACTION_EVENTS = "pl.mobilization.konfeo.checkin.action.FOO"

val INCLUDE_FINISHED = "pl.mobilization.konfeo.checkin.extra.PARAM1"

val RESULT_EVENT_PAID = 0

val RESULT_EVENT_NAME = "pl.mobilization.konfeo.checkin.result.NAME"
val RESULT_EVENT_URL = "pl.mobilization.konfeo.checkin.result.URL"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 *
 */
class EventListService : IntentService("EventListService") {
    lateinit var resultReceiver: ResultReceiver

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_EVENTS == action) {
                val includeFinished = intent.getBooleanExtra(INCLUDE_FINISHED, false)
                resultReceiver = intent.getParcelableExtra<ResultReceiver>(RECEIVER_PARAM)

                handleActionFindEvents(includeFinished)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFindEvents(includeFinished: Boolean) {

        val response = Jsoup.connect("https://admin.konfeo.com/events/closed")
                .cookies(COOKIE_MAP)
                .method(Connection.Method.GET)
                .execute()

        COOKIE_MAP.putAll(response.cookies())

        val events = response.parse()

        val eventFullViewTable = events.getElementById("event-full-view-table")

        val elementsByEventPayClass = eventFullViewTable.getElementsByClass("event-pay")

        for (eventPay in elementsByEventPayClass) {

            val href = eventPay.attr("href")
            if(href.endsWith("/dashboard")) {
                val bundle = Bundle()
                bundle.putString(RESULT_EVENT_NAME, eventPay.text())
                bundle.putString(RESULT_EVENT_URL, href)
                resultReceiver.send(RESULT_EVENT_PAID, bundle)
            }
        }
    }

    companion object {


        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionEvents(context: Context, includeFinished: Boolean, receiver: ResultReceiver) {
            val intent = Intent(context, EventListService::class.java)
            intent.action = ACTION_EVENTS
            intent.putExtra(INCLUDE_FINISHED, includeFinished)
            intent.putExtra(RECEIVER_PARAM, receiver)

            context.startService(intent)
        }

    }
}
