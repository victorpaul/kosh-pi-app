package com.sukinsan.kosh_pi.util

import android.util.Log
import com.sukinsan.kosh_pi.retrofit.KoshPiApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface KoshPiUtil {

    interface OnHealth {
        fun onHealthResponse(health: String)
    }

    fun getPiIp(callback: (ip: String) -> Unit)

    fun refreshIp(ip: String, callback: () -> Unit)

    fun pingServer(cllbk: (pong: String) -> Unit)

    fun getServerHealth(callback: OnHealth)

    fun handlePiRequest(request: Call<String>,
                        success: (call: Call<String>, resp: Response<String>) -> Unit,
                        refresh: () -> Unit)
}

class KoshPiUtilImpl(private val koshPiApi: KoshPiApi) : KoshPiUtil {

    private val TAG: String = KoshPiUtil::class.java.simpleName
    private var cachedIp: String = ""

    private fun log(msg: String) {
        Log.i(TAG, msg)
    }

    /**
     * get ip address of server from cache or amazon
     */
    override fun getPiIp(callback: (ip: String) -> Unit) {
        when (cachedIp.isNullOrEmpty()) {
            true -> koshPiApi.ip().enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        log("Ip from amazon is " + response.body())
                        callback.invoke(response.body()!!)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {}
            })
            false -> callback.invoke(cachedIp)
        }
    }

    /**
     * will callback only if there is new ip
     */
    override fun refreshIp(ip: String, callback: () -> Unit) {
        log("Let's check if '$ip' is wrong")
        koshPiApi.ip().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val legalIp = response.body()
                if (!legalIp.isNullOrEmpty() && !legalIp.equals(ip)) {
                    log("'$ip' was wrong, request can be repeated with new one '$legalIp'")
                    cachedIp = legalIp!!
                    callback.invoke()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {}
        })
    }

    /**
     * handle requests/reponses/server ip from/to server
     */
    override fun handlePiRequest(request: Call<String>,
                                 success: (call: Call<String>, response: Response<String>) -> Unit,
                                 refresh: () -> Unit) {
        log(request.request().method() + " " + request.request().url().toString())
        request.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val security = response.headers().get("last-page")
                if ("kosh was here".equals(security)) {
                    log("resp: ${response.body()}")
                    success.invoke(call, response)
                } else {
                    log("Wrong ip, no header from kosh-pi")
                    refresh.invoke()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                refresh.invoke()
            }
        })
    }

    override fun getServerHealth(cllbk: KoshPiUtil.OnHealth) {
        getPiIp { ip ->
            handlePiRequest(koshPiApi.health(ip),
                    { c, r -> cllbk.onHealthResponse(r.body()!!) },
                    { refreshIp(ip) { getServerHealth(cllbk) } })
        }
    }

    override fun pingServer(cllbk: (pong: String) -> Unit) {
        getPiIp { ip ->
            handlePiRequest(koshPiApi.ping(ip),
                    { c, r -> cllbk.invoke(r.body()!!) },
                    { refreshIp(ip) { pingServer(cllbk) } })
        }
    }
}
