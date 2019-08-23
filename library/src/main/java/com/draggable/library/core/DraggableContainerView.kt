package com.draggable.library.core

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * 可以缩放子View  && 拖拽退出 的容器View
 * */
class DraggableContainerView : FrameLayout {

    interface ActionListener {
        fun onExit() // drag to exit
    }

    private val TAG = javaClass.simpleName

    var scaledChildView: View? = null

    var actionListener: ActionListener? = null
    lateinit var draggableParams: DraggableParamsInfo

    //core animator params
    private val ANIMATOR_DURATION = 300L
    private var mAlpha = 0
    private var mTargetTranslateY = 1f
    private var mCurrentTransLateY: Float = 0.toFloat()
    private var mCurrentTranslateX: Float = 0.toFloat()
    private var mCurrentScaleX = 1f
    private var mCurrentScaleY = 1f
    private var isAutoAnimating = false
    private var minScaleXY = 0.3f
    private var maxHeight = 1f
    private var mCurrentWidth: Int = 0
    private var mCurrentHeight: Int = 0

    //drag event params
    private var mDownX: Float = 0f
    private var mDownY: Float = 0f
    private val MAX_TRANSLATE_Y = 500

    private var isClickEvent = false

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    private fun initView() {

    }

    fun setScaleChildView(view: View) {
        if (scaledChildView?.parent != null) {
            removeView(scaledChildView)
        }
        scaledChildView = view
        addView(scaledChildView)
    }

    fun setParamsAndStartEnterAnimate(paramsInfo: DraggableParamsInfo) {
        draggableParams = paramsInfo
        post {
            adjustAnimateParams(paramsInfo)
            enterWithAnimator()
        }
    }

    fun setParamsAndStartEnterAnimate(paramsInfo: DraggableParamsInfo, childView: View) {
        draggableParams = paramsInfo
        setScaleChildView(childView)
        post {
            adjustAnimateParams(paramsInfo)
            enterWithAnimator()
        }
    }

    private fun adjustAnimateParams(paramsInfo: DraggableParamsInfo) {
        mCurrentHeight = paramsInfo.viewHeight
        mCurrentWidth = paramsInfo.viewWidth
        mCurrentTranslateX = paramsInfo.viewLeft.toFloat()
        mCurrentTransLateY = paramsInfo.viewTop.toFloat()
        maxHeight = width / paramsInfo.contentWHRadio
        mTargetTranslateY = (height - maxHeight) / 2
        Log.d(
            TAG,
            "width $width  height $height  mCurrentWidth : $mCurrentWidth  mCurrentHeight : $mCurrentHeight mTargetTranslateY : $mTargetTranslateY maxHeight : $maxHeight"
        )
    }

    //child view scale core params
    private fun changeChildViewAnimateParams() {
        scaledChildView?.apply {
            layoutParams = LayoutParams(mCurrentWidth, mCurrentHeight)
            translationX = mCurrentTranslateX
            translationY = mCurrentTransLateY
            scaleX = mCurrentScaleX
            scaleY = mCurrentScaleY
        }
        background = ColorDrawable(Color.argb(mAlpha, 0, 0, 0))
    }

    private fun changeChildViewDragParams() {
        scaledChildView?.apply {
            translationX = mCurrentTranslateX
            translationY = mCurrentTransLateY
            scaleX = mCurrentScaleX
            scaleY = mCurrentScaleY
        }
        background = ColorDrawable(Color.argb(mAlpha, 0, 0, 0))
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (isAutoAnimating) return super.dispatchTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x
                mDownY = event.y
                isClickEvent = true
            }
            MotionEvent.ACTION_MOVE -> {
                isClickEvent = false
                if (event.pointerCount == 1) {
                    //在viewpager中的话， 避免过分拦截事件
                    val dx = event.x - mDownX
                    val dy = event.y - mDownY
                    if (Math.abs(dx) > Math.abs(dy)) {
                        super.dispatchTouchEvent(event)
                    } else {
                        if (dy > 0) {
                            onActionMove(event)
                        }else{
                            scaledChildView?.dispatchTouchEvent(event)
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (event.pointerCount == 1) {
                    onActionUp()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun onActionUp() {
        Log.d(TAG, "mCurrentTransLateY : $mCurrentTransLateY")
        if (mCurrentScaleY != 1f) {
            if (mCurrentScaleY < 0.5) {
                exitWithAnimator()
            } else {
                isClickEvent = false
                restoreStatusWithAnimator()
            }
        } else {
            if (isClickEvent) {
                isClickEvent = false
                exitWithAnimator()
            }
        }
    }

    private fun onActionMove(event: MotionEvent) {
        var offsetY = event.y - mDownY
        val offsetX = event.x - mDownX

        if (offsetY > MAX_TRANSLATE_Y) {
            offsetY = MAX_TRANSLATE_Y.toFloat()
        }

        val percent = offsetY / MAX_TRANSLATE_Y

        mCurrentTransLateY = mTargetTranslateY + offsetY
        mCurrentTranslateX = offsetX

        mCurrentScaleX = 1 - percent
        mCurrentScaleY = 1 - percent

        if (mCurrentScaleX <= minScaleXY) {
            mCurrentScaleX = minScaleXY
        }

        if (mCurrentScaleY <= minScaleXY) {
            mCurrentScaleY = minScaleXY
        }

        mCurrentWidth = (width * mCurrentScaleX).toInt()
        mCurrentHeight = (height * mCurrentScaleY).toInt()

        mAlpha = (255 - 255 * percent).toInt()

        changeChildViewDragParams()
    }

    /**
     * enter animator
     * */
    private fun enterWithAnimator() {
        val dx = mCurrentTranslateX - 0
        val dy = mCurrentTransLateY - mTargetTranslateY
        val dWidth = width - draggableParams.viewWidth
        val dHeight = maxHeight - draggableParams.viewHeight
        Log.d(TAG, "dx:$dx  dy:$dy  dWidth : $dWidth  dHeight:$dHeight")
        val enterAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener {
                val percent = it.animatedValue as Float
                mCurrentTranslateX = draggableParams.viewLeft - dx * percent
                mCurrentTransLateY = draggableParams.viewTop - dy * percent
                mCurrentWidth = draggableParams.viewWidth + (dWidth * percent).toInt()
                mCurrentHeight = draggableParams.viewHeight + (dHeight * percent).toInt()
                mAlpha = (255 * percent).toInt()
                changeChildViewAnimateParams()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isAutoAnimating = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAutoAnimating = false
                }
            })
        }
        enterAnimator.start()
    }

    /**
     * exit animator
     * */
    fun exitWithAnimator() {
        val scaleWidth = width * mCurrentScaleX
        val scaleHeight = maxHeight * mCurrentScaleY
        mCurrentTranslateX += width * (1 - mCurrentScaleX) / 2
        mCurrentTransLateY += maxHeight * (1 - mCurrentScaleY) / 2
        mCurrentScaleX = 1f
        mCurrentScaleY = 1f

        val dx = mCurrentTranslateX - draggableParams.viewLeft
        val dy = mCurrentTransLateY - draggableParams.viewTop
        val dWidth = scaleWidth - draggableParams.viewWidth
        val dHeight = scaleHeight - draggableParams.viewHeight

        val exitAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener {
                val percent = it.animatedValue as Float
                mCurrentTranslateX = draggableParams.viewLeft + dx * percent
                mCurrentTransLateY = draggableParams.viewTop + dy * percent
                mCurrentWidth = draggableParams.viewWidth + (dWidth * percent).toInt()
                mCurrentHeight = draggableParams.viewHeight + (dHeight * percent).toInt()
                mAlpha = (mAlpha * percent).toInt()
                changeChildViewAnimateParams()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isAutoAnimating = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAutoAnimating = false
                    actionListener?.onExit()
                }
            })
        }
        exitAnimator.start()
    }

    //用户没有触发拖拽退出，还原状态
    private fun restoreStatusWithAnimator() {

        val initAlpha = mAlpha
        val dyAlpha = 255 - mAlpha

        val initScaleX = mCurrentScaleX
        val dScaleX = 1 - mCurrentScaleX

        val initScaleY = mCurrentScaleY
        val dScaleY = 1 - mCurrentScaleY
        val initX = mCurrentTranslateX
        val dx = 0 - mCurrentTranslateX

        val initY = mCurrentTransLateY
        val dy = mTargetTranslateY - mCurrentTransLateY

        val restoreAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener {
                val percent = it.animatedValue as Float
                mCurrentTransLateY = initY + (dy * percent)
                mCurrentTranslateX = initX + (dx * percent)

                mCurrentScaleY = initScaleY + (dScaleY * percent)
                mCurrentScaleX = initScaleX + (dScaleX * percent)
                mAlpha = initAlpha + (dyAlpha * percent).toInt()

                changeChildViewDragParams()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    isAutoAnimating = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAutoAnimating = false
                }
            })
        }
        restoreAnimator.start()
    }

}