package com.draggable.library.core

import java.io.Serializable

/**
 * [DraggableImageView] 动画参数
 * */
data class DraggableParamsInfo(
    val viewLeft: Int = 0,
    val viewTop: Int = 0,
    val viewWidth: Int = 0,
    val viewHeight: Int = 0,
    val contentWHRadio: Float = 1f // 显示内容的宽高比
) : Serializable {
    fun isValid() = viewWidth != 0 && viewHeight != 0
}