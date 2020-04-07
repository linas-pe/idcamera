package cn.justforfun.views

import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import cn.justforfun.utils.CameraUtils
import cn.justforfun.utils.ScreenUtils
import kotlin.math.abs

class CameraPreview : SurfaceView, SurfaceHolder.Callback {
    private val TAG = "CameraPreview"
    private var camera: Camera? = null
    private var _autoFocusManager: AutoFocusManager? = null
    private var _sensorController: SensorController? = null
    private var _context: Context? = null
    private var _surfaceHolder: SurfaceHolder? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        _context = context
        _surfaceHolder = holder
        _surfaceHolder!!.addCallback(this)
        _surfaceHolder!!.setKeepScreenOn(true)
        _surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        _sensorController = SensorController.getInstance(context.applicationContext)
    }

    fun startPreview() {
        camera?.startPreview()
    }

    fun focus() {
        if (camera == null) {
            return
        }
        try {
            camera!!.autoFocus(null)
        } catch (e: Exception) {
            Log.d(TAG, "takePhoto $e")
        }
    }

    fun switchFlashLight(): Boolean {
        if (camera == null) {
            return false
        }
        val parameters = camera!!.parameters
        if (parameters.flashMode == Camera.Parameters.FLASH_MODE_OFF) {
            parameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
            camera!!.parameters = parameters
            return true
        }
        parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
        camera!!.parameters = parameters
        return false
    }

    fun onStart() {
        addCallback()
        _sensorController?.onStart()
        _sensorController?.setCameraFocusListener(object : SensorController.CameraFocusListener {
            override fun onFocus() {
                focus()
            }
        })
    }

    fun onStop() {
        _sensorController?.onStop()
    }

    fun addCallback() {
        _surfaceHolder?.addCallback(this)
    }

    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        holder.removeCallback(this)
        release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        camera = CameraUtils.openCamera()
        if (camera == null) {
            return
        }
        try {
            camera!!.setPreviewDisplay(holder)

            val parameters: Camera.Parameters = camera!!.parameters
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                camera!!.setDisplayOrientation(90)
                parameters.setRotation(90)
            } else {
                camera!!.setDisplayOrientation(0)
                parameters.setRotation(0)
            }
            val sizeList = parameters.supportedPreviewSizes
            val bestSize = getOptimalPreviewSize(sizeList, ScreenUtils.getScreenWidth(_context!!), ScreenUtils.getScreenHeight(_context!!))
            parameters.setPreviewSize(bestSize.width, bestSize.height)
            camera!!.parameters = parameters
            camera!!.startPreview()
            focus()
        } catch (e: Exception) {
            Log.d(TAG, "Error setting camera preview: ${e.message}")
            try {
                val parameters = camera!!.parameters
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    camera!!.setDisplayOrientation(90)
                    parameters.setRotation(90)
                } else {
                    camera!!.setDisplayOrientation(0)
                    parameters.setRotation(0)
                }
                camera!!.parameters = parameters
                camera!!.startPreview()
                focus()
            } catch (e1: Exception) {
                e.printStackTrace()
                camera = null
            }
        }
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>, w: Int, h: Int): Camera.Size {
        val ASPECT_TOLERANCE: Double = 0.1
        val targetRatio: Double = w.toDouble() / h

        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue
            if (abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = abs(size.height - h).toDouble()
            }
        }
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = abs(size.height - h).toDouble()
                }
            }
        }
        return optimalSize!!
    }

    private fun release() {
        if (camera == null) {
            return
        }
        camera!!.setPreviewCallback(null)
        camera!!.stopPreview()
        camera!!.release()
        camera = null

        _autoFocusManager?.stop()
        _autoFocusManager = null
    }
}