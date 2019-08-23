package com.draggable.library.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.draggable.library.photoview.PhotoView

/**
 * 简单的图片展示容器。 宽度一定充满父View， 可指定高度
 * */
class SimpleMaxWidthImageViewer : FrameLayout {

    private val TAG = javaClass.simpleName
    private val photoView = PhotoView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        scaleType = ImageView.ScaleType.CENTER_CROP
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    private fun initView() {
        layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(photoView)
    }

    fun loadImage(imageInfo: ImageInfo) {
        Glide.with(context).load(imageInfo.url).into(object : ImageViewTarget<Drawable>(photoView) {
            override fun setResource(resource: Drawable?) {
                setDrawable(resource)
            }

            override fun onLoadStarted(placeholder: Drawable?) {
                super.onLoadStarted(placeholder)
            }
        })

        post {
            val imageActualHeight = (this@SimpleMaxWidthImageViewer.width / imageInfo.whRadio).toInt()
            if (imageActualHeight > height) {    //长图
                val scaleRadio = 1f/imageInfo.whRadio
                Log.d(TAG, "scaleRadio : $scaleRadio")
                photoView.setScaleLevels(0f, 1f, scaleRadio)
                photoView.setScale(0.5f, false)
//                photoView.scaleType = ImageView.ScaleType.FIT_CENTER

//                val scaleY = height * 1f / imageActualHeight
//                photoView.scaleX = scaleX
//                photoView.scaleY = scaleY
//                Log.d(TAG, "imageActualHeight : $imageActualHeight  ; imageActualWidth : $imageActualWidth ;  scaleX : $scaleX ; scaleY:$scaleY")
//                photoView.scaleType = ImageView.ScaleType.FIT_XY
//                photoView.sca
//                val scale
            }
        }
    }

    class ImageInfo(val url: String, val whRadio: Float = 1f)
}