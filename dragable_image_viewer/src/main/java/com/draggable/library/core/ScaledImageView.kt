package com.draggable.library.core

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import com.draggable.library.core.photoview.PhotoView


/**
 * 利用 PhotoView显示图片
 *
 * */
class ScaledImageView : FrameLayout {

    private val photoView = PhotoView(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.CENTER
        }
    }

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView()
    }

    private fun initView() {
        layoutParams = LayoutParams(0, 0)
        addView(photoView)
    }

}