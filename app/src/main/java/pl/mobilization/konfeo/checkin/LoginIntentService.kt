package pl.mobilization.konfeo.checkin

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.os.ResultReceiver
import org.jsoup.Connection

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

val RESULT_PARAM_REASON = "pl.mobilization.konfeo.checkin.extra.REASON"

class LoginIntentService : IntentService("LoginIntentService") {
    lateinit var parcelableExtra : ResultReceiver

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_LOGIN == action) {
                val param1 = intent.getStringExtra(LOGIN_PARAM)
                val param2 = intent.getStringExtra(PASSWORD_PARAM)
                parcelableExtra = intent.getParcelableExtra<ResultReceiver>(RECEIVER_PARAM)
                handleActionLogin(param1, param2)
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

            val elementsByAttributeValue = response.parse().getElementsByAttributeValue("name", "authenticity_token")

            val authenticityToken = elementsByAttributeValue.`val`()

            val postResponse = org.jsoup.Jsoup.connect(KONFEO_LOGIN_URL)
                    .cookies(cookies)
                    .data("email", login)
                    .data("password", password)
                    .data("authenticity_token", authenticityToken)
                    .method(Connection.Method.POST)
                    .execute()

            if(postResponse.statusCode() !in intArrayOf(200, 302))
                sendNotOK("Error: Http response code ${postResponse.statusCode()}")

            val elementsByClass = postResponse.parse().getElementsByClass("alert alert-danger")

            if(elementsByClass.size > 1)
                sendNotOK(elementsByClass[0].data())

            sendOK()
        }
        catch (e: Exception) {
            sendNotOK(e.toString())
        }
    }

    private fun sendOK() {
        val bundle = Bundle()
        parcelableExtra.send(RESULT_LOGIN_SUCCESSFUL, bundle)
    }

    private fun sendNotOK(reason: String) {
        val bundle = Bundle()
        bundle.putString(RESULT_PARAM_REASON, reason)
        parcelableExtra.send(RESULT_LOGIN_FAILED, bundle)
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

    }
}
