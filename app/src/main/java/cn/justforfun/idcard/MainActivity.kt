package cn.justforfun.idcard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.justforfun.utils.IDCardCamera
import cn.justforfun.utils.ImageUtils

class MainActivity : AppCompatActivity() {
    private var _ivFront: ImageView? = null
    private var _ivBack: ImageView? = null
    private var _btnSave: Button? = null
    private var _hasFront = false
    private var _hasBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _ivFront = findViewById(R.id.iv_front)
        _ivBack = findViewById(R.id.iv_back)
        _btnSave = findViewById(R.id.btn_save)
    }

    fun front(view: View) {
        IDCardCamera.create(this).openCamera(IDCardCamera.TYPE_IDCARD_FRONT)
    }

    fun back(view: View) {
        IDCardCamera.create(this).openCamera(IDCardCamera.TYPE_IDCARD_BACK)
    }

    fun save(view: View) {
        val name = ""
        val mergeImages = ImageUtils.mergeImages(this) ?: return
        val msg = if (ImageUtils.save(this, mergeImages, name))
            R.string.save_success else  R.string.save_failed
        Toast.makeText(this, getString(msg), Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == IDCardCamera.RESULT_CODE) {
            val name = IDCardCamera.getImageName(requestCode)
            val img = ImageUtils.openTemp(this, name) ?: return
            when (requestCode) {
                IDCardCamera.TYPE_IDCARD_FRONT -> {
                    _ivFront!!.setImageBitmap(img)
                    _hasFront = true
                }
                IDCardCamera.TYPE_IDCARD_BACK -> {
                    _ivBack!!.setImageBitmap(img)
                    _hasBack = true
                }
            }
            if (_hasBack && _hasFront) {
                _btnSave!!.visibility = View.VISIBLE
            }
        }
    }
}
