package cn.justforfun.views

import android.graphics.Bitmap

interface CropListener {
    fun onFinish(bitmap: Bitmap?)
}