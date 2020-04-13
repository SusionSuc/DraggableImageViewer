package com.draggable.library.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.ViewGroup
import com.draggable.library.extension.entities.DraggableImageInfo
import com.draggable.library.extension.view.DraggableImageGalleryViewer

//查看多图
class ImagesViewerActivity : AppCompatActivity() {

    companion object {
        private const val PARAMS = "draggableImages"
        private const val INDEX = "index"
        fun start(context: Context, draggableImages: ArrayList<DraggableImageInfo>, index: Int = 0) {
            val intent = Intent(context, ImagesViewerActivity::class.java)
            intent.putExtra(PARAMS, draggableImages)
            intent.putExtra(INDEX, index)
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(0, 0)
            }
        }
    }

    private val galleryViewer by lazy {
        DraggableImageGalleryViewer(this).apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            actionListener = object : DraggableImageGalleryViewer.ActionListener {
                override fun closeViewer() {
                    finish()
                    overridePendingTransition(0, 0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.transparentStatusBar(this)
        setContentView(galleryViewer)
        val draggableImages: ArrayList<DraggableImageInfo> =
            intent.getSerializableExtra(PARAMS) as? ArrayList<DraggableImageInfo> ?: ArrayList()
        val index = intent.getIntExtra(INDEX, 0)

        if (draggableImages.isNotEmpty()) {
            galleryViewer.showImagesWithAnimator(draggableImages, index)
        }
    }

    override fun onBackPressed() {
        if (!galleryViewer.closeWithAnimator()) {
            super.onBackPressed()
        }
    }
}
