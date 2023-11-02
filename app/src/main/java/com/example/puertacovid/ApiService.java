package com.example.puertacovid;




import okhttp3.ResponseBody;


import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("/get_aforo")
    Call<ResponseBody> getAforo();
}
