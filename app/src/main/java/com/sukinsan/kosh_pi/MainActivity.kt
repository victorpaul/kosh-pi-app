package com.sukinsan.kosh_pi

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.sukinsan.kosh_pi.dagger2.DaggerAppComponent
import com.sukinsan.kosh_pi.util.KoshPiUtil
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(), KoshPiUtil.OnHealth {

    val TAG: String = MainActivity::class.java.simpleName

    @Inject
    lateinit var koshPiApi: KoshPiUtil

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                loadImage()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                loadImage()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                loadImage()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerAppComponent.create().bean(this)
        setContentView(R.layout.activity_main)

        koshPiApi.pingServer { pong -> Log.i(TAG, "onHealthResponse $pong") }
        koshPiApi.getServerHealth(this)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onHealthResponse(health: String) {
        Log.i(TAG, "onHealthResponse $health")
    }

    fun loadImage() {
        koshPiApi.getPiIp {
            Glide.with(this)
                    .load("http://$it/api/camera")
                    .apply(RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true))
                    .into(img_home)
            Toast.makeText(this, "update", Toast.LENGTH_SHORT).show()
        }
    }
}
