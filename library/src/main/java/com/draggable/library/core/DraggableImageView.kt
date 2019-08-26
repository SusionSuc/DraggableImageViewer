package com.draggable.library.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.draggable.library.extension.Utils
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
    var actionListener: ActionListener? = null
    private var currentLoadUrl = ""

    private var draggableZoomActionListener = object : DraggableZoomCore.ActionListener {
        override fun currentAlphaValue(alpha: Int) {
            background = ColorDrawable(Color.argb(alpha, 0, 0, 0))
        }

        override fun onExit() {
            actionListener?.onExit()
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
        mDraggableImageViewPhotoView.setOnClickListener {
            loadImage(draggableImageInfo?.originImg ?: "", true)
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

        if (!paramsInfo.draggableInfo.isValid()) {
            showImage(paramsInfo)
            return
        }

        val targetUrl: String = getFinalLoadImageUrl()
        loadImage(targetUrl)
        post {
            draggableZoomCore = DraggableZoomCore(
                paramsInfo.draggableInfo,
                mDraggableImageViewScaledView,
                width,
                height,
                draggableZoomActionListener
            )
            draggableZoomCore?.adjustScaleViewToInitLocation()
            val imageHasLoad = GlideHelper.imageIsInCache(context, targetUrl)
            if (imageHasLoad) {
                draggableZoomCore?.enterWithAnimator() //播放入场动画
            }
        }
    }

    fun showImage(paramsInfo: DraggableImageInfo) {
        draggableImageInfo = paramsInfo
        loadImage(getFinalLoadImageUrl())

        if (paramsInfo.draggableInfo.isValid()) {
            mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.FIT_CENTER
        }

        post {
            draggableZoomCore = DraggableZoomCore(
                paramsInfo.draggableInfo,
                mDraggableImageViewScaledView,
                width,
                height,
                draggableZoomActionListener
            )
            draggableZoomCore?.adjustScaleViewToCorrectLocation()
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

    private fun loadImage(url: String, isLoadOriginImage: Boolean = false) {
        currentLoadUrl = url
        Glide.with(this)
            .load(url)
            .into(object : ImageViewTarget<Drawable>(mDraggableImageViewPhotoView) {
                override fun setResource(resource: Drawable?) {
                    if (resource != null) {
                        mDraggableImageViewPhotoView.setImageBitmap(translateToFixedBitmap(resource))
                    }
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

    private fun translateToFixedBitmap(originDrawable: Drawable): Bitmap? {
        val whRadio = originDrawable.intrinsicWidth * 1f / originDrawable.intrinsicHeight

        var bpWidth = originDrawable.intrinsicWidth
        var bpHeight = originDrawable.intrinsicHeight

        when {
            whRadio < 1 && bpWidth > this@DraggableImageView.width -> { //宽图
                bpWidth = this@DraggableImageView.width
            }

            whRadio > 1 && bpHeight > this@DraggableImageView.height -> { //长图
                bpWidth = this@DraggableImageView.width
            }
        }

        bpHeight = (bpWidth * 1f / whRadio).toInt()

        Log.d(TAG, "bpWidth : $bpWidth  bpHeight : $bpHeight")

        val glideBitmapPool = Glide.get(context).bitmapPool
        var bp = glideBitmapPool.get(
            bpWidth,
            bpHeight,
            Bitmap.Config.ARGB_8888
        )
        if (bp == null) {
            bp = Bitmap.createBitmap(
                bpWidth,
                bpHeight,
                Bitmap.Config.ARGB_8888
            )
        }

        val canvas = Canvas(bp)
        originDrawable.setBounds(0, 0, bpWidth, bpHeight)
        originDrawable.draw(canvas)
        return bp
    }

    fun setDownLoadBtnVisiable(visiable: Boolean) {
        mDraggableImageViewViewDownLoadImage.visibility = if (visiable) View.VISIBLE else View.GONE
    }

    fun setViewOriginImageBtnVisible(visiable: Boolean) {
        mDraggableImageViewViewOriginImage.visibility = if (visiable) View.VISIBLE else View.GONE
    }

    fun closeWithAnimator() {
        draggableZoomCore?.exitWithAnimator()
    }

}