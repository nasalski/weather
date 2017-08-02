package com.example.wwwsl.testweather;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;


/**
 * Created by wwwsl on 28.07.2017.
 */

public interface ApiInterface {
    //weather api
    /*@GET("/data/2.5/forecast")
    Call<List<Weather>> getKey(@Query("id") String id, @Query("APPID") String Apikey);

    @GET("/data/2.5/weather")
    Call<List<Weather>> getWeather(@Query("q") String location);*/
    //yahoo weather api
    @GET("/v1/public/yql")
    Call<Resp> getWeather(@Query("q") String query, @Query("format") String format);
}
