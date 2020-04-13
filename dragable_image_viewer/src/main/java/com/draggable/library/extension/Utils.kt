package com.draggable.library.extension

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import java.text.DecimalFormat


object Utils {

    private val sMetrics = Resources.getSystem().displayMetrics

    @JvmStatic
    fun getScreenHeight(): Int {
        return sMetrics?.heightPixels ?: 0
    }

    @JvmStatic
    fun getScreenWidth(): Int {
        return sMetrics?.widthPixels ?: 0
    }

    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun transparentStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window
                .decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            activity.window.statusBarColor = Color.TRANSPARENT
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    fun isWifiConnected(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            val type = networkInfo.typeName
            if (type.equals("WIFI", ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    fun formatImageSize(size: Long): String {
        if (size <= 0) return ""
        val formater = DecimalFormat("####")
        return if (size < 1024) {
            size.toString() + "B"
        } else if (size < 1024 * 1024) {
            val kbsize = size / 1024f
            formater.format(kbsize) + "KB"
        } else if (size < 1024 * 1024 * 1024) {
            val mbsize = size / 1024f / 1024f
            formater.format(mbsize) + "MB"
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            val gbsize = size / 1024f / 1024f / 1024f
            formater.format(gbsize) + "GB"
        } else ""
    }

}