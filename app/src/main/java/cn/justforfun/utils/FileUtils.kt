package cn.justforfun.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import java.io.Closeable
import java.io.File
import java.io.IOException

class FileUtils {
    companion object {
        fun sdCardIsAvailable(): Boolean {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                return File(Environment.getExternalStorageDirectory().path).canWrite()
            }
            return false
        }

        fun createOrExistsDir(dirPath: String): Boolean {
            return createOrExistsDir(getFileByPath(dirPath))
        }

        fun createOrExistsDir(file: File?): Boolean {
            if (file == null) {
                return false
            }

            if (file.exists())
                return file.isDirectory
            try {
                file.mkdirs()
                return true
            } catch (e: SecurityException) {
                e.printStackTrace()
                return false
            }
        }

        fun createOrExistsFile(filePath: String): Boolean {
            return createOrExistsFile(getFileByPath(filePath))
        }

        fun createOrExistsFile(file: File?): Boolean {
            if (file == null) {
                return false
            }
            if (file.exists()) {
                return file.isFile
            }
            if (!createOrExistsDir(file.parentFile)) {
                return false
            }
            return file.createNewFile()
        }

        fun getFileByPath(filePath: String): File? {
            return if (isSpace(filePath)) null else File(filePath)
        }

        private fun isSpace(s: String?): Boolean {
            if (s == null) {
                return true
            }
            for (c in s) {
                if (!Character.isWhitespace(c)) {
                    return false
                }
            }
            return true
        }

        fun closeIO(vararg closeables: Closeable?) {
            try {
                for (closeable in closeables) {
                    closeable?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}