package cn.justforfun.utils

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.Fragment
import cn.justforfun.idcard.CameraActivity
import java.lang.RuntimeException
import java.lang.ref.WeakReference

class IDCardCamera private constructor(activity: Activity?, fragment: Fragment?) {
    private var _activity: WeakReference<Activity?> = WeakReference(null)
    private var _fragment: WeakReference<Fragment?> = WeakReference(null)

    companion object {
        const val TYPE_IDCARD_FRONT = 1
        const val TYPE_IDCARD_BACK = 2
        const val RESULT_CODE = 0X11
        const val PERMISSION_CODE_FIRST = 0x12
        const val TAKE_TYPE = "take_type"
        const val IMAGE_NAME = "image_name"

        fun create(activity: Activity): IDCardCamera {
            return IDCardCamera(activity)
        }

        fun getImageName(type: Int): String {
            when (type) {
                TYPE_IDCARD_BACK -> return "temp_idcard_back.jpg"
                TYPE_IDCARD_FRONT -> return "temp_idcard_back.jpg"
            }
            throw RuntimeException("unknown type")
        }
    }

    init {
        this._activity = WeakReference(activity)
        this._fragment = WeakReference(fragment)
    }

    private constructor(activity: Activity) : this(activity, null)

    fun openCamera(IDCardDirection: Int) {
        val activity: Activity? = this._activity.get()
        val fragment: Fragment? = this._fragment.get()

        val intent = Intent(activity, CameraActivity::class.java)
        intent.putExtra(TAKE_TYPE, IDCardDirection)
        if (fragment != null) {
            fragment.startActivityForResult(intent, IDCardDirection)
        } else {
            activity?.startActivityForResult(intent, IDCardDirection)
        }
    }
}