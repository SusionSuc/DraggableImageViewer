package com.draggable.library.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import com.draggable.library.photoview.PhotoView
import com.draggable.library.ImageViewImageInfo

/**
 * 可拖拽的ImageView
 * */
class MiHoYoBigImageView : FrameLayout {

    private val TAG = "MiHoYoBigImageView"

    private var mDownX: Float = 0f
    private var mDownY: Float = 0f

    private val MAX_TRANSLATE_Y = 1000
    private val DRAG_EXIT_DISTANCE = 500

    private var isAnimating = false

    //is event on PhotoView
    var actionListener: ActionListener? = null

    val photoView: PhotoView = PhotoView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        scaleType = ImageView.ScaleType.FIT_XY
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    private fun initView() {
        addView(photoView)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        if (isAnimating) return false
        //当前照片处于缩放状态下，不支持拖拽缩放
        if (photoView.scale != 1f) return photoView.dispatchTouchEvent(event)

        Log.d(TAG, "event.pointerCount : ${event.pointerCount}")

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x
                mDownY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                Log.d(TAG, "拖拽处理 ACTION_MOVE")
                if (event.pointerCount == 1) {
                    //在viewpager中的话， 避免过分拦截事件
                    val dx = event.x - mDownX
                    val dy = event.y - mDownY
                    if (Math.abs(dx) > Math.abs(dy)) {
                        Log.d(TAG, "横滑， 事件交给ViewPager！")
                        super.dispatchTouchEvent(event)
                    } else {
                        if (dy > 0) {
                            Log.d(TAG, "ACTION_MOVE， onActionMove！")
                            onActionMove(event)
                        } else {
                            photoView.dispatchTouchEvent(event)
                        }
                    }
                } else {
                    photoView.dispatchTouchEvent(event)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (event.pointerCount == 1) {
                    Log.d(TAG, "拖拽处理 ACTION_UP")
                    onActionUp()
                }
            }
        }

        return photoView.dispatchTouchEvent(event)
    }

    private fun onActionUp() {
        if (actualTranslateY > 0) {
            if (actualTranslateY > DRAG_EXIT_DISTANCE) {
                startExitAnimator()
            } else {
                startRestoreInitAnimator()
            }
        } else {
            postDelayed({
                if (photoView.scale == 1f) {
                    startExitAnimator()
                }
            }, 200)
        }
    }

    private fun onActionMove(event: MotionEvent) {
        var offsetY = Math.abs(event.y - mDownY)
        val offsetX = Math.abs(event.x - mDownX)

        if (offsetY > MAX_TRANSLATE_Y) {
            offsetY = MAX_TRANSLATE_Y.toFloat()
        }

        val percent = offsetY / MAX_TRANSLATE_Y

        actualTranslateY = (offsetY * 1.5).toFloat()
        mCurrentTransLateY = mMinTransLateY + actualTranslateY
        mCurrentTranslateX = (offsetX * 1.5).toFloat()

        mCurrentScaleX = mMaxScaleX - (mMaxScaleX - 1f) * percent
        mCurrentScaleY = mMaxScaleY - (mMaxScaleY - 1f) * percent
        mAlpha = (255 - 255 * percent).toInt()

        animatingForEnterOrExit = false

        changeContentViewAnimateParams()
    }

    /**
     *  进入和退出动画的实现
     */
    private var originWidth: Int = 0
    private var originHeight: Int = 0
    private var originLeft: Int = 0
    private var originTop: Int = 0

    private var photoViewHeight: Float = 0f

    private var mMaxScaleX = 0f
    private var mMaxScaleY = 0f

    private var actualTranslateY = 0f
    private var mMinTransLateY = 0f
    private var animatingForEnterOrExit = false

    private var mCurrentTransLateY: Float = 0.toFloat()
    private var mCurrentTranslateX: Float = 0.toFloat()
    private var mCurrentScaleX = 1f
    private var mCurrentScaleY = 1f
    private var mAlpha = 0

    private var hasExit = false

    //不支持拖拽归位, 没有图片宽高信息
    fun setImageWithoutInfo(imgInfo: ImageViewImageInfo) {
//        imgInfo.apply {
//
//        }
        post {
            photoViewHeight = (imgInfo.imageHeight * 1f / imgInfo.imageWidth) * width
            mMinTransLateY = (height - photoViewHeight) * 1.0f / 2

            val minScaleRadio = 0.3f
            originWidth = (width * minScaleRadio).toInt()
            originHeight = (photoViewHeight * minScaleRadio).toInt()
            originLeft = 500
            originTop = 3000
            initAnimateParams()
            mCurrentTranslateX = 0f
            mCurrentTransLateY = mMinTransLateY
            mAlpha = 255
            changeContentViewAnimateParams()
        }
    }

    //支持拖拽归位
    fun setImageInfo(imgInfo: ImageViewImageInfo) {
        post {
            initBaseParams(imgInfo)
            initAnimateParams()
            mCurrentTranslateX = 0f
            mCurrentTransLateY = mMinTransLateY
            mAlpha = 255
            changeContentViewAnimateParams()
        }
    }

    //设置图片时， 带有入场动画效果
    fun setImageInfoWithAnimate(imgInfo: ImageViewImageInfo) {
        post {
            initBaseParams(imgInfo)
            initAnimateParams()
            startEnterAnimator(200)
        }
    }

    private fun initBaseParams(imgInfo: ImageViewImageInfo) {
        originWidth = imgInfo.viewWidth
        originHeight = imgInfo.viewHeight
        originLeft = imgInfo.viewLeft
        originTop = imgInfo.viewTop
        val imgaeRadito = imgInfo.imageHeight * 1f / imgInfo.imageWidth
        photoViewHeight = imgaeRadito * width
        if (photoViewHeight > height) {
            photoViewHeight = height.toFloat()
            photoView.setScaleLevels(1f, 1.5f, imgaeRadito)
            postDelayed({
                photoView.setScale(imgaeRadito, false)
            }, 200) //
            photoView.scaleType = ImageView.ScaleType.FIT_CENTER
        } else {
            photoView.scaleType = ImageView.ScaleType.FIT_XY
        }
        mMinTransLateY = (height - photoViewHeight) * 1.0f / 2
    }

    private fun initAnimateParams() {
        mCurrentScaleX = width * 1f / originWidth
        mCurrentScaleY = photoViewHeight / originHeight
        mCurrentTranslateX = originLeft.toFloat()
        mCurrentTransLateY = originTop.toFloat()
        mMaxScaleX = mCurrentScaleX //还原动画时使用
        mMaxScaleY = mCurrentScaleY
    }

    private fun changeContentViewAnimateParams() {
        photoView.apply {
            layoutParams = LayoutParams(originWidth, originHeight)  //初始化到正确的位置
            translationX = mCurrentTranslateX
            translationY = mCurrentTransLateY
            scaleX = mCurrentScaleX
            scaleY = mCurrentScaleY
            pivotX = 0f
            pivotY = 0f
        }
        background = ColorDrawable(Color.argb(mAlpha, 0, 0, 0))
    }

    /**
     * 入场动画
     * */
    private fun startEnterAnimator(time: Long = 200L) {
        Log.d(TAG, "startEnterAnimator")
        val translateXAnimator = ValueAnimator.ofFloat(mCurrentTranslateX, 0f).apply {
            duration = time
            addUpdateListener {
                mCurrentTranslateX = it.animatedValue as Float
                changeContentViewAnimateParams()
            }
        }

        val translateYAnimator = ValueAnimator.ofFloat(mCurrentTransLateY, mMinTransLateY).apply {
            duration = time
            addUpdateListener {
                mCurrentTransLateY = it.animatedValue as Float
            }
        }

        val scaleYAnimator = ValueAnimator.ofFloat(1f, mCurrentScaleY).apply {
            duration = time
            addUpdateListener {
                mCurrentScaleY = it.animatedValue as Float
            }
        }

        val scaleXAnimator = ValueAnimator.ofFloat(1f, mCurrentScaleX).apply {
            duration = time
            addUpdateListener {
                mCurrentScaleX = it.animatedValue as Float
            }
        }

        val alphaAnimator = ValueAnimator.ofInt(0, 255).apply {
            duration = time
            addUpdateListener {
                mAlpha = it.animatedValue as Int
            }
        }

        val enterAnimator = AnimatorSet().apply {
            playTogether(translateXAnimator, translateYAnimator, scaleYAnimator, scaleXAnimator, alphaAnimator)
            duration = time
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isAnimating = true
                    animatingForEnterOrExit = true

                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                    animatingForEnterOrExit = false
                }
            })
        }
        enterAnimator.start()
    }

    /**
     * 退出动画
     * */
    fun startExitAnimator() {

//        photoView.scaleType = ImageView.ScaleType.CENTER_CROP

        Log.d(TAG, "exitWithAnimator")
        val ENTER_ANIMATOR_DURATION = 200L

        val translateXAnimator = ValueAnimator.ofFloat(mCurrentTranslateX, originLeft.toFloat()).apply {
            duration = ENTER_ANIMATOR_DURATION
            addUpdateListener {
                mCurrentTranslateX = it.animatedValue as Float
                changeContentViewAnimateParams()
            }
        }

        val translateYAnimator = ValueAnimator.ofFloat(mCurrentTransLateY, originTop.toFloat()).apply {
            duration = ENTER_ANIMATOR_DURATION
            addUpdateListener {
                mCurrentTransLateY = it.animatedValue as Float
            }
        }

        val scaleYAnimator = ValueAnimator.ofFloat(mCurrentScaleY, 1f).apply {
            duration = ENTER_ANIMATOR_DURATION
            addUpdateListener {
                mCurrentScaleY = it.animatedValue as Float
            }
        }

        val scaleXAnimator = ValueAnimator.ofFloat(mCurrentScaleX, 1f).apply {
            duration = ENTER_ANIMATOR_DURATION
            addUpdateListener {
                mCurrentScaleX = it.animatedValue as Float
            }
        }

        val alphaAnimator = ValueAnimator.ofInt(mAlpha, 0).apply {
            duration = ENTER_ANIMATOR_DURATION
            addUpdateListener {
                mAlpha = it.animatedValue as Int
            }
        }

        val enterAnimator = AnimatorSet().apply {
            playTogether(translateXAnimator, translateYAnimator, scaleYAnimator, scaleXAnimator, alphaAnimator)
            duration = ENTER_ANIMATOR_DURATION
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isAnimating = true
                    animatingForEnterOrExit = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                    hasExit = true
                    animatingForEnterOrExit = false
                    actionListener?.onExit()
                }
            })
        }
        enterAnimator.start()
    }

    //用户没有触发拖拽退出，还原状态
    private fun startRestoreInitAnimator() {
        //还原动画
        val RESTORE_DURATION: Long = 200

        val scaleYAnimator = ValueAnimator.ofFloat(mCurrentScaleY, mMaxScaleY).apply {
            duration = RESTORE_DURATION
            addUpdateListener {
                mCurrentScaleY = it.animatedValue as Float
                changeContentViewAnimateParams()
            }
        }

        val scaleXAnimator = ValueAnimator.ofFloat(mCurrentScaleX, mMaxScaleX).apply {
            duration = RESTORE_DURATION
            addUpdateListener {
                mCurrentScaleX = it.animatedValue as Float
            }
        }

        val translateXRestoreAnimator = ValueAnimator.ofFloat(mCurrentTranslateX, 0f).apply {
            duration = RESTORE_DURATION
            addUpdateListener { valueAnimator -> mCurrentTranslateX = valueAnimator.animatedValue as Float }
        }

        val translateYRestoreAnimator = ValueAnimator.ofFloat(mCurrentTransLateY, mMinTransLateY).apply {
            duration = RESTORE_DURATION
            addUpdateListener { valueAnimator -> mCurrentTransLateY = valueAnimator.animatedValue as Float }
        }

        val alphaRestoreAnimator = ValueAnimator.ofInt(mAlpha, 255).apply {
            duration = RESTORE_DURATION
            addUpdateListener { valueAnimator -> mAlpha = valueAnimator.animatedValue as Int }
        }

        val restoreAnimator = AnimatorSet().apply {
            playTogether(
                scaleYAnimator,
                scaleXAnimator,
                translateXRestoreAnimator,
                translateYRestoreAnimator,
                alphaRestoreAnimator
            )
            duration = RESTORE_DURATION
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                }
            })
        }

        restoreAnimator.start()
    }

    interface ActionListener {
        fun onExit()
    }

}