package cn.justforfun.views

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.*
import kotlin.math.abs

class SensorController private constructor(context: Context) : SensorEventListener {
    private var _sensorManager: SensorManager? = null
    private var _sensor: Sensor? = null
    private var mX: Int = 0
    private var mY: Int = 0
    private var mZ: Int = 0
    private var lastStaticStamp = 0L
    var _calendar: Calendar? = null
    private var foucsing = 1

    companion object {
        val DELEY_DURATION = 500
        val STATUS_NONE = 0
        val STATUS_STATIC = 1
        val STATUS_MOVE = 2
        private var _instence: SensorController? = null

        fun getInstance(context: Context): SensorController {
            if (_instence == null) {
                _instence = SensorController(context)
            }
            return _instence!!
        }
    }

    var isFocusing = false
    var canFocusIn = false
    var canFocus = false
    private var STATUE: Int = STATUS_NONE

    init {
        _sensorManager = context.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        _sensor = _sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun onStart() {
        resetParams()
        canFocus = true
        _sensorManager!!.registerListener(this, _sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun onStop() {
        _cameraFocusListener = null
        _sensorManager!!.unregisterListener(this, _sensor)
        canFocus = false
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor == null) {
            return
        }
        if (isFocusing) {
            resetParams()
            return
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0].toInt()
            val y = event.values[1].toInt()
            val z = event.values[2].toInt()
            _calendar = Calendar.getInstance()
            val stamp = _calendar!!.timeInMillis
            val second = _calendar!!.get(Calendar.SECOND)

            if (STATUE != STATUS_NONE) {
                val px = abs(mX - x)
                val py = abs(mY - y)
                val pz = abs(mZ - z)

                val value = Math.sqrt((px * px + py * py + pz * pz).toDouble())
                if (value > 1.4) {
                    STATUE = STATUS_MOVE
                } else {
                    if (STATUE == STATUS_MOVE) {
                        lastStaticStamp = stamp
                        canFocusIn = true
                    }

                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELEY_DURATION) {
                            if (!isFocusing) {
                                canFocusIn = false
                                _cameraFocusListener?.onFocus()
                            }
                        }
                    }
                    STATUE = STATUS_STATIC
                }
            } else {
                lastStaticStamp = stamp
                STATUE = STATUS_STATIC
            }
            mX = x
            mY = y
            mZ = z
        }
    }

    private fun resetParams() {
        STATUE = STATUS_NONE
        canFocus = false
        mX = 0
        mY = 0
        mZ = 0
    }

    private var _cameraFocusListener: CameraFocusListener? = null

    interface CameraFocusListener {
        fun onFocus()
    }

    fun setCameraFocusListener(listener: CameraFocusListener) {
        _cameraFocusListener = listener
    }
}