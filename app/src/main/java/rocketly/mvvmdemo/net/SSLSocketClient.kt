package rocketly.mvvmdemo.net

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Created by zhuliyuan on 2018/8/16.
 */

//获取这个SSLSocketFactory
fun getSSLSocketFactory(): SSLSocketFactory {
    try {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, getTrustManager(), SecureRandom())
        return sslContext.socketFactory
    } catch (e: Exception) {
        throw RuntimeException(e)
    }

}

//获取TrustManager
private fun getTrustManager(): Array<TrustManager> {
    return arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf<X509Certificate>()

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
    })
}

//获取HostnameVerifier
fun getHostnameVerifier(): HostnameVerifier {
    return HostnameVerifier { _, _ -> true }
}

