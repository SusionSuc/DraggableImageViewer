package com.draggable.library.core

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

//拖拽、缩放动画的实现
class DraggableZoomCore(
    val draggableParams: DraggableParamsInfo,
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
    var isAutoAnimating = false
    private var minScaleXY = 0.3f
    private var maxHeight = 1f
    private var mCurrentWidth: Int = 0
    private var mCurrentHeight: Int = 0

    //drag event params
    private var mDownX: Float = 0f
    private var mDownY: Float = 0f
    private val MAX_TRANSLATE_Y = 500
    private var isClickEvent = false

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

    fun dispatchTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x
                mDownY = event.y
                isClickEvent = true
            }

            MotionEvent.ACTION_MOVE -> {
                isClickEvent = false
                if (event.pointerCount == 1) {
                    val dx = event.x - mDownX              //在viewpager中的话， 避免过分拦截事件
                    val dy = event.y - mDownY
                    if (Math.abs(dx) > Math.abs(dy)) {
                        Log.d(TAG, "横滑不拦截事件...")
                    }

                    if (dy > 0) {
                        onActionMove(event)
//                        if (mGraggableImageViewPhotoView is DraggableChildViewEventRules) {
//                            if ((mGraggableImageViewPhotoView as DraggableChildViewEventRules).canInterceptEvent()) {
//                                Log.d(TAG, "子View 允许拦截事件")
//                                draggableZoomCore?.onActionMove(event)
//                                return true
//                            }
//                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (event.pointerCount == 1) {
                    onActionUp()
                }
            }
        }
    }

    private fun onActionUp() {
        if (mCurrentScaleY != 1f) {
            if (mCurrentScaleY < 0.5) {
                exitWithAnimator()
            } else {
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
        Log.d(TAG, "onActionMove...")
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

        mCurrentWidth = (mContainerWidth * mCurrentScaleX).toInt()
        mCurrentHeight = (mContainerHeight * mCurrentScaleY).toInt()

        mAlpha = (255 - 255 * percent).toInt()

        changeChildViewDragParams()
    }

    /**
     * enter animator
     * */
    fun enterWithAnimator() {
        val dx = mCurrentTranslateX - 0
        val dy = mCurrentTransLateY - mTargetTranslateY
        val dWidth = mContainerWidth - draggableParams.viewWidth
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
        val scaleWidth = mContainerWidth * mCurrentScaleX
        val scaleHeight = maxHeight * mCurrentScaleY
        mCurrentTranslateX += mContainerWidth * (1 - mCurrentScaleX) / 2
        mCurrentTransLateY += maxHeight * (1 - mCurrentScaleY) / 2
        mCurrentScaleX = 1f
        mCurrentScaleY = 1f

        var dx = mCurrentTranslateX - draggableParams.viewLeft
        var dy = mCurrentTransLateY - draggableParams.viewTop
        var dWidth = scaleWidth - draggableParams.viewWidth
        var dHeight = scaleHeight - draggableParams.viewHeight

        if (!draggableParams.isValid()) {
            dy = 2500f
            dx = mContainerWidth * 1f / 2
            dWidth = 0f
            dHeight = 0f
        }

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

    //child view scale core params
    private fun changeChildViewAnimateParams() {
        scaleDraggableView.apply {
            layoutParams = FrameLayout.LayoutParams(mCurrentWidth, mCurrentHeight)
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

    interface ActionListener {
        fun onExit()
        fun currentAlphaValue(percent: Int)
    }

}