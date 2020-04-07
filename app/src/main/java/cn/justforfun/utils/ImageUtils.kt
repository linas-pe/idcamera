package cn.justforfun.utils

import android.graphics.*
import java.io.*

class ImageUtils {
    companion object {
        fun save(src: Bitmap, filePath: String, format: Bitmap.CompressFormat): Boolean {
            return save(src, FileUtils.getFileByPath(filePath), format, false)
        }

        fun save(src: Bitmap, file: File, format: Bitmap.CompressFormat): Boolean {
            return save(src, file, format, false)
        }

        fun save(src: Bitmap, filePath: String, format: Bitmap.CompressFormat, recycle: Boolean): Boolean {
            return save(src, FileUtils.getFileByPath(filePath), format, recycle)
        }

        fun save(src: Bitmap, file: File?, format: Bitmap.CompressFormat, recycle: Boolean): Boolean {
            if (isEmptyBitmap(src) || !FileUtils.createOrExistsFile(file)) {
                return false
            }
            println(src.width.toString() + ", " + src.height)
            var os: OutputStream? = null
            var ret = false
            try {
                os = BufferedOutputStream(FileOutputStream(file!!))
                ret = src.compress(format, 100, os)
                if (recycle && !src.isRecycled) {
                    src.recycle()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                FileUtils.closeIO(os)
            }
            return ret
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