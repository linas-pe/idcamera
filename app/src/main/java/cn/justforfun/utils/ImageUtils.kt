package cn.justforfun.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.os.Environment
import android.provider.MediaStore
import java.io.*
import java.nio.file.Paths

class ImageUtils {
    companion object {
        private val RELATIVE_PATH = Paths.get("MeiTeng", "IDCard").toString()
        private val LAST_IMAGE_DIR = Paths.get(Environment.DIRECTORY_DCIM, RELATIVE_PATH).toString()
        private val IMG_FORMAT = Bitmap.CompressFormat.JPEG

        private val IDCARD_WIDTH = 1000
        private val IDCARD_HEIGHT = 600

        private fun imagePath(context: Context): String {
            val root = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath
            return  Paths.get(root, RELATIVE_PATH).toString()
        }

        fun saveTemp(context: Context, src: Bitmap, name: String): Boolean {
            val file = File(imagePath(context), name)
            if (isEmptyBitmap(src) || !FileUtils.createOrExistsFile(file)) {
                return false
            }
            var os: OutputStream? = null
            var ret = false
            try {
                os = BufferedOutputStream(FileOutputStream(file))
                ret = src.compress(IMG_FORMAT, 100, os)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                FileUtils.closeIO(os)
            }
            return ret
        }

        fun openTemp(context: Context, name: String): Bitmap? {
            val file = File(imagePath(context), name)
            if (!file.exists()) {
                return null
            }
            return BitmapFactory.decodeFile(file.absolutePath)
        }

        fun save(context: Context, src: Bitmap, name: String): Boolean {
            if (isEmptyBitmap(src)) {
                return false
            }
            val resolver = context.contentResolver
            val contentValue = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, LAST_IMAGE_DIR)
            }

            var ret = false
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValue)
                ?: return false
            try {
                resolver.openOutputStream(uri).use {
                    try {
                        ret = src.compress(IMG_FORMAT, 100, it)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return ret
        }

        private fun scaleImage(src: Bitmap): Bitmap {
            return Bitmap.createScaledBitmap(src, IDCARD_WIDTH, IDCARD_HEIGHT, false)
        }

        fun mergeImages(context: Context, frontName: String, backName: String): Bitmap? {
            val frontImg = openTemp(context, frontName) ?: return null
            val backImg = openTemp(context, backName) ?: return null

            val scaleFrontImg = scaleImage(frontImg)
            val scaleBackImg = scaleImage(backImg)

            val dist = Bitmap.createBitmap(2480, 3508, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(dist)

            val topRect = Rect(0, 0, IDCARD_WIDTH, IDCARD_HEIGHT)
            val frontRect = Rect(740, 577, IDCARD_WIDTH + 740, IDCARD_HEIGHT + 577)
            val backRectT = Rect(740, 2331, IDCARD_WIDTH + 740, IDCARD_HEIGHT + 2331)

            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(scaleFrontImg, topRect, frontRect, null)
            canvas.drawBitmap(scaleBackImg, topRect, backRectT, null)

            return dist
        }

        private fun isEmptyBitmap(src: Bitmap?): Boolean {
            return src == null || src.width == 0 || src.height == 0
        }

        fun getBitmapFromByte(bytes: ByteArray, width: Int, height: Int): Bitmap? {
            val image = YuvImage(bytes, ImageFormat.NV21, width, height, null)
            val os = ByteArrayOutputStream(bytes.size)
            if (!image.compressToJpeg(Rect(0,0, width, height), 100, os)) {
                return null
            }
            val tmp = os.toByteArray()
            return BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
        }
    }
}