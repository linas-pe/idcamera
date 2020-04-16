package cn.justforfun.utils

import java.io.Closeable
import java.io.File
import java.io.IOException

class FileUtils {
    companion object {
        fun createOrExistsDir(file: File?): Boolean {
            if (file == null) {
                return false
            }

            if (file.exists())
                return file.isDirectory
            return try {
                file.mkdirs()
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
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
            try {
                return file.createNewFile()
            } catch (e: RuntimeException) {
                e.printStackTrace()
                return false
            }
        }

        private fun getFileByPath(filePath: String): File? {
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