package pl.mobilization.konfeocheckin;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by marekdef on 26.12.17.
 */

public interface KonfeoEndpoint {

    @GET("/en/login")
    Call<ResponseBody> login();
}
