package pl.mobilization.konfeocheckin

import org.jsoup.nodes.Document

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

/**
 * Created by marekdef on 26.12.17.
 */

interface KonfeoEndpoint {

    @GET("/en/login")
    fun login(): Call<Document>

    @GET("/en/login")
    fun login2(): Call<LoginPage>
}
