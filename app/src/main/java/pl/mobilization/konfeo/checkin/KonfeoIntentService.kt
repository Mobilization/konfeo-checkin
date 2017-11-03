package pl.mobilization.konfeo.checkin

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import org.jsoup.Connection
import org.jsoup.Jsoup

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 */
val ACTION_LOGIN = "pl.mobilization.konfeo.checkin.action.login"

val LOGIN_PARAM = "pl.mobilization.konfeo.checkin.extra.LOGIN"
val PASSWORD_PARAM = "pl.mobilization.konfeo.checkin.extra.PASSWORD"
val RECEIVER_PARAM = "pl.mobilization.konfeo.checkin.extra.RECEIVER"

val KONFEO_LOGIN_URL = "https://admin.konfeo.com/pl/login"

val RESULT_LOGIN_SUCCESSFUL = 1
val RESULT_LOGIN_FAILED = 2

val RESULT_PARAM_REASON = "pl.mobilization.konfeo.checkin.result.REASON"

val COOKIE_MAP = mutableMapOf<String, String>()
val HEADERS_MAP = mutableMapOf<String, String>()

val ACTION_EVENTS = "pl.mobilization.konfeo.checkin.action.FOO"

val INCLUDE_FINISHED = "pl.mobilization.konfeo.checkin.extra.INCLUDE_FINISHED"

val RESULT_EVENT_PAID = 0

val RESULT_EVENT_NAME = "pl.mobilization.konfeo.checkin.result.NAME"
val RESULT_EVENT_URL = "pl.mobilization.konfeo.checkin.result.URL"

class LoginIntentService : IntentService("LoginIntentService") {
    lateinit var resultReceiver: ResultReceiver

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_LOGIN.equals(action)) {
                val param1 = intent.getStringExtra(LOGIN_PARAM)
                val param2 = intent.getStringExtra(PASSWORD_PARAM)
                resultReceiver = intent.getParcelableExtra<ResultReceiver>(RECEIVER_PARAM)
                handleActionLogin(param1, param2)
            } else if (ACTION_EVENTS.equals(action)) {
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

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionLogin(login: String, password: String) {
        try {
            val response = org.jsoup.Jsoup.connect(KONFEO_LOGIN_URL)
                    .method(Connection.Method.GET)
                    .execute()

            val cookies = response.cookies()

            COOKIE_MAP.putAll(cookies)

            val elementsByAttributeValue = response.parse().getElementsByAttributeValue("name", "authenticity_token")

            val authenticityToken = elementsByAttributeValue.`val`()

            val postResponse = org.jsoup.Jsoup.connect(KONFEO_LOGIN_URL)
                    .cookies(cookies)
                    .data("email", login)
                    .data("password", password)
                    .data("authenticity_token", authenticityToken)
                    .method(Connection.Method.POST)
                    .execute()

            if(postResponse.statusCode() !in intArrayOf(200, 302)) {
                sendNotOK("Error: Http response code ${postResponse.statusCode()}")
                return
            }

            COOKIE_MAP.putAll(postResponse.cookies())

            val document = postResponse.parse()
            val elementsByAlertDangerClass = document.getElementsByClass("alert alert-danger")

            if(elementsByAlertDangerClass.size > 0) {
                sendNotOK(elementsByAlertDangerClass[0].text())
                return
            }

            val elementsByAlertInfoClass = document.getElementsByClass("alert alert-info")

            if(elementsByAlertInfoClass.size > 0) {
                sendOK(elementsByAlertInfoClass[0].text())
                return
            }

            sendOK(document.title())
        }
        catch (e: Exception) {
            sendNotOK(e.toString())
        }
    }

    private fun sendOK(reason : String) {
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
            val intent = Intent(context, LoginIntentService::class.java)
            intent.action = ACTION_LOGIN
            intent.putExtra(LOGIN_PARAM, login)
            intent.putExtra(PASSWORD_PARAM, password)
            intent.putExtra(RECEIVER_PARAM, receiver)
            context.startService(intent)
        }

        fun startActionEvents(context: Context, includeFinished: Boolean, receiver: ResultReceiver) {
            val intent = Intent(context, LoginIntentService::class.java)
            intent.action = ACTION_EVENTS
            intent.putExtra(INCLUDE_FINISHED, includeFinished)
            intent.putExtra(RECEIVER_PARAM, receiver)

            context.startService(intent)
        }

    }
}
