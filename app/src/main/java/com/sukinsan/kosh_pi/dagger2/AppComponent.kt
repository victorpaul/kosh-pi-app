package com.sukinsan.kosh_pi.dagger2

import com.sukinsan.kosh_pi.MainActivity
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
    fun bean(app: MainActivity)
}