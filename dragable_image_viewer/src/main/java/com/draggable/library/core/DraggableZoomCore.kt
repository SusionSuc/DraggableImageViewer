package com.draggable.library.core

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.View

/**
 * 把一个View 从一个地方移动到另一个地方 & 伴随缩放事件。
 * */
class DraggableZoomCore(
    private var draggableParams: DraggableParamsInfo,
    private val scaleDraggableView: View,
    private val mContainerWidth: Int,
    private val mContainerHeight: Int,
    val actionListener: ActionListener? = null,
    val exitCallback: ExitAnimatorCallback? = null
) {

    private val TAG = javaClass.simpleName

    //core animator params
    private val ANIMATOR_DURATION = 200L
    private var mAlpha = 0
    private var mCurrentTransLateY: Float = 0.toFloat()
    private var mCurrentTranslateX: Float = 0.toFloat()
    private var mCurrentScaleX = 1f
    private var mCurrentScaleY = 1f
    var isAnimating = false
    private var minScaleXY = 0.3f
    private var maxHeight = 1f
    private var mCurrentWidth: Int = 0
    private var mCurrentHeight: Int = 0
    private var mTargetTranslateY = 0f

    //drag event params
    private var mDownX: Float = 0f
    private var mDownY: Float = 0f
    private val MAX_TRANSLATE_Y = 1500

    fun adjustScaleViewToInitLocation() {
        if (draggableParams.isValid()) {
            mCurrentHeight = draggableParams.viewHeight
            mCurrentWidth = draggableParams.viewWidth
            mCurrentTranslateX = draggableParams.viewLeft.toFloat()
            mCurrentTransLateY = draggableParams.viewTop.toFloat()
            maxHeight = mContainerWidth / draggableParams.scaledViewWhRadio
            if (maxHeight > mContainerHeight) {
                maxHeight = mContainerHeight.toFloat()
            }
            mTargetTranslateY = (mContainerHeight - maxHeight) / 2
        }
    }

    fun adjustScaleViewToCorrectLocation() {
        if (draggableParams.isValid()) {
            maxHeight = mContainerWidth / draggableParams.scaledViewWhRadio
            if (maxHeight > mContainerHeight) {
                maxHeight = mContainerHeight.toFloat()
            }
            mCurrentHeight = maxHeight.toInt()
            mCurrentWidth = mContainerWidth
            mCurrentTranslateX = 0f
            mCurrentTransLateY = (mContainerHeight - maxHeight) / 2
            mTargetTranslateY = mCurrentTransLateY
        } else {
            mCurrentWidth = mContainerWidth
            mCurrentHeight = mContainerHeight
            mCurrentTranslateX = 0f
            mCurrentTransLateY = 0f
            mTargetTranslateY = 0f
        }
        mAlpha = 255
        changeChildViewAnimateParams()
    }

    //兼容 photo view, 使photo view 的缩放功能体验更好
    fun adjustViewToMatchParent() {
        mCurrentWidth = mContainerWidth
        mCurrentHeight = mContainerHeight
        mCurrentTranslateX = 0f
        mCurrentTransLateY = 0f
        mTargetTranslateY = 0f
        changeChildViewAnimateParams()
    }

    //child view scale core params
    private fun changeChildViewAnimateParams() {
        scaleDraggableView.apply {
            layoutParams = layoutParams?.apply {
                width = mCurrentWidth
                height = mCurrentHeight
            }
            translationX = mCurrentTranslateX
            translationY = mCurrentTransLateY
            scaleX = mCurrentScaleX
            scaleY = mCurrentScaleY
        }
        actionListener?.currentAlphaValue(mAlpha)
    }

    private fun changeChildViewDragParams() {
        scaleDraggableView.apply {
            translationX = mCurrentTranslateX
            translationY = mCurrentTransLateY
            scaleX = mCurrentScaleX
            scaleY = mCurrentScaleY
        }
        actionListener?.currentAlphaValue(mAlpha)
    }

    //是否可以拦截事件
    fun onInterceptTouchEvent(intercept: Boolean, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onInterceptTouchEvent  ACTION_DOWN")
                mDownX = event.x
                mDownY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                //下滑手势
                if (event.pointerCount == 1) {
                    val dx = event.x - mDownX
                    val dy = event.y - mDownY
                    if (Math.abs(dx) > Math.abs(dy)) {
                        Log.d(TAG, "不拦截横滑事件...")
                        return false
                    }

                    if (dy > 0) {
                        return true
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                Log.d(TAG, "ACTION_UP...")
            }
        }

        Log.d(TAG, "DraggableZoomCore onInterceptTouchEvent  intercept : $intercept")
        return intercept
    }

    //处理用户拖拽事件
    fun onTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "onTouchEvent  ACTION_DOWN")
                mDownX = event.x
                mDownY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (mDownX == 0f && mDownY == 0f) { //fix 有时 Action Down 没有回调的问题
                    mDownX = event.x
                    mDownY = event.y
                }
                val dx = event.x - mDownX
                val dy = event.y - mDownY
                onActionMove(dx, dy)
            }
            MotionEvent.ACTION_UP -> {
                if (event.pointerCount == 1) {
                    if (mCurrentScaleY != 1f) {
                        if (mCurrentScaleY < 0.85) {
                            exitWithAnimator(true)
                        } else {
                            restoreStatusWithAnimator()
                        }
                    }

                    if (mCurrentTransLateY < mTargetTranslateY){
                        restoreStatusWithAnimator()
                    }
                }
            }
        }
    }

    private fun onActionMove(offsetX: Float, offsetY: Float) {
        var percent = offsetY / MAX_TRANSLATE_Y
        if (percent > 1) {
            percent = 1f
        }
        if (percent < 0) {
            percent = 0f
        }

        mCurrentTransLateY = mTargetTranslateY + offsetY
        mCurrentTranslateX = offsetX

        mCurrentScaleX = 1 - percent
        mCurrentScaleY = 1 - percent

        if (mCurrentScaleX <= minScaleXY) mCurrentScaleX = minScaleXY
        if (mCurrentScaleY <= minScaleXY) mCurrentScaleY = minScaleXY

        if (mCurrentScaleX > 1) mCurrentScaleX = 1f
        if (mCurrentScaleY > 1) mCurrentScaleY = 1f

        mCurrentWidth = (mContainerWidth * mCurrentScaleX).toInt()
        mCurrentHeight = (mContainerHeight * mCurrentScaleY).toInt()

        mAlpha = (255 - 255 * percent).toInt()
        changeChildViewDragParams()
    }

    /**
     * enter animator
     * */
    fun enterWithAnimator(animatorCallback: EnterAnimatorCallback? = null) {
        if (!draggableParams.isValid()) return
        val dx = mCurrentTranslateX - 0
        val dy = mCurrentTransLateY - mTargetTranslateY
        val dWidth = mContainerWidth - draggableParams.viewWidth
        val dHeight = maxHeight - draggableParams.viewHeight
        Log.d(TAG, "enterWithAnimator : dx:$dx  dy:$dy  dWidth : $dWidth xss dHeight:$dHeight")
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
                    isAnimating = true
                    animatorCallback?.onEnterAnimatorStart()
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                    animatorCallback?.onEnterAnimatorEnd()
                }
            })
        }
        enterAnimator.start()
    }

    /**
     * exit animator
     * */
    fun exitWithAnimator(isDragScale: Boolean) {
        val scaleWidth = mContainerWidth * mCurrentScaleX
        val scaleHeight = maxHeight * mCurrentScaleY

        mCurrentTranslateX += mContainerWidth * (1 - mCurrentScaleX) / 2

        Log.d(
            TAG,
            "mCurrentTransLateY : $mCurrentTransLateY  1111   mTargetTranslateY : $mTargetTranslateY"
        )

//        mCurrentTransLateY += maxHeight * (1 - mCurrentScaleY) / 2   正常对view的缩放代码
        if (isDragScale) {
            val exScale = maxHeight / mContainerHeight   //这个是为 photo view 特殊处理的！！！
            mCurrentTransLateY += (mContainerHeight * (1 - mCurrentScaleY * exScale) / 2 - mTargetTranslateY)
        } else {
            mCurrentTransLateY += maxHeight * (1 - mCurrentScaleY) / 2
        }

        Log.d(TAG, "mCurrentTransLateY : $mCurrentTransLateY  222")

        mCurrentScaleX = 1f
        mCurrentScaleY = 1f

        if (draggableParams.isValid()) {
            animateToOriginLocation(scaleWidth, scaleHeight)
        } else {
            actionListener?.onExit()
        }
    }

    //退出时，如果有指定入场时的view， 则回到最初的位置
    private fun animateToOriginLocation(currentWidth: Float, currentHeight: Float) {
        exitCallback?.onStartInitAnimatorParams()

        Log.d(
            TAG,
            "mCurrentTranslateX : $mCurrentTranslateX  mCurrentTransLateY : $mCurrentTransLateY"
        )

        val dx = mCurrentTranslateX - draggableParams.viewLeft
        val dy = mCurrentTransLateY - draggableParams.viewTop
        val dWidth = currentWidth - draggableParams.viewWidth
        val dHeight = currentHeight - draggableParams.viewHeight

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
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                    actionListener?.onExit()
                }
            })
        }
        exitAnimator.start()
    }

    //用户没有触发拖拽退出，还原状态
    private fun restoreStatusWithAnimator() {

        Log.d(TAG, "mCurrentTransLateY : $mCurrentTransLateY ")
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
        fun currentAlphaValue(percent: Int)
    }

    interface EnterAnimatorCallback {
        fun onEnterAnimatorStart()
        fun onEnterAnimatorEnd()
    }

    interface ExitAnimatorCallback {
        fun onStartInitAnimatorParams()
    }

}