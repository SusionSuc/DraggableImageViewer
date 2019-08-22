package com.draggable.library.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget

/**
 * 简单的图片展示容器。 宽度一定充满父View， 可指定高度
 * */
class SimpleMaxWidthImageViewer : FrameLayout {

    private val TAG = javaClass.simpleName
    private val imageView = ImageView(context).apply {
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
        addView(imageView)
    }

    fun loadImage(imageInfo: ImageInfo) {
        Glide.with(context).load(imageInfo.url).into(object : ImageViewTarget<Drawable>(imageView) {
            override fun setResource(resource: Drawable?) {
                setDrawable(resource)
            }

            override fun onLoadStarted(placeholder: Drawable?) {
                super.onLoadStarted(placeholder)
            }
        })
//        post {
//            //(this@SimpleMaxWidthImageViewer.width / imageInfo.whRadio).toInt()
//            Log.d(TAG, "imageInfo.whRadio : ${imageInfo.whRadio}  this@SimpleMaxWidthImageViewer.width : ${this@SimpleMaxWidthImageViewer.width}")
//            imageView.layoutParams =
//                LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,   //宽默认撑满父View
//                    (width * imageInfo.whRadio).toInt()
//                ).apply {
//                    gravity = Gravity.CENTER
//                }
//        }
    }


    class ImageInfo(val url: String, val whRadio: Float = 1f)
}