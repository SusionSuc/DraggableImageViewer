package com.draggable.library

import java.io.Serializable

data class ImageViewImageInfo(
    val originImg: String = "",
    val thumbnailImg: String  = "",
    val viewLeft: Int = 0,
    val viewTop: Int = 0,
    val viewWidth: Int = 0,
    val viewHeight: Int = 0,
    val imgIndex: Int = 0,
    val showDownLoadIcon: Boolean = true,
    val imageWidth:Int = 500,
    val imageHeight:Int = 500
) : Serializable{
    fun isValid() = viewWidth != 0 && viewHeight != 0 && imageWidth != 0 && imageHeight != 0
}
