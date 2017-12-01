package pl.mobilization.konfeo.checkin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import checkin.konfeo.com.konfeocheckin.R

class ImportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        KonfeoIntentService.startActionImport(this, UsersReceiver())
    }

    inner class UsersReceiver() : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            super.onReceiveResult(resultCode, resultData)

            startActivity(Intent(this@ImportActivity, AttendeesActivity::class.java))
        }

    }
}
