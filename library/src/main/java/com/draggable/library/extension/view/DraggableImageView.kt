package com.draggable.library.extension.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.draggable.library.Utils
import com.draggable.library.core.DraggableParamsInfo
import com.draggable.library.core.DraggableZoomCore
import com.draggable.library.extension.glide.GlideHelper
import com.draggable.library.extension.entities.DraggableImageInfo
import com.drawable.library.R
import kotlinx.android.synthetic.main.view_draggable_simple_image.view.*

/**
 * 单张图片查看器
 *
 * 1. 支持动画进入和退出
 * 2. 支持拖放退出
 *
 * */
class DraggableImageView : FrameLayout {

    interface ActionListener {
        fun onExit() // drag to exit
    }

    private val TAG = javaClass.simpleName
    private var draggableImageInfo: DraggableImageInfo? = null
    var actionListenr: ActionListener? = null
    private var currentLoadUrl = ""

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
        mDraggableImageViewPhotoView.setOnClickListener {
            loadImage(draggableImageInfo?.originImg ?: "", mDraggableImageViewPhotoView, true)
        }
        mDraggableImageViewViewDownLoadImage.setOnClickListener {
            GlideHelper.downloadPicture(context, currentLoadUrl)
        }
        mDraggableImageViewPhotoView.setOnClickListener {
            if (mDraggableImageViewPhotoView.scale != 1f) {
                mDraggableImageViewPhotoView.setScale(1f, true)
            } else {
                draggableZoomCore?.exitWithAnimator()
            }
        }
    }

    fun showImageWithAnimator(paramsInfo: DraggableImageInfo) {
        draggableImageInfo = paramsInfo
        val targetUrl: String = getFinalLoadImageUrl()
        loadImage(targetUrl, mDraggableImageViewPhotoView)
        post {
            draggableZoomCore = DraggableZoomCore(
                paramsInfo.draggableInfo,
                mDraggableImageViewPhotoView,
                width,
                height,
                draggableZoomActionListener
            )
            val imageHasLoad = GlideHelper.imageIsInCache(context, targetUrl)
            if (imageHasLoad) {
                draggableZoomCore?.enterWithAnimator() //播放入场动画
            }
        }
    }

    fun showImage(paramsInfo: DraggableImageInfo) {
        draggableImageInfo = paramsInfo
        loadImage(getFinalLoadImageUrl(), mDraggableImageViewPhotoView)
//        if (paramsInfo.draggableInfo.contentWHRadio == DraggableParamsInfo.INVALID_RADIO) {
//            mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.FIT_CENTER
//        } else {
//            mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.CENTER_CROP
//        }
//        mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.FIT_CENTER
        post {
            draggableZoomCore = DraggableZoomCore(
                paramsInfo.draggableInfo,
                mDraggableImageViewPhotoView,
                width,
                height,
                draggableZoomActionListener
            )
            draggableZoomCore?.adjustScaleViewLocation(paramsInfo)
        }
    }

    private fun getFinalLoadImageUrl(): String {
        if (draggableImageInfo == null) return ""
        val targetUrl: String
        //如果是wifi网络 or 原图已经加载过则直接加载原图
        if (GlideHelper.imageIsInCache(context, draggableImageInfo!!.originImg) || Utils.isWifiConnected(context)) {
            targetUrl = draggableImageInfo!!.originImg
            mDraggableImageViewViewOriginImage.visibility = View.GONE
        } else {
            targetUrl = draggableImageInfo!!.thumbnailImg
            mDraggableImageViewViewOriginImage.visibility = View.VISIBLE
        }
        return targetUrl
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (draggableZoomCore?.dispatchTouchEvent(ev) == true) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    //是否可以拦截事件
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val isIntercept = super.onInterceptTouchEvent(ev)

        if (mDraggableImageViewPhotoView.scale != 1f) {
            return false
        }

        return draggableZoomCore?.onInterceptTouchEvent(isIntercept, ev) ?: false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        draggableZoomCore?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun loadImage(url: String, imageView: ImageView, isLoadOriginImage: Boolean = false) {
        Log.d(TAG, "load thumbnailUrl : $url")
        currentLoadUrl = url
        Glide.with(this).load(url)
            .into(object : ImageViewTarget<Drawable>(imageView) {
                override fun setResource(resource: Drawable?) {
                    if (resource != null) {
                        setDrawable(resource)
                    }
//                    mDraggableImageViewPhotoView.setScale(1f, false)
                }

                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    super.onResourceReady(resource, transition)
                    mDraggableImageViewViewOProgressBar.visibility = View.GONE
                    if (isLoadOriginImage) {
                        mDraggableImageViewViewOriginImage.visibility = View.GONE
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    mDraggableImageViewViewOProgressBar.visibility = View.GONE
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                    if (!isLoadOriginImage) {
                        mDraggableImageViewViewOProgressBar.visibility = View.VISIBLE
                    } else {
                        mDraggableImageViewViewOriginImage.text = "加载中"
                    }
                }
            })
    }

    fun closeWithAnimator() {
        draggableZoomCore?.exitWithAnimator()
    }

}