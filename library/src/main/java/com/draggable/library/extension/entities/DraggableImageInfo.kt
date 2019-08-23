package com.draggable.library.extension.entities

import com.draggable.library.core.DraggableParamsInfo
import java.io.Serializable

data class DraggableImageInfo(
    val originImg: String = "",
    val thumbnailImg: String = "",
    val draggableInfo: DraggableParamsInfo = DraggableParamsInfo()
) : Serializable
