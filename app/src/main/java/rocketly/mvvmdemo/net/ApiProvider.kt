package rocketly.mvvmdemo.net

import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rocketly.mvvmdemo.BuildConfig
import java.nio.charset.Charset

/**
 * Created by zhuliyuan on 2018/8/15.
 */
object ApiProvider {
    private val mBaseUrl = "https://search.heweather.com/"
    private val mRetrofit: Retrofit by lazy {
        val okHttpBuilder = OkHttpClient.Builder()
                .addInterceptor(getCommonParamsInterceptor())
                .addInterceptor(getBusinessInterceptor())
                .sslSocketFactory(getSSLSocketFactory())
                .hostnameVerifier(getHostnameVerifier())
        if (BuildConfig.DEBUG) {
            okHttpBuilder.addInterceptor(getHttpLoggingInterceptor())
        }


        Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpBuilder.build())
                .build()
    }

    fun <T> create(clazz: Class<T>): T {
        return mRetrofit.create(clazz)
    }

    /**
     * 通用参数
     */
    private fun getCommonParamsInterceptor() = Interceptor {
        val originRequest = it.request()
        val newRequest = originRequest.newBuilder()
//                .header("aaa", "bbb")
                .build()
        it.proceed(newRequest)
    }

    /**
     * 日志
     */
    private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }

    /**
     * 业务逻辑错误返回码处理
     */
    private fun getBusinessInterceptor() = Interceptor {
        val response = it.proceed(it.request())
        val responseBody = response.body()
        responseBody?.let {
            if (it.contentLength() == 0L) return@let
            val UTF8 = Charset.forName("UTF-8")
            val charset = if (it.contentType() == null) UTF8 else it.contentType()?.charset(UTF8)
            val source = it.source()
            source.request(Long.MAX_VALUE)
            val buffer = source.buffer()
            val result = Gson().fromJson(buffer.clone().readString(charset), ApiResult::class.java)
            result.HeWeather6[0].apply {
                if (!isOk()) {
                    throw ApiException(status)
                }
            }
        }
        return@Interceptor response
    }
}