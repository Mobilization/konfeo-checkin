package pl.mobilization.konfeocheckin

import org.jsoup.nodes.Document
import org.junit.Test

import org.junit.Assert.*
import pl.droidsonroids.retrofit2.JspoonConverterFactory
import retrofit2.Retrofit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun canAccessLoginPage() {
        val endpoint = Retrofit.Builder()
                .baseUrl("https://admin.konfeo.com")
                .addConverterFactory(JsoupConverterFactory())
                .build().create(KonfeoEndpoint::class.java)

        val login = endpoint.login()

        val execute = login.execute()

        val body: Document? = execute.body()

        assertTrue(execute.isSuccessful)
        assertFalse(execute.raw().isRedirect)
        assertNotNull(body)


            val title = body!!.getElementsByClass("form-title")
            assertNotNull(title)
            assertEquals("Log in", title!!.text())

    }

    @Test
    fun canParseLoginPage() {
        val endpoint = Retrofit.Builder()
                .baseUrl("https://admin.konfeo.com")
                .addConverterFactory(JspoonConverterFactory.create())
                .build().create(KonfeoEndpoint::class.java)

        val login = endpoint.login2()

        val execute = login.execute()

        val loginPage = execute.body()

        assertTrue(execute.isSuccessful)
        assertFalse(execute.raw().isRedirect)
        assertNotNull(loginPage)


        val authenticity_token = loginPage!!.authenticity_token
        assertNotNull(authenticity_token)
        assertTrue(authenticity_token.length > 0)

    }
}
