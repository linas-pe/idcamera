package cn.justforfun.utils

import android.content.Context

class ScreenUtils {
    companion object {
        fun getScreenWidth(context: Context): Int {
            return context.resources.displayMetrics.widthPixels
        }

        fun getScreenHeight(context: Context): Int {
            return context.resources.displayMetrics.heightPixels
        }
    }
}