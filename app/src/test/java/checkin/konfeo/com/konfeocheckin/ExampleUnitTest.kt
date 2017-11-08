package checkin.konfeo.com.konfeocheckin

import android.os.Environment
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import pl.mobilization.konfeo.checkin.MyCookieJar

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class ExampleUnitTest {
    @Test
    fun cookies_are_persisted() {
        val sharedPrefsCookiePersistor = SharedPrefsCookiePersistor(RuntimeEnvironment.application)
        val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .cookieJar(MyCookieJar(SetCookieCache(), sharedPrefsCookiePersistor))
                .build()


        val request = Request.Builder().url("https://admin.konfeo.com/pl/login").get().build()

        val execute = okHttpClient.newCall(request).execute()

        assertTrue(sharedPrefsCookiePersistor.loadAll().size > 0)


    }
}
