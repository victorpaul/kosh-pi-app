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

    interface OnPong {
        fun onPongResponse(pong: String)
    }

    fun getIp(): String

    fun isExpiredIp(callback: () -> Unit)

    fun pingServer(callback: OnPong)

    fun getServerHealth(callback: OnHealth)

    fun interceptor(request: Call<String>,
                    success: (call: Call<String>, resp: Response<String>) -> Unit,
                    fail: (call: Call<String>, t: Throwable) -> Unit)
}

class KoshPiUtilImpl(private val koshPiApi: KoshPiApi) : KoshPiUtil {

    private val TAG: String = KoshPiUtil::class.java.simpleName
    private var cachedIp: String = ""

    override fun getIp(): String {
        if (cachedIp.isEmpty()) {
            cachedIp = koshPiApi.ip().execute().body()!!
        }
        return cachedIp
    }


    override fun interceptor(request: Call<String>, success: (call: Call<String>, response: Response<String>) -> Unit, fail: (call: Call<String>, t: Throwable) -> Unit) {
        request.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                //todo check kosh-pi headers
                when (response.isSuccessful) {
                    true -> success.invoke(call, response)
                    false -> fail.invoke(call, Throwable("Bad response from server"))
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                fail.invoke(call, t)

                //isExpiredIp { request.enqueue(this) } //todo repeat this call if it was ip problem
            }
        })
    }

    override fun isExpiredIp(callback: () -> Unit) {
        Log.i(TAG, "isExpiredIp")
        koshPiApi.ip().enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                val ip = response.body()
                Log.i(TAG, "isExpiredIp $ip and $cachedIp")
                if (!ip.isNullOrEmpty() && !ip.equals(cachedIp)) {
                    Log.i(TAG, "let's say that there is new cached ip")
                    cachedIp = ip!!
                    callback.invoke()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    override fun getServerHealth(cllbk: KoshPiUtil.OnHealth) {
        Log.i(TAG, "getServerHealth")
        interceptor(koshPiApi.health(getIp()),
                { c, r -> cllbk.onHealthResponse(r.body()!!) },
                { c, t -> isExpiredIp { getServerHealth(cllbk) } })
    }

    override fun pingServer(cllbk: KoshPiUtil.OnPong) {
        Log.i(TAG, "pingServer")
        interceptor(koshPiApi.ping(getIp()),
                { c, r -> cllbk.onPongResponse(r.body()!!) },
                { c, t -> isExpiredIp { pingServer(cllbk) } })
    }
}
