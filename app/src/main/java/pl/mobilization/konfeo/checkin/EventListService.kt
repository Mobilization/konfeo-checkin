package pl.mobilization.konfeo.checkin

import android.app.IntentService
import android.content.Intent


/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 *
 */
class EventListService : IntentService("EventListService") {

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_FOO == action) {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionFindEvents(param1, param2)
            } else if (ACTION_BAZ == action) {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionBaz(param1, param2)
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFindEvents(param1: String, param2: String) {
        // TODO: Handle action Foo
        throw UnsupportedOperationException("Not yet implemented")
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        // TODO: Handle action Baz
        throw UnsupportedOperationException("Not yet implemented")
    }

    companion object {
        // TODO: Rename actions, choose action names that describe tasks that this
        // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
        val ACTION_FOO = "pl.mobilization.konfeo.checkin.action.FOO"
        val ACTION_BAZ = "pl.mobilization.konfeo.checkin.action.BAZ"

        // TODO: Rename parameters
        val EXTRA_PARAM1 = "pl.mobilization.konfeo.checkin.extra.PARAM1"
        val EXTRA_PARAM2 = "pl.mobilization.konfeo.checkin.extra.PARAM2"
    }
}
