package com.draggable.library.extension.entities

import com.draggable.library.core.DraggableParamsInfo
import java.io.Serializable

data class DraggableImageInfo(
    val originImg: String = "",
    val thumbnailImg: String = "",
    val draggableInfo: DraggableParamsInfo = DraggableParamsInfo(),
    val imageSize: Long = 0,
    val imageCanDown:Boolean = true
) : Serializable
