package cn.justforfun.views

import android.hardware.Camera
import android.os.AsyncTask
import android.util.Log

class AutoFocusManager : Camera.AutoFocusCallback {
    private val TAG = "AutoFocusManager"

    private val AUTO_FOCUS_INTERVAL_MS = 2000L
    private val FOCUS_MODES_CALLING_AF: Collection<String>? = null

    private var useAutoFocus: Boolean = false
    private var camera: Camera? = null
    private var stopped = false
    private var focusing = false
    private var outstandingTask: AsyncTask<*, *, *>? = null

    override fun onAutoFocus(p0: Boolean, p1: Camera?) {
        TODO("Not yet implemented")
    }

    @Synchronized
    fun stop() {
        stopped = true
        if (useAutoFocus) {
            cancelOutstandingTask()
            // Doesn't hurt to call this even if not focusing
            try {
                camera?.cancelAutoFocus()
            } catch (re: RuntimeException) {
                // Have heard RuntimeException reported in Android 4.0.x+;
                // continue?
                Log.w(TAG, "Unexpected exception while cancelling focusing", re)
            }
        }
    }

    @Synchronized
    private fun cancelOutstandingTask() {
        if (outstandingTask == null) {
            return
        }
        if (outstandingTask!!.status != AsyncTask.Status.FINISHED) {
            outstandingTask!!.cancel(true)
        }
        outstandingTask = null
    }
}