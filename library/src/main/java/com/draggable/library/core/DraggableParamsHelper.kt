package com.draggable.library.core

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.View

object DraggableParamsHelper {

    /**
     * 根据宽高比，显示一张图片
     * @param whRadio  图片宽高比
     * */
    fun createImageDraggableParamsWithWHRadio(view: View, whRadio: Float = 1f): DraggableParamsInfo {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val windowRect = Rect()
        view.getWindowVisibleDisplayFrame(windowRect)
        val top = location[1] - windowRect.top + getStatusBarHeight(view.context)
        return DraggableParamsInfo(
            location[0],
            top,
            view.width,
            view.height,
            whRadio
        )
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

}