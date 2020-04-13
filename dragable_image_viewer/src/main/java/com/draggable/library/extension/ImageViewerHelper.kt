package com.draggable.library.extension

import android.content.Context
import android.graphics.Rect
import android.view.View
import com.draggable.library.core.DraggableParamsInfo
import com.draggable.library.extension.entities.DraggableImageInfo

object ImageViewerHelper {

    private val TAG = javaClass.simpleName

    class ImageInfo(
        val thumbnailUrl: String,
        val originUrl: String = "",
        val imgSize: Long = 0
    )

    fun showSimpleImage(
        context: Context,
        url: String,
        thumbUrl: String = "",
        view: View? = null,
        showDownLoadBtn: Boolean = true
    ) {
        showImages(
            context,
            if (view == null) null else listOf(view),
            listOf(
                ImageInfo(
                    thumbUrl,
                    url
                )
            ),
            showDownLoadBtn = showDownLoadBtn
        )
    }

    fun showImages(
        context: Context,
        images: List<String>,
        index: Int = 0,
        showDownLoadBtn: Boolean = true
    ) {
        val imgInfos = ArrayList<ImageInfo>()
        images.forEach {
            imgInfos.add(
                ImageInfo(
                    it,
                    it
                )
            )
        }
        showImagesWithSingleView(
            context,
            null,
            imgInfos,
            index,
            showDownLoadBtn
        )
    }

    fun showSimpleImage(
        context: Context,
        imgInfo: ImageInfo,
        view: View? = null,
        showDownLoadBtn: Boolean = true
    ) {
        showImages(
            context,
            if (view == null) null else listOf(view),
            listOf(imgInfo),
            showDownLoadBtn = showDownLoadBtn
        )
    }

    fun showImages(
        context: Context,
        views: List<View>?,
        imgInfos: List<ImageInfo>,
        index: Int = 0,
        showDownLoadBtn: Boolean = true
    ) {
        if (imgInfos.isEmpty()) return
        //多张图片开启复杂的方式显示
        val draggableImageInfos = ArrayList<DraggableImageInfo>()
        imgInfos.forEachIndexed { index, imageInfo ->
            if (views != null && index < views.size) {
                draggableImageInfos.add(
                    createImageDraggableParamsWithWHRadio(
                        views[index],
                        imageInfo.originUrl,
                        imageInfo.thumbnailUrl,
                        imageInfo.imgSize,
                        showDownLoadBtn
                    )
                )
            } else {
                draggableImageInfos.add(
                    createImageDraggableParamsWithWHRadio(
                        null,
                        imageInfo.originUrl,
                        imageInfo.thumbnailUrl,
                        imageInfo.imgSize,
                        showDownLoadBtn
                    )
                )
            }
        }
        ImagesViewerActivity.start(context, draggableImageInfos, index)
    }

    fun showImagesWithSingleView(
        context: Context,
        view: View?,
        imgInfos: List<ImageInfo>,
        picIndex: Int = 0,
        showDownLoadBtn: Boolean = true
    ){
        if (imgInfos.isEmpty()) return
        //多张图片开启复杂的方式显示
        val draggableImageInfos = ArrayList<DraggableImageInfo>()
        imgInfos.forEachIndexed { index, imageInfo ->
            if (view != null && index == picIndex) {
                draggableImageInfos.add(
                    createImageDraggableParamsWithWHRadio(
                        view,
                        imageInfo.originUrl,
                        imageInfo.thumbnailUrl,
                        imageInfo.imgSize,
                        showDownLoadBtn
                    )
                )
            } else {
                draggableImageInfos.add(
                    createImageDraggableParamsWithWHRadio(
                        null,
                        imageInfo.originUrl,
                        imageInfo.thumbnailUrl,
                        imageInfo.imgSize,
                        showDownLoadBtn
                    )
                )
            }
        }
        ImagesViewerActivity.start(context, draggableImageInfos, picIndex)
    }

    /**
     * 根据宽高比，显示一张图片
     * @param whRadio  图片宽高比
     * */
    private fun createImageDraggableParamsWithWHRadio(
        view: View?,
        originUrl: String,
        thumbUrl: String,
        imgSize: Long = 0,
        showDownLoadBtn: Boolean = true
    ): DraggableImageInfo {
        val draggableInfo: DraggableImageInfo
        if (view != null) {
            val location = IntArray(2)
            view.getLocationInWindow(location)
//            val windowRect = Rect()
//            view.getWindowVisibleDisplayFrame(windowRect)
            val top = location[1]
            draggableInfo = DraggableImageInfo(
                originUrl,
                thumbUrl,
                DraggableParamsInfo(
                    location[0],
                    top,
                    view.width,
                    view.height
                ),
                imgSize,
                showDownLoadBtn
            )
        } else {
            draggableInfo = DraggableImageInfo(originUrl, thumbUrl, imageSize = imgSize,imageCanDown = showDownLoadBtn)
        }

        draggableInfo.adjustImageUrl()

        return draggableInfo
    }

}