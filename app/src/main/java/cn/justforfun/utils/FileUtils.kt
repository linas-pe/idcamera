package cn.justforfun.utils

import java.io.Closeable
import java.io.File
import java.io.IOException

class FileUtils {
    companion object {
        private fun createOrExistsDir(file: File?): Boolean {
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