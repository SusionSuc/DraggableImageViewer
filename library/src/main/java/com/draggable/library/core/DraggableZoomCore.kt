package com.draggable.library.core

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.draggable.library.extension.entities.DraggableImageInfo

/**
 * 把一个View 从一个地方移动到另一个地方 & 伴随缩放事件。
 *
 * */
class DraggableZoomCore(
    var draggableParams: DraggableParamsInfo,
    val scaleDraggableView: View,
    val mContainerWidth: Int,
    val mContainerHeight: Int,
    val actionListener: ActionListener? = null
) {

    private val TAG = javaClass.simpleName

    //core animator params
    private val ANIMATOR_DURATION = 300L
    private var mAlpha = 0
    private var mTargetTranslateY = 1f
    private var mCurrentTransLateY: Float = 0.toFloat()
    private var mCurrentTranslateX: Float = 0.toFloat()
    private var mCurrentScaleX = 1f
    private var mCurrentScaleY = 1f
    var isAnimating = false
    private var minScaleXY = 0.3f
    private var maxHeight = 1f
    private var mCurrentWidth: Int = 0
    private var mCurrentHeight: Int = 0

    //drag event params
    private var mDownX: Float = 0f
    private var mDownY: Float = 0f
    private val MAX_TRANSLATE_Y = 1500

    init {
        adjustAnimateParams()
    }

    private fun adjustAnimateParams() {
        mCurrentHeight = draggableParams.viewHeight
        mCurrentWidth = draggableParams.viewWidth
        mCurrentTranslateX = draggableParams.viewLeft.toFloat()
        mCurrentTransLateY = draggableParams.viewTop.toFloat()
        maxHeight = mContainerWidth / draggableParams.contentWHRadio
        if (maxHeight > mContainerHeight) {
            maxHeight = mContainerHeight.toFloat()
        }
        mTargetTranslateY = (mContainerHeight - maxHeight) / 2
    }

    fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (isAnimating) return true
        return false
    }

    //是否可以拦截事件
    fun onInterceptTouchEvent(intercept: Boolean, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
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
        return intercept
    }

    //处理用户拖拽事件
    fun onTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - mDownX
                val dy = event.y - mDownY
                onActionMove(dx, dy)
            }
            MotionEvent.ACTION_UP -> {
                if (event.pointerCount == 1) {
                    if (mCurrentScaleY != 1f) {
                        if (mCurrentScaleY < 0.7) {
                            exitWithAnimator()
                        } else {
                            restoreStatusWithAnimator()
                        }
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
    fun enterWithAnimator() {
        if (!draggableParams.isValid()) return
        val dx = mCurrentTranslateX - 0
        val dy = mCurrentTransLateY - mTargetTranslateY
        val dWidth = mContainerWidth - draggableParams.viewWidth
        val dHeight = maxHeight - draggableParams.viewHeight
        Log.d(TAG, "dx:$dx  dy:$dy  dWidth : $dWidth  dHeight:$dHeight")
        val enterAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener {
                val percent = it.animatedValue as Float
                Log.d(TAG, "update ..... percent $percent")
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
                }

                override fun onAnimationEnd(animation: Animator?) {
                    isAnimating = false
                }
            })
        }
        enterAnimator.start()
    }

    /**
     * exit animator
     * */
    fun exitWithAnimator() {
        val scaleWidth = mContainerWidth * mCurrentScaleX
        val scaleHeight = maxHeight * mCurrentScaleY
        mCurrentTranslateX += mContainerWidth * (1 - mCurrentScaleX) / 2
        mCurrentTransLateY += maxHeight * (1 - mCurrentScaleY) / 2
        mCurrentScaleX = 1f
        mCurrentScaleY = 1f

        if (draggableParams.isValid()) {
            animateToOriginLocation(scaleWidth, scaleHeight)
        } else {
            animateToScreenBottom(scaleWidth, scaleHeight)
        }
    }

    private fun animateToScreenBottom(currentWidth: Float, currentHeight: Float) {
        val initTranslateY = mCurrentTransLateY
        val initTranslateX = mCurrentTranslateX
        val dy = 1000f
        val dx = (currentWidth * 1f / 2) - mCurrentTranslateX
        mCurrentWidth = currentWidth.toInt()
        mCurrentHeight = currentHeight.toInt()

        val exitAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = ANIMATOR_DURATION
            addUpdateListener {
                val percent = it.animatedValue as Float
                mCurrentTranslateX = initTranslateX + dx * percent
                mCurrentTransLateY = initTranslateY + dy * percent
                mAlpha = (mAlpha * (1 - percent)).toInt()
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

    private fun animateToOriginLocation(currentWidth: Float, currentHeight: Float) {
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

    fun adjustScaleViewLocation(draggableImageInfo: DraggableImageInfo) {
        draggableParams = draggableImageInfo.draggableInfo
        maxHeight = mContainerWidth / draggableParams.contentWHRadio
        if (maxHeight > mContainerHeight) {
            maxHeight = mContainerHeight.toFloat()
        }
        mCurrentHeight = maxHeight.toInt()
        mCurrentWidth = mContainerWidth
        mCurrentTranslateX = 0f
        mCurrentTransLateY = (mContainerHeight - maxHeight) / 2
        mTargetTranslateY = mCurrentTransLateY
        mAlpha = 255
        changeChildViewAnimateParams()
    }

    interface ActionListener {
        fun onExit()
        fun currentAlphaValue(percent: Int)
    }

}