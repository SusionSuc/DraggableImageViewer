package com.draggable.library.core

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
    private val DRAG_EXIT_DISTANCE = 230

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
            startEnterAnimator()
        }
    }

    fun setParamsAndStartEnterAnimate(paramsInfo: DraggableParamsInfo, childView: View) {
        draggableParams = paramsInfo
        setScaleChildView(childView)
        post {
            adjustAnimateParams(paramsInfo)
            startEnterAnimator()
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
//        Log.d(TAG, "mCurrentWidth : $mCurrentWidth ; mCurrentHeight : $mCurrentHeight;  mCurrentTranslateX : $mCurrentTranslateX ; mCurrentTransLateY : $mCurrentTransLateY")
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
                return true
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
        if (mCurrentTransLateY > 0) {
            if (mCurrentTransLateY > DRAG_EXIT_DISTANCE) {
                startExitAnimator()
            } else {
                startRestoreInitAnimator()
            }
        } else {
            if (isClickEvent) {
                isClickEvent = false
                startExitAnimator()
            }
        }
    }

    private fun onActionMove(event: MotionEvent) {
        var offsetY = Math.abs(event.y - mDownY)
        val offsetX = Math.abs(event.x - mDownX)

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
    private fun startEnterAnimator() {
        val dx = mCurrentTranslateX - 0
        val dy = mCurrentTransLateY - mTargetTranslateY
        val dWidth = width - draggableParams.viewWidth
        val dHeight = maxHeight - draggableParams.viewHeight
        Log.d(TAG, "dx:$dx  dy:$dy  dWidth : $dWidth  dHeight:$dHeight")
        val newEnterAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
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
        newEnterAnimator.start()
    }

    /**
     * exit animator
     * */
    fun startExitAnimator() {
        val scaleWidth = width * mCurrentScaleX
        val scaleHeight = maxHeight * mCurrentScaleY
//        mCurrentTranslateX += width * (1 - mCurrentScaleX) / 2
        mCurrentScaleX = 1f
        mCurrentScaleY = 1f

        val dx = mCurrentTranslateX - draggableParams.viewLeft
        val dy = mCurrentTransLateY - draggableParams.viewTop
        val dWidth = scaleWidth - draggableParams.viewWidth
        val dHeight = scaleHeight - draggableParams.viewHeight

        Log.d(
            TAG,
            "mCurrentTranslateX:$mCurrentTranslateX  mCurrentTransLateY:$mCurrentTransLateY  scaleWidth:$scaleWidth  scaleHeight:$scaleHeight"
        )
        Log.d(TAG, "dx:$dx  dy:$dy  dWidth:$dWidth  dHeight:$dHeight")

        val newExitAnimator = ValueAnimator.ofFloat(1f, 0f).apply {
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
        newExitAnimator.start()
    }

    //用户没有触发拖拽退出，还原状态
    private fun startRestoreInitAnimator() {
        val scaleYAnimator = ValueAnimator.ofFloat(mCurrentScaleY, 1f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener {
                mCurrentScaleY = it.animatedValue as Float
                changeChildViewDragParams()
            }
        }

        val scaleXAnimator = ValueAnimator.ofFloat(mCurrentScaleX, 1f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener {
                mCurrentScaleX = it.animatedValue as Float
            }
        }

        val translateXRestoreAnimator = ValueAnimator.ofFloat(mCurrentTranslateX, 0f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener { valueAnimator -> mCurrentTranslateX = valueAnimator.animatedValue as Float }
        }

        val translateYRestoreAnimator = ValueAnimator.ofFloat(mCurrentTransLateY, 0f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener { valueAnimator -> mCurrentTransLateY = valueAnimator.animatedValue as Float }
        }

        val alphaRestoreAnimator = ValueAnimator.ofInt(mAlpha, 255).apply {
            duration = ANIMATOR_DURATION
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
            duration = ANIMATOR_DURATION
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