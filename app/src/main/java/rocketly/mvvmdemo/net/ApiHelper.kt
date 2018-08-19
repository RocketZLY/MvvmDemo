package rocketly.mvvmdemo.net

import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Created by zhuliyuan on 2018/8/15.
 */

/**
 * 和风天气认证key
 */
const val AUTHKEY = "ff9f669cde394e02b0273f3449304402"

/**
 * 根据服务端返回的status转为msg
 */
fun status2Msg(status: String) =
        when (status) {
            "invalid key" -> "错误的key，请检查你的key是否输入以及是否输入有误"
            "unknown location" -> "未知或错误城市/地区"
            "no data for this location" -> "该城市/地区没有你所请求的数据"
            "no more requests" -> "超过访问次数，需要等到当月最后一天24点（免费用户为当天24点）后进行访问次数的重置或升级你的访问量"
            "param invalid" -> "参数错误，请检查你传递的参数是否正确"
            "too fast" -> "超过限定的QPM，请参考QPM说明"
            "dead" -> "无响应或超时，接口服务异常请联系我们"
            "permission denied" -> "无访问权限，你没有购买你所访问的这部分服务"
            "sign error" -> "签名错误，请参考签名算法"
            else -> "返回status未知"
        }

/**
 * 处理错误信息
 */
fun handleError(e: Throwable) =
        if (e is ConnectException || e is UnknownHostException) {
            "连接错误，请检查您的网络稍后重试"
        } else if (e is SocketTimeoutException) {
            "连接超时，请稍后重试"
        } else if (e is HttpException) { //http状态码错误
            val code = e.code()
            when (code) {
                401 -> "未授权的访问$code"
                in 500..599 -> {
                    "服务器错误$code"
                }
                404 -> {
                    "服务器找到不给定的接口资源$code"
                }
                in 400..499 -> {
                    "网络错误$code"
                }
                else -> {
                    "网络未知错误$code"
                }
            }
        } else if (e is ApiException) {
            status2Msg(e.status)
        } else {
            "未知错误" + e.message
        }
