package cn.justforfun.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.NonNull
import cn.justforfun.idcard.R

class CropImageView : FrameLayout {
    private var _imageView: ImageView? = null
    private var _cropOverlayView: CropOverlayView? = null

    constructor(@NonNull context: Context) : super(context) {
    }

    constructor(@NonNull context: Context, @NonNull attrs: AttributeSet) : super(context, attrs) {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val v: View = inflater.inflate(R.layout.crop_image_view, this, true)
        _imageView = v.findViewById(R.id.img_crop)
        _cropOverlayView = v.findViewById<CropOverlayView>(R.id.overlay_crop)
    }

    fun setImageBitmap(bitmap: Bitmap) {
        _imageView?.setImageBitmap(bitmap)
        _cropOverlayView?.setBitmap(bitmap)
    }

    fun crop(listener: CropListener, needStretch: Boolean) {
        _cropOverlayView?.crop(listener, needStretch)
    }
}