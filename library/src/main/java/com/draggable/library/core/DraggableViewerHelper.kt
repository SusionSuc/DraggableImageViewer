package com.draggable.library.core

import android.content.Context
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.util.Log
import android.view.View
import com.draggable.library.ImagesViewerActivity
import com.draggable.library.SimpleImageViewerActivity
import com.draggable.library.core.DraggableViewerHelper.ImageInfo.Companion.DEFAULT_RADDIO
import com.draggable.library.extension.entities.DraggableImageInfo

object DraggableViewerHelper {

    class ImageInfo(val url: String, val originUrl: String = "", val whRadio: Float = DEFAULT_RADDIO) {
        companion object {
            const val DEFAULT_RADDIO = 1f
        }
    }

    fun showSimpleImage(context: Context, view: View, url: String, whRadio: Float = DEFAULT_RADDIO) {
        SimpleImageViewerActivity.start(
            context,
            createImageDraggableParamsWithWHRadio(
                view,
                getValidWhRadio(whRadio, view),
                url,
                url
            )
        )
    }

    private fun getValidWhRadio(whRadio: Float, view: View): Float {
        var radio = whRadio
        if (radio == DEFAULT_RADDIO) {
            radio = view.width * 1f / view.height
        }
        return radio
    }

    fun showImages(context: Context, views: List<View>, imgInfos: List<ImageInfo>, index: Int = 0) {
        val draggableImageInfos = ArrayList<DraggableImageInfo>()
        imgInfos.forEachIndexed { index, imageInfo ->
            if (index < views.size) {
                draggableImageInfos.add(
                    createImageDraggableParamsWithWHRadio(
                        views[index],
                        getValidWhRadio(imageInfo.whRadio, views[index]),
                        imageInfo.url,
                        imageInfo.url
                    )
                )
            } else {
                draggableImageInfos.add(DraggableImageInfo(imageInfo.url, imageInfo.url))
            }
        }
        ImagesViewerActivity.start(context, draggableImageInfos, index)
    }

    /**
     * 根据宽高比，显示一张图片
     * @param whRadio  图片宽高比
     * */
    private fun createImageDraggableParamsWithWHRadio(
        view: View,
        whRadio: Float = 1f,
        originUrl: String,
        thumbUrl: String
    ): DraggableImageInfo {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        val windowRect = Rect()
        view.getWindowVisibleDisplayFrame(windowRect)
        val top = location[1] - windowRect.top + getStatusBarHeight(view.context)
        val draggableInfo = DraggableParamsInfo(
            location[0],
            top,
            view.width,
            view.height,
            whRadio
        )

        return DraggableImageInfo(originUrl, thumbUrl, draggableInfo)
    }

    private fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return context.resources.getDimensionPixelSize(resourceId)
    }

}