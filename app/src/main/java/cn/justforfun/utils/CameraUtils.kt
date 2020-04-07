package cn.justforfun.utils

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera

class CameraUtils {
    companion object {
        private var camera: Camera? = null

        fun hasCamera(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }

        fun openCamera(): Camera? {
            camera = null
            camera = Camera.open()
            return camera
        }

        fun getCamera(): Camera? {
            return camera
        }

        fun hasFlash(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        }
    }
}