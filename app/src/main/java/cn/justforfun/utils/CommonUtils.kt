package cn.justforfun.utils

class CommonUtils {
    companion object {
        var lastClickTime = 0L

        fun isFastClick(): Boolean {
            return isFastClick(1000)
        }

        fun isFastClick(intervalTime: Long): Boolean {
            val time = System.currentTimeMillis()
            if (time - lastClickTime < intervalTime) {
                return true
            }
            lastClickTime = time
            return false
        }
    }
}