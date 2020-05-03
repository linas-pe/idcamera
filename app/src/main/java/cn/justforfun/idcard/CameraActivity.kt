package cn.justforfun.idcard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import cn.justforfun.utils.*
import cn.justforfun.views.CameraPreview
import cn.justforfun.views.CropImageView
import cn.justforfun.views.CropListener

class CameraActivity : Activity(), View.OnClickListener {
    private var _cropImageView: CropImageView? = null
    private var _cropBitmap: Bitmap? = null
    private var _cameraPreview: CameraPreview? = null
    private var _l1CameraCropContainer: View? = null
    private var _ivCameraCrop: ImageView? = null
    private var _ivCameraFlash: ImageView? = null
    private var _l1CameraOption: View? = null
    private var _l1CameraResult: View? = null
    private var _viewCameraCropBottom: TextView? = null
    private var _flCameraOption: FrameLayout? = null
    private var _viewCameraCropLeft: View? = null

    private var _type: Int = 0
    private var isToast = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

        if (PermissionUtils.checkPermissionFirst(this,
                IDCardCamera.PERMISSION_CODE_FIRST, permissions))
        {
            init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isPermissions = true

        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                isPermissions = false
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                    if (isToast) {
                        Toast.makeText(this, getString(R.string.open_permissions),
                            Toast.LENGTH_SHORT).show()
                        isToast = false
                    }
                }
            }
        }
        isToast = true
        if (isPermissions) {
            init()
        } else {
            finish()
        }
    }

    private fun init() {
        setContentView(R.layout.activity_camera)
        _type = intent.getIntExtra(IDCardCamera.TAKE_TYPE, 0)
        initView()
        initListener()
    }

    private fun initView() {
        _cameraPreview = findViewById(R.id.camera_preview)
        _l1CameraCropContainer = findViewById(R.id.ll_camera_crop_container)
        _ivCameraCrop = findViewById(R.id.iv_camera_crop)
        _ivCameraFlash = findViewById(R.id.iv_camera_flash)
        _l1CameraOption = findViewById(R.id.ll_camera_option)
        _l1CameraResult = findViewById(R.id.ll_camera_result)
        _cropImageView = findViewById(R.id.crop_image_view)
        _viewCameraCropBottom = findViewById(R.id.view_camera_crop_bottom)
        _flCameraOption = findViewById(R.id.fl_camera_option)
        _viewCameraCropLeft = findViewById(R.id.view_camera_crop_left)

        val screenMinSize =
            ScreenUtils.getScreenWidth(this).coerceAtMost(ScreenUtils.getScreenHeight(this))
        val screenMaxSize =
            ScreenUtils.getScreenWidth(this).coerceAtLeast(ScreenUtils.getScreenHeight(this))
        val height = (screenMinSize * 0.75).toInt()
        val width = (height * 75F / 47).toInt()

        val flCameraOptionWidth = (screenMaxSize - width) / 2F
        val containerParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT)
        val cropParams = LinearLayout.LayoutParams(width, height)
        val cameraOptionParams = LinearLayout.LayoutParams(flCameraOptionWidth.toInt(), ViewGroup.LayoutParams.MATCH_PARENT)
        _l1CameraCropContainer!!.layoutParams = containerParams
        _ivCameraCrop!!.layoutParams = cropParams
        _flCameraOption!!.layoutParams = cameraOptionParams

        when (_type) {
            IDCardCamera.TYPE_IDCARD_FRONT -> _ivCameraCrop!!.setImageResource(R.mipmap.camera_idcard_front)
            IDCardCamera.TYPE_IDCARD_BACK -> _ivCameraCrop!!.setImageResource(R.mipmap.camera_idcard_back)
        }

        Handler().postDelayed({ runOnUiThread { _cameraPreview!!.visibility = View.VISIBLE } }, 500)
    }

    private fun initListener() {
        _cameraPreview!!.setOnClickListener(this)
        _ivCameraFlash!!.setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_camera_close).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_camera_take).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_camera_result_ok).setOnClickListener(this)
        findViewById<ImageView>(R.id.iv_camera_result_cancel).setOnClickListener(this)
        findViewById<Button>(R.id.btn_dcim).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.camera_preview -> _cameraPreview!!.focus()
            R.id.iv_camera_close -> finish()
            R.id.iv_camera_take -> {
                if (!CommonUtils.isFastClick()) {
                    takePhoto()
                }
            }
            R.id.iv_camera_flash -> {
                if (CameraUtils.hasFlash(this)) {
                    val flashRes = if (_cameraPreview!!.switchFlashLight()) R.mipmap.camera_flash_on else R.mipmap.camera_flash_off
                    _ivCameraFlash!!.setImageResource(flashRes)
                } else {
                    Toast.makeText(this, R.string.no_flash, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.iv_camera_result_ok -> confirm()
            R.id.iv_camera_result_cancel -> {
                _cameraPreview!!.isEnabled = true
                _cameraPreview!!.addCallback()
                _cameraPreview!!.startPreview()
                _ivCameraFlash!!.setImageResource(R.mipmap.camera_flash_off)
                setTakePhotoLayout()
            }
            R.id.btn_dcim -> chosePhoto()
        }
    }

    private fun chosePhoto() {
        val intentToPickPic = Intent(Intent.ACTION_PICK, null)
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intentToPickPic, IDCardCamera.CHOOSE_CODE)
    }

    private fun takePhoto() {
        _cameraPreview!!.isEnabled = false
        CameraUtils.getCamera()?.setOneShotPreviewCallback { bytes, camera ->
            val size = camera.parameters.previewSize //获取预览大小
            camera.stopPreview()
            Thread(Runnable {
                val w = size.width
                val h = size.height
                val bitmap = ImageUtils.getBitmapFromByte(bytes, w, h)
                cropImage(bitmap!!)
            }).start()
        }
    }

    private fun cropImage(bitmap: Bitmap) {
        val left = _viewCameraCropLeft!!.width.toFloat()
        val top = _ivCameraCrop!!.top.toFloat()
        val right = _ivCameraCrop!!.right.toFloat() + left
        val bottom = _ivCameraCrop!!.bottom.toFloat()

        val leftProportion = left / _cameraPreview!!.width
        val topProportion = top / _cameraPreview!!.height
        val rightProportion = right / _cameraPreview!!.width
        val bottomProportion = bottom / _cameraPreview!!.bottom

        _cropBitmap = Bitmap.createBitmap(bitmap,
            (leftProportion * bitmap.width).toInt(),
            (topProportion * bitmap.height).toInt(),
            ((rightProportion - leftProportion) * bitmap.width).toInt(),
            ((bottomProportion - topProportion) * bitmap.height).toInt())

        runOnUiThread {
            _cropImageView!!.layoutParams = LinearLayout.LayoutParams(_ivCameraCrop!!.width, _ivCameraCrop!!.height)
            setCropLayout()
            _cropImageView!!.setImageBitmap(_cropBitmap!!)
        }
    }

    private fun setCropLayout() {
        _ivCameraCrop!!.visibility = View.GONE
        _cameraPreview!!.visibility = View.GONE
        _l1CameraOption!!.visibility = View.GONE
        _cropImageView!!.visibility = View.VISIBLE
        _l1CameraResult!!.visibility = View.VISIBLE
        _viewCameraCropBottom!!.text = ""
    }

    private fun setTakePhotoLayout() {
        _ivCameraCrop!!.visibility = View.VISIBLE
        _cameraPreview!!.visibility = View.VISIBLE
        _l1CameraOption!!.visibility = View.VISIBLE
        _cropImageView!!.visibility = View.GONE
        _l1CameraResult!!.visibility = View.GONE
        _viewCameraCropBottom!!.text = getString(R.string.touch_to_focus)

        _cameraPreview!!.focus()
    }

    private fun confirm() {
        _cropImageView!!.crop(object: CropListener{
            override fun onFinish(bitmap: Bitmap?) {
                if (bitmap == null) {
                    Toast.makeText(applicationContext, getString(R.string.crop_fail), Toast.LENGTH_SHORT).show()
                    finish()
                }

                val imageName = IDCardCamera.getImageName(_type)
                if (ImageUtils.saveTemp(applicationContext, bitmap!!, imageName)) {
                    val intent = Intent()
                    intent.putExtra(IDCardCamera.IMAGE_NAME, imageName)
                    setResult(IDCardCamera.RESULT_CODE, intent)
                    finish()
                }
            }
        }, true)
    }

    override fun onStart() {
        super.onStart()
        _cameraPreview?.onStart()
    }

    override fun onStop() {
        super.onStop()
        _cameraPreview?.onStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == IDCardCamera.CHOOSE_CODE && data.data != null) {
            val bitmap = ImageUtils.openURI(this, data.data!!)
            cropImage(bitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
