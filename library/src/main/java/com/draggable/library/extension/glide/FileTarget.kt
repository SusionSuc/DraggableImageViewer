package com.draggable.library.extension.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File

open class FileTarget : Target<File> {
    override fun onLoadStarted(placeholder: Drawable?) {
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
    }

    override fun getSize(cb: SizeReadyCallback) {
    }

    override fun getRequest(): Request? {
        return null
    }

    override fun onStop() {
    }

    override fun setRequest(request: Request?) {
    }

    override fun removeCallback(cb: SizeReadyCallback) {
        cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
    }

    override fun onResourceReady(resource: File, transition: Transition<in File>?) {
    }

    override fun onStart() {
    }

    override fun onDestroy() {
    }
}