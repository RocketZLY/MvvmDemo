package rocketly.mvvmdemo.net

/**
 * Created by zhuliyuan on 2018/8/15.
 */
data class ApiResult(val HeWeather6: List<ApiStatus>)

data class ApiStatus(val status: String) {
    fun isOk() = status == "ok"
}