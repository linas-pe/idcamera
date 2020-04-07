package cn.justforfun.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat

class PermissionUtils {

    companion object {
        fun checkPermissionFirst(context: Context, requestCode: Int, permission: List<String>) : Boolean {
            val permissions: MutableList<String> = mutableListOf()

            for (per in permission) {
                val permissionCode: Int = ActivityCompat.checkSelfPermission(context, per)
                if (permissionCode != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(per)
                }
            }
            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(context as Activity, permissions.toTypedArray(), requestCode)
                return false;
            }
            return true;
        }

        fun checkPermissionSecond(context: Context, requestCode: Int, permission: List<String>) : Boolean {
            val permissions: MutableList<String> = mutableListOf()

            for (per in permission) {
                val permissionCode: Int = ActivityCompat.checkSelfPermission(context, per)
                if (permissionCode != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(per)
                }
            }
            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(context as Activity, permissions.toTypedArray(), requestCode)

                val localIntent: Intent = Intent()
                localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (Build.VERSION.SDK_INT >= 9) {
                    localIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                    localIntent.data = Uri.fromParts("package", context.packageName, null)
                } else {
                    localIntent.action = Intent.ACTION_VIEW
                    localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
                    localIntent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
                }
                context.startActivity(localIntent)
                return false
            }
            return true
        }
    }
}