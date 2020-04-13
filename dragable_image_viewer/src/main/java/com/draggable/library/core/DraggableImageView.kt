package com.draggable.library.core

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
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
    private var draggableZoomCore: DraggableZoomCore? = null
    private var needFitCenter = true
    private var viewSelfWhRadio = 1f

    private var draggableZoomActionListener = object : DraggableZoomCore.ActionListener {
        override fun currentAlphaValue(alpha: Int) {
            background = ColorDrawable(Color.argb(alpha, 0, 0, 0))
        }

        override fun onExit() {
            actionListener?.onExit()
        }
    }

    private val exitAnimatorCallback = object : DraggableZoomCore.ExitAnimatorCallback {
        override fun onStartInitAnimatorParams() {
            mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.view_draggable_simple_image, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        setOnClickListener {
            clickToExit()
        }

        mDraggableImageViewPhotoView.setOnClickListener {
            clickToExit()
        }

        mDraggableImageViewViewOriginImage.setOnClickListener {
            loadImage(draggableImageInfo?.originImg ?: "", false)
        }
        mDraggableImageViewViewOProgressBar.indeterminateDrawable.setColorFilter(
            Color.parseColor("#ebebeb"),
            PorterDuff.Mode.MULTIPLY
        )
    }

    private fun clickToExit() {
        if (draggableZoomCore?.isAnimating == true) return
        mDraggableImageViewViewOProgressBar.visibility = View.GONE
        if (mDraggableImageViewPhotoView.scale != 1f) {
            mDraggableImageViewPhotoView.setScale(1f, true)
        } else {
            draggableZoomCore?.adjustScaleViewToCorrectLocation()
            draggableZoomCore?.exitWithAnimator(false)
            downloadDisposable?.dispose()
        }
    }

    /**
     * 拖拽支持
     * */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val isIntercept = super.onInterceptTouchEvent(ev)
        if (draggableZoomCore?.isAnimating == true) {
            return false
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
        currentLoadUrl = ""
        refreshOriginImageInfo()
        GlideHelper.retrieveImageWhRadioFromMemoryCache(
            context,
            paramsInfo.thumbnailImg
        ) { inMemCache, whRadio, isGif ->
            draggableImageInfo?.draggableInfo?.scaledViewWhRadio = whRadio
            post {

                viewSelfWhRadio = (width * 1f / height)
                needFitCenter = whRadio > viewSelfWhRadio
                if (!paramsInfo.draggableInfo.isValid() || (isGif && !needFitCenter) ) {
                    //退出的时候不要再开启动画
                    paramsInfo.draggableInfo = DraggableParamsInfo()
                    showImage(paramsInfo)
                    return@post
                }

                draggableZoomCore = DraggableZoomCore(
                    paramsInfo.draggableInfo,
                    mDraggableImageViewPhotoView,
                    width,
                    height,
                    draggableZoomActionListener,
                    exitAnimatorCallback
                )
                draggableZoomCore?.adjustScaleViewToInitLocation()
                loadAvailableImage(true, inMemCache)
            }
        }
    }

    fun showImage(paramsInfo: DraggableImageInfo) {
        draggableImageInfo = paramsInfo
        currentLoadUrl = ""
        refreshOriginImageInfo()
        GlideHelper.retrieveImageWhRadioFromMemoryCache(
            context,
            paramsInfo.thumbnailImg
        ) { inMemCache, whRadio , isGif->
            draggableImageInfo?.draggableInfo?.scaledViewWhRadio = whRadio
            post {
                viewSelfWhRadio = (width * 1f / height)
                needFitCenter = whRadio > viewSelfWhRadio

                draggableZoomCore = DraggableZoomCore(
                    paramsInfo.draggableInfo,
                    mDraggableImageViewPhotoView,
                    width,
                    height,
                    draggableZoomActionListener,
                    exitAnimatorCallback
                )
                draggableZoomCore?.adjustScaleViewToCorrectLocation()
                loadAvailableImage(false, inMemCache)
            }
        }
    }

    private fun loadAvailableImage(startAnimator: Boolean, imgInMemCache: Boolean) {
        if ((context as? AppCompatActivity)?.isDestroyed == true || (context as? AppCompatActivity)?.isFinishing == true) {
            return
        }

        mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.FIT_CENTER
        mDraggableImageViewPhotoView.setImageResource(R.drawable.place_holder_transparent)

        val thumnailImg = draggableImageInfo!!.thumbnailImg
        val originImg = draggableImageInfo!!.originImg

        val wifiIsConnect = Utils.isWifiConnected(context)

        val originImgInCache = GlideHelper.imageIsInCache(context, originImg)

        val targetUrl = if (wifiIsConnect || originImgInCache) originImg else thumnailImg

        setViewOriginImageBtnVisible(targetUrl != originImg)

        if (imgInMemCache) {
            loadImage(thumnailImg, originImgInCache)
        }

        if (imgInMemCache && startAnimator) {  //只有缩略图在缓存中时，才播放缩放入场动画
            draggableZoomCore?.enterWithAnimator(object :
                DraggableZoomCore.EnterAnimatorCallback {
                override fun onEnterAnimatorStart() {
                    mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.CENTER_CROP
                }

                override fun onEnterAnimatorEnd() {
                    if (needFitCenter) {
                        mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.FIT_CENTER
                        draggableZoomCore?.adjustViewToMatchParent()
                    }
                    loadImage(targetUrl, originImgInCache)
                }
            })
        } else {
            loadImage(targetUrl, originImgInCache)
            if (needFitCenter) {
                draggableZoomCore?.adjustViewToMatchParent()
            }
        }
    }

    private fun loadImage(url: String, originIsInCache: Boolean) {

        if (url == currentLoadUrl) return

        if ((context is Activity) && ((context as Activity).isFinishing || (context as Activity).isDestroyed)) {
            return
        }

        currentLoadUrl = url

        if (url == draggableImageInfo?.originImg && !originIsInCache) {
            mDraggableImageViewViewOProgressBar.visibility = View.VISIBLE
        }

        val options = RequestOptions().priority(Priority.HIGH) //被查看的图片应该由最高的请求优先级

        Glide.with(context)
            .load(url)
            .apply(options)
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    val isGif = resource is GifDrawable
                    mDraggableImageViewViewOProgressBar.visibility = View.GONE
                    val whRadio = resource.intrinsicWidth * 1f / resource.intrinsicHeight
                    val longImage = whRadio < viewSelfWhRadio

                    if (isGif) {
                        if (longImage){
                            mDraggableImageViewPhotoView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        }
                        Glide.with(context).load(url).into(mDraggableImageViewPhotoView)
                    } else {
                        //普通图片已经做了缩放 -> 宽度缩放至屏幕的宽度
                        mDraggableImageViewPhotoView.scaleType = if (longImage)ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
                        mDraggableImageViewPhotoView.setImageBitmap(translateToFixedBitmap(resource))
                    }

                    if (url == draggableImageInfo?.originImg) {
                        mDraggableImageViewViewOriginImage.visibility = View.GONE
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    mDraggableImageViewViewOProgressBar.visibility = View.GONE
                }
            })
    }

    //avoid oom
    private fun translateToFixedBitmap(originDrawable: Drawable): Bitmap? {
        val whRadio = originDrawable.intrinsicWidth * 1f / originDrawable.intrinsicHeight

        val screenWidth = Utils.getScreenWidth()

        var bpWidth = if (this@DraggableImageView.width != 0) {
            if (originDrawable.intrinsicWidth > this@DraggableImageView.width) {
                this@DraggableImageView.width
            } else {
                originDrawable.intrinsicWidth
            }
        } else {
            if (originDrawable.intrinsicWidth > screenWidth) {
                screenWidth
            } else {
                originDrawable.intrinsicWidth
            }
        }

        if (bpWidth > screenWidth) bpWidth = screenWidth

        val bpHeight = (bpWidth * 1f / whRadio).toInt()

        Log.d(TAG, "bpWidth : $bpWidth  bpHeight : $bpHeight")

        var bp = Glide.get(context).bitmapPool.get(
            bpWidth,
            bpHeight,
            if (bpHeight > 4000) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
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

    private fun refreshOriginImageInfo() {
        if (draggableImageInfo!!.imageSize > 0) {
            mDraggableImageViewViewOriginImage.text =
                "查看原图(${Utils.formatImageSize(draggableImageInfo?.imageSize ?: 0)})"
        } else {
            mDraggableImageViewViewOriginImage.text = "查看原图"
        }
    }

    private fun setViewOriginImageBtnVisible(visible: Boolean) {
        mDraggableImageViewViewOriginImage.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun closeWithAnimator() {
        draggableZoomCore?.adjustScaleViewToCorrectLocation()
        draggableZoomCore?.exitWithAnimator(false)
        downloadDisposable?.dispose()
    }

}