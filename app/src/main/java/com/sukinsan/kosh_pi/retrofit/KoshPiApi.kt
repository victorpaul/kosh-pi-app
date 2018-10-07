package com.sukinsan.kosh_pi.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface KoshPiApi {

    @GET("https://s3-eu-west-1.amazonaws.com/kosh-dns/v1/dns/object/homepi.txt")
    fun ip(): Call<String>

    @GET("http://{ip}/ping")
    fun ping(@Path("ip") ip: String): Call<String>

    @GET("http://{ip}/health")
    fun health(@Path("ip") ip: String): Call<String>

}