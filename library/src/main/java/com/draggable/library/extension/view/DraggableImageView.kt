package com.draggable.library.extension.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.draggable.library.core.DraggableParamsInfo
import com.draggable.library.core.DraggableZoomCore
import com.draggable.library.core.photoview.PhotoView
import com.draggable.library.extension.entities.DraggableImageInfo
import com.drawable.library.R
import kotlinx.android.synthetic.main.view_draggable_simple_image.view.*

/**
 * 单张图片查看器
 *
 * 1. 支持动画进入和退出
 * 2. 支持拖放退出
 * */
class DraggableImageView : FrameLayout {

    interface ActionListener {
        fun onExit() // drag to exit
    }

    private val TAG = javaClass.simpleName

    var actionListenr: ActionListener? = null
    private var draggableZoomActionListener = object : DraggableZoomCore.ActionListener {
        override fun currentAlphaValue(alpha: Int) {
            background = ColorDrawable(Color.argb(alpha, 0, 0, 0))
        }

        override fun onExit() {
            actionListenr?.onExit()
        }
    }
    private var draggableZoomCore: DraggableZoomCore? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_draggable_simple_image, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.CENTER_CROP
        mDraggableImageViewPhotoView.layoutParams = LayoutParams(0, 0)
    }

    fun enterWithAnimator(paramsInfo: DraggableImageInfo, withAnimator: Boolean = true) {
        loadImage(paramsInfo.originImg, mDraggableImageViewPhotoView)
        post {
            draggableZoomCore = DraggableZoomCore(
                paramsInfo.draggableInfo,
                mDraggableImageViewPhotoView,
                width,
                height,
                draggableZoomActionListener
            )
            if (withAnimator && paramsInfo.draggableInfo.isValid()) {
                draggableZoomCore?.enterWithAnimator()
            }
        }
    }

    fun enterWithFixedSizeparamsInfo(
        fixedWidth: Int,
        fixedHeight: Int,
        paramsInfo: DraggableImageInfo,
        withAnimator: Boolean = true
    ) {
        loadImage(paramsInfo.originImg, mDraggableImageViewPhotoView)
        draggableZoomCore = DraggableZoomCore(
            paramsInfo.draggableInfo,
            mDraggableImageViewPhotoView,
            fixedWidth,
            fixedHeight,
            draggableZoomActionListener
        )
        if (withAnimator && paramsInfo.draggableInfo.isValid()) {
            draggableZoomCore?.enterWithAnimator()
        } else {
            mDraggableImageViewPhotoView.layoutParams = LayoutParams(fixedWidth, fixedHeight)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.dispatchTouchEvent(event)
        draggableZoomCore?.dispatchTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    private fun loadImage(url: String, imageVeiew: ImageView, sucBlock: () -> Unit = {}) {
        Glide.with(this).load(url)
            .into(object : ImageViewTarget<Drawable>(imageVeiew) {
                override fun setResource(resource: Drawable?) {
                    if (resource != null) {
                        setDrawable(resource)
                        sucBlock()
                    }
                }
            })
    }


    fun close() {
        draggableZoomCore?.exitWithAnimator()
    }

    fun getImageView(): ImageView = mDraggableImageViewPhotoView

}