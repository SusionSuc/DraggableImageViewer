package com.draggable.library.extension.entities

import com.draggable.library.core.DraggableParamsInfo
import java.io.Serializable

data class DraggableImageInfo(
    var originImg: String = "",
    var thumbnailImg: String = "",
    var draggableInfo: DraggableParamsInfo = DraggableParamsInfo(),
    val imageSize: Long = 0,
    val imageCanDown: Boolean = true
) : Serializable {
    fun adjustImageUrl() {

        if (originImg.isNotEmpty() && thumbnailImg.isNotEmpty()) return

        if (originImg.isEmpty() && thumbnailImg.isNotEmpty()) {
            originImg = thumbnailImg
        } else {
            thumbnailImg = originImg
        }
    }
}
