package com.sukinsan.kosh_pi.util

import com.sukinsan.kosh_pi.retrofit.KoshPiApi

interface KoshPiUtil {

    interface OnGetIp {
        fun onServerIpResponse(ip: String)
    }

    interface OnHealth {
        fun onHealthResponse(health: String)
    }

    interface OnPong {
        fun onPongResponse(pong: String)
    }

    fun getServerIp(callback: OnGetIp)

    fun pingServer(ip: String, callback: OnPong)

    fun getServerHealth(ip: String, callback: OnHealth)

}

class KoshPiUtilImpl(private val koshPiApi: KoshPiApi) : KoshPiUtil {

    override fun getServerIp(callback: KoshPiUtil.OnGetIp) {
        Thread {
            val resp = koshPiApi.ip().execute()
            if (resp.isSuccessful) callback.onServerIpResponse(resp.body()!!)
        }.start()
    }

    override fun getServerHealth(ip: String, callback: KoshPiUtil.OnHealth) {
        Thread {
            val resp = koshPiApi.health(ip).execute()
            if (resp.isSuccessful) callback.onHealthResponse(resp.body()!!)
        }.start()
    }

    override fun pingServer(ip: String, callback: KoshPiUtil.OnPong) {
        Thread {
            val resp = koshPiApi.ip().execute()
            if (resp.isSuccessful) callback.onPongResponse(resp.body()!!)
        }.start()
    }
}
