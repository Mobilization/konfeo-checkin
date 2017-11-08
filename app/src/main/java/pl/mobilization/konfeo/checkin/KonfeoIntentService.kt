package pl.mobilization.konfeo.checkin

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.jsoup.Jsoup
import android.arch.persistence.room.Room
import pl.mobilization.konfeo.checkin.entities.Attendee
import pl.mobilization.konfeo.checkin.entities.AttendeeDatabase


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 */
val ACTION_LOGIN = "pl.mobilization.konfeo.checkin.action.login"
val ACTION_EVENTS = "pl.mobilization.konfeo.checkin.action.EVENTS"
val ACTION_USERS = "pl.mobilization.konfeo.checkin.action.USERS"

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

val RESULT_PARAM_REASON = "pl.mobilization.konfeo.checkin.result.REASON"
val RESULT_EVENT_NAME = "pl.mobilization.konfeo.checkin.result.NAME"
val RESULT_EVENT_URL = "pl.mobilization.konfeo.checkin.result.URL"

val COOKIE_MAP = mutableMapOf<String, String>()

class KonfeoIntentService : IntentService("KonfeoIntentService") {
    lateinit var resultReceiver: ResultReceiver


    private lateinit var okHttpClient: OkHttpClient

    private lateinit var db : AttendeeDatabase

    private lateinit var sharedPrefsCookiePersistor: SharedPrefsCookiePersistor

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        sharedPrefsCookiePersistor = SharedPrefsCookiePersistor(this)
        okHttpClient = OkHttpClient.Builder()
                .cookieJar(MyCookieJar(SetCookieCache(), sharedPrefsCookiePersistor))
                .build()

        db = Room.databaseBuilder(applicationContext,
                AttendeeDatabase::class.java, "attendees").build()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            resultReceiver = intent.getParcelableExtra<ResultReceiver>(RECEIVER_PARAM)
            if (ACTION_LOGIN.equals(action)) {
                val param1 = intent.getStringExtra(LOGIN_PARAM)
                val param2 = intent.getStringExtra(PASSWORD_PARAM)
                handleActionLogin(param1, param2)
            } else if (ACTION_EVENTS.equals(action)) {
                val includeFinished = intent.getBooleanExtra(INCLUDE_FINISHED, false)
                handleActionFindEvents(includeFinished)
            } else if (ACTION_USERS.equals(action)) {
                val url = intent.getStringExtra(EVENT_URL_PARAM)

                handleActionUsers(url)
            }
        }
    }

    private fun handleActionUsers(url: String) {

        val eventId = url.substring(8, url.length - 10)


        val response = okHttpClient.newCall(Request.Builder().url("https://admin.konfeo.com/events/$eventId/attendees.csv?format=csv").build()).execute()


        response.body()?.let {
            val attendees = mutableListOf<Attendee>()

            val records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(it.charStream())
            for (record: CSVRecord in records) {

                attendees.add(Attendee(
                        id = record.get(0).toLong(),
                        first_name = record.get("Imię"),
                        last_name = record.get("Nazwisko"),
                        email = record.get("E-mail"),
                        group = record.get("Grupa"),
                        number = record.get("Number biletu").toLongOrNull()
                ))
            }

            db.attendeeDAO().insertAttendees(*attendees.toTypedArray())
        }

    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFindEvents(includeFinished: Boolean) {
        val response = okHttpClient.newCall(Request.Builder().url("https://admin.konfeo.com/events/closed").build()).execute()

        response.body()?.let {
            val events = Jsoup.parse(it.string())
            val eventFullViewTable = events.getElementById("event-full-view-table")

            val elementsByEventPayClass = eventFullViewTable.getElementsByClass("event-pay")

            for (eventPay in elementsByEventPayClass) {

                val href = eventPay.attr("href")
                if (href.endsWith("/dashboard")) {
                    val bundle = Bundle()
                    bundle.putString(RESULT_EVENT_NAME, eventPay.text())
                    bundle.putString(RESULT_EVENT_URL, href)
                    resultReceiver.send(RESULT_EVENT_PAID, bundle)
                }
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


        fun startActionUsers(context: Context, eventUrl: String, receiver: ResultReceiver) {
            val intent = Intent(context, KonfeoIntentService::class.java)
            intent.action = ACTION_USERS
            intent.putExtra(EVENT_URL_PARAM, eventUrl)
            intent.putExtra(RECEIVER_PARAM, receiver)

            context.startService(intent)
        }
    }
}
