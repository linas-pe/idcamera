package cn.justforfun.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.os.Environment
import android.provider.MediaStore
import java.io.*

class ImageUtils {
    companion object {
        private val RELATIVE_PATH = File.separator + "MeiTeng" + File.separator + "IDCard"
        private val LAST_IMAGE_DIR = Environment.DIRECTORY_DCIM + RELATIVE_PATH
        private val IMG_FORMAT = Bitmap.CompressFormat.JPEG

        fun saveTemp(context: Context, src: Bitmap, name: String): Boolean {
            val path = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath + RELATIVE_PATH
            val file = File(path, name)
            if (isEmptyBitmap(src) || !FileUtils.createOrExistsFile(file)) {
                return false
            }
            println(src.width.toString() + ", " + src.height)
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
            val path = context.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath + RELATIVE_PATH
            val file = File(path, name)
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

        fun mergeImages(context: Context): Bitmap? {
            return null
        }

        private fun isEmptyBitmap(src: Bitmap?): Boolean {
            return src == null || src.width == 0 || src.height == 0
        }

        fun getBitmapFromByte(bytes: ByteArray, width: Int, height: Int): Bitmap? {
            val image = YuvImage(bytes, ImageFormat.NV21, width, height, null)
            val os = ByteArrayOutputStream(bytes.size)
            if (!image.compressToJpeg(Rect(0,0, width, height), 100, os)) {
                return null;
            }
            val tmp = os.toByteArray()
            return BitmapFactory.decodeByteArray(tmp, 0, tmp.size)
        }
    }
}