package pl.mobilization.konfeocheckin

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import retrofit2.Retrofit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun canAccessLoginPage() {
        val endpoint = Retrofit.Builder().baseUrl("https://admin.konfeo.com").build().create(KonfeoEndpoint::class.java)

        val login = endpoint.login()


        val execute = login.execute()

        val body = execute.body()

        assertTrue(execute.isSuccessful)
        assertFalse(execute.raw().isRedirect)


    }
}
