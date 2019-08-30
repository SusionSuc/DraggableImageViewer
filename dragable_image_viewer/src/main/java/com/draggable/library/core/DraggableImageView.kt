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
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.draggable.library.extension.Utils
import com.draggable.library.extension.glide.GlideHelper
import com.draggable.library.extension.entities.DraggableImageInfo
import com.drawable.library.R
import io.reactivex.disposables.Disposable
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
    private var downloadDisposable: Disposable? = null

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
        mDraggableImageViewPhotoView?.apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setOnClickListener {
                loadImage(draggableImageInfo?.originImg ?: "")
            }
        }

        setOnClickListener {
            clickToExit()
        }
        mDraggableImageViewPhotoView.setOnClickListener {
            clickToExit()
        }

        mDraggableImageViewViewOriginImage.setOnClickListener {
            loadImage(draggableImageInfo?.originImg ?: "")
        }
    }

    private fun clickToExit() {
        if (draggableZoomCore?.isAnimating == true) return
        mDraggableImageViewViewOProgressBar.visibility = View.GONE
        if (mDraggableImageViewPhotoView.scale != 1f) {
            mDraggableImageViewPhotoView.setScale(1f, true)
        } else {
            draggableZoomCore?.exitWithAnimator()
            downloadDisposable?.dispose()
        }
    }

    /**
     * 拖拽支持
     * */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val isIntercept = super.onInterceptTouchEvent(ev)
        if (draggableZoomCore?.isAnimating == true) {
            return true
        }
        if (mDraggableImageViewPhotoView.scale != 1f) {
            return false
        }
        if (!mDraggableImageViewPhotoView.attacher.displyRectIsFromTop()) {
            return false
        }
        if (mDraggableImageViewViewOProgressBar.visibility == View.VISIBLE) {    // loading 时不允许拖拽退出
            return false
        }
        return draggableZoomCore?.onInterceptTouchEvent(isIntercept, ev) ?: false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        draggableZoomCore?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun showImageWithAnimator(paramsInfo: DraggableImageInfo) {
        draggableImageInfo = paramsInfo
        GlideHelper.retrieveImageWhRadioFromMemoryCache(context, paramsInfo.thumbnailImg) { whRadio ->
            draggableImageInfo?.draggableInfo?.scaledViewWhRadio = whRadio
            post {
                if (!paramsInfo.draggableInfo.isValid()) {
                    showImage(paramsInfo)
                    return@post
                }

                draggableZoomCore = DraggableZoomCore(
                    paramsInfo.draggableInfo,
                    mDraggableImageViewScaledView,
                    width,
                    height,
                    draggableZoomActionListener
                )
                draggableZoomCore?.adjustScaleViewToInitLocation()
                loadAvailableImage(true)
            }
        }
    }

    fun showImage(paramsInfo: DraggableImageInfo) {
        draggableImageInfo = paramsInfo
        GlideHelper.retrieveImageWhRadioFromMemoryCache(context, paramsInfo.thumbnailImg) { whRadio ->
            draggableImageInfo?.draggableInfo?.scaledViewWhRadio = whRadio
            post {
                draggableZoomCore = DraggableZoomCore(
                    paramsInfo.draggableInfo,
                    mDraggableImageViewScaledView,
                    width,
                    height,
                    draggableZoomActionListener
                )
                draggableZoomCore?.adjustScaleViewToCorrectLocation()
                loadAvailableImage(false)
            }
        }
    }

    private fun loadAvailableImage(startAnimator: Boolean) {
        mDraggableImageViewPhotoView.setImageResource(R.drawable.place_holder_transparent)
        GlideHelper.checkImageIsInMemoryCache(context, draggableImageInfo!!.thumbnailImg) { inCache ->
            if (inCache) {
                loadImage(draggableImageInfo!!.thumbnailImg)
                if (startAnimator) {
                    draggableZoomCore?.enterWithAnimator {
                        if (Utils.isWifiConnected(context)) loadImage(draggableImageInfo?.originImg ?: "")
                        mDraggableImageViewScaledView.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT)
                    }
                } else {
                    if (Utils.isWifiConnected(context)) loadImage(draggableImageInfo?.originImg ?: "")
                }
            } else {
                if (Utils.isWifiConnected(context)) {
                    loadImage(draggableImageInfo?.originImg ?: "")
                } else {
                    loadImage(draggableImageInfo!!.thumbnailImg)
                }
            }
        }
    }

    private fun loadImage(url: String) {
        currentLoadUrl = url
        if (currentLoadUrl != draggableImageInfo?.originImg) {
            mDraggableImageViewViewOriginImage.visibility = View.VISIBLE
            if (draggableImageInfo!!.imageSize > 0) {
                mDraggableImageViewViewOriginImage.text =
                    "查看原图(${Utils.formatImageSize(context, draggableImageInfo?.imageSize ?: 0)})"
            }
        } else {
            mDraggableImageViewViewOriginImage.visibility = View.GONE
        }

        val showLoading = !GlideHelper.imageIsInCache(context, url)

        Glide.with(context)
            .load(url)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    mDraggableImageViewViewOProgressBar.visibility = View.GONE
                    if (resource is GifDrawable) {
                        Glide.with(context).load(url).into(mDraggableImageViewPhotoView)
                    } else {
                        mDraggableImageViewPhotoView.setImageBitmap(translateToFixedBitmap(resource))
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    mDraggableImageViewViewOProgressBar.visibility = View.GONE
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(null)
                    if (showLoading) mDraggableImageViewViewOProgressBar.visibility = View.VISIBLE
                }
            })
    }

    //avoid oom
    private fun translateToFixedBitmap(originDrawable: Drawable): Bitmap? {
        val whRadio = originDrawable.intrinsicWidth * 1f / originDrawable.intrinsicHeight

        var bpWidth = if (this@DraggableImageView.width != 0) this@DraggableImageView.width else 1080

        if (bpWidth > 1080) bpWidth = 1080

        val bpHeight = (bpWidth * 1f / whRadio).toInt()

        Log.d(TAG, "bpWidth : $bpWidth  bpHeight : $bpHeight")

        var bp = Glide.get(context).bitmapPool.get(
            bpWidth,
            bpHeight,
            if (bpHeight > 5000) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
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

    fun setViewOriginImageBtnVisible(visible: Boolean) {
        mDraggableImageViewViewOriginImage.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun closeWithAnimator() {
        draggableZoomCore?.exitWithAnimator()
        downloadDisposable?.dispose()
    }

}