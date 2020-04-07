package cn.justforfun.idcard

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import cn.justforfun.utils.IDCardCamera

class MainActivity : AppCompatActivity() {
    private var _ivFront: ImageView? = null
    private var _ivBack: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _ivFront = findViewById(R.id.iv_front)
        _ivBack = findViewById(R.id.iv_back)
    }

    fun front(view: View) {
        IDCardCamera.create(this).openCamera(IDCardCamera.TYPE_IDCARD_FRONT)
    }

    fun back(view: View) {
        IDCardCamera.create(this).openCamera(IDCardCamera.TYPE_IDCARD_BACK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == IDCardCamera.RESULT_CODE) {
            val path = data?.let { IDCardCamera.getImagePath(it) }
            if (!TextUtils.isEmpty(path)) {
                if (requestCode == IDCardCamera.TYPE_IDCARD_FRONT) {
                    _ivFront!!.setImageBitmap((BitmapFactory.decodeFile(path)))
                } else if (requestCode == IDCardCamera.TYPE_IDCARD_BACK) {
                    _ivBack!!.setImageBitmap(BitmapFactory.decodeFile(path))
                }
            }
        }
    }
}
