package com.draggable.library.core

import android.content.Context
import android.graphics.Rect
import android.view.View
import com.draggable.library.extension.ImagesViewerActivity
import com.draggable.library.extension.SimpleImageViewerActivity
import com.draggable.library.core.DraggableViewerHelper.ImageInfo.Companion.DEFAULT_RADDIO
import com.draggable.library.extension.entities.DraggableImageInfo

object DraggableViewerHelper {

    private val TAG = javaClass.simpleName

    class ImageInfo(val thumbnailUrl: String, val originUrl: String = "", val whRadio: Float = DEFAULT_RADDIO) {
        companion object {
            const val DEFAULT_RADDIO = 1f
        }
    }

    fun showSimpleImage(context: Context, url: String, view: View? = null, whRadio: Float = DEFAULT_RADDIO) {
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

    fun showSimpleImage(context: Context, imgInfo: ImageInfo, view: View? = null, whRadio: Float = DEFAULT_RADDIO) {
        SimpleImageViewerActivity.start(
            context,
            createImageDraggableParamsWithWHRadio(
                view,
                getValidWhRadio(whRadio, view),
                imgInfo.originUrl,
                imgInfo.thumbnailUrl
            )
        )
    }

    fun showImages(context: Context, views: List<View>?, imgInfos: List<ImageInfo>, index: Int = 0) {
        if (imgInfos.isEmpty()) return

        //单张图片使用简单的方式显示
        if (imgInfos.size == 1) {
            val imgInfo = imgInfos[0]
            val firstView: View? = if (views == null || views.isEmpty()) null else views[0]
            if (firstView != null) {
                showSimpleImage(context, imgInfo, firstView, imgInfo.whRadio)
            } else {

            }
            return
        }

        //多张图片开启复杂的方式显示
        val draggableImageInfos = ArrayList<DraggableImageInfo>()
        imgInfos.forEachIndexed { index, imageInfo ->
            if (views != null && index < views.size) {
                draggableImageInfos.add(
                    createImageDraggableParamsWithWHRadio(
                        views[index],
                        getValidWhRadio(imageInfo.whRadio, views[index]),
                        imageInfo.thumbnailUrl,
                        imageInfo.thumbnailUrl
                    )
                )
            } else {
                draggableImageInfos.add(DraggableImageInfo(imageInfo.thumbnailUrl, imageInfo.thumbnailUrl))
            }
        }
        ImagesViewerActivity.start(context, draggableImageInfos, index)
    }

    private fun getValidWhRadio(whRadio: Float, view: View?): Float {
        if (view == null) return DraggableParamsInfo.INVALID_RADIO
        var radio = whRadio
        if (radio == DEFAULT_RADDIO) {
            radio = view.width * 1f / view.height
        }
        return radio
    }

    /**
     * 根据宽高比，显示一张图片
     * @param whRadio  图片宽高比
     * */
    private fun createImageDraggableParamsWithWHRadio(
        view: View?,
        whRadio: Float = 1f,
        originUrl: String,
        thumbUrl: String
    ): DraggableImageInfo {
        val draggableInfo: DraggableImageInfo
        if (view != null) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val windowRect = Rect()
            view.getWindowVisibleDisplayFrame(windowRect)
            val top = location[1]
            draggableInfo = DraggableImageInfo(
                originUrl,
                thumbUrl,
                DraggableParamsInfo(
                    location[0],
                    top,
                    view.width,
                    view.height,
                    whRadio
                )
            )
        } else {
            draggableInfo = DraggableImageInfo(originUrl, thumbUrl)
        }

        return draggableInfo
    }

}