package com.sukinsan.kosh_pi.dagger2

import com.sukinsan.kosh_pi.retrofit.KoshPiApi
import com.sukinsan.kosh_pi.util.KoshPiUtil
import com.sukinsan.kosh_pi.util.KoshPiUtilImpl
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


@Module
class AppModule {

    @Provides
    fun retrofit(): Retrofit = Retrofit
            .Builder()
            .baseUrl("http://sukinsan.com")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    fun koshPiApi(retrofit: Retrofit): KoshPiApi = retrofit.create<KoshPiApi>(KoshPiApi::class.java)

    @Provides
    fun koshPiUtil(koshPiApi: KoshPiApi): KoshPiUtil = KoshPiUtilImpl(koshPiApi)

}