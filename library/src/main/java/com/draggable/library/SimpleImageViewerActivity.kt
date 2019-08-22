package com.draggable.library

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.draggable.library.core.DraggableContainerView
import com.draggable.library.core.DraggableParamsInfo
import com.draggable.library.view.SimpleMaxWidthImageViewer

class SimpleImageViewerActivity : AppCompatActivity() {

    companion object {
        private const val PARAMS = "params"
        private const val IMAGE_URL = "image_url"
        private const val CONTENT_WH_RADIO = "radio"
        fun start(context: Context, draggableInfo: DraggableParamsInfo, imageUrl: String) {
            val intent = Intent(context, SimpleImageViewerActivity::class.java)
            intent.putExtra(PARAMS, draggableInfo)
            intent.putExtra(IMAGE_URL, imageUrl)
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(0, 0)
            }
        }
    }

    private val TAG = javaClass.simpleName
    private val imageView by lazy {
        SimpleMaxWidthImageViewer(this)
    }

    private val draggableView by lazy {
        DraggableContainerView(this).apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setScaleChildView(imageView)
            actionListener = object : DraggableContainerView.ActionListener {
                override fun onExit() {
                    finish()
                    overridePendingTransition(0, 0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.transparentStatusBar(this)
        setContentView(draggableView)
        val info = intent.getSerializableExtra(PARAMS) as? DraggableParamsInfo
        val url = intent.getStringExtra(IMAGE_URL)
        if (info != null) {
            draggableView.setParamsAndStartEnterAnimate(info, imageView)
            imageView.loadImage(SimpleMaxWidthImageViewer.ImageInfo(url, info.contentWHRadio))
        }
    }

}
