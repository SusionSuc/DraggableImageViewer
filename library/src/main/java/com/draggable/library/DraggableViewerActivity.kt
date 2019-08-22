package com.draggable.library

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.draggable.library.view.ImageGalleryViewer

class DraggableViewerActivity : AppCompatActivity() {

    companion object {
        private val PARAMS_LOCATIONS = "locations"
        private val PARAMS_LOCATION = "location"
        private val PARAMS_INDEX = "index"
        fun start(context: Activity, imageLocations: ArrayList<ImageViewImageInfo>, index: Int = 0) {
            val intent = Intent(context, DraggableViewerActivity::class.java)
            intent.putExtra(PARAMS_LOCATIONS, imageLocations)
            intent.putExtra(PARAMS_INDEX, index)
            context.startActivity(intent)
            context.overridePendingTransition(0, 0)
        }

        fun start(context: Activity, locationInfo: ImageViewImageInfo, index: Int = 0) {
            val intent = Intent(context, DraggableViewerActivity::class.java)
            intent.putExtra(PARAMS_LOCATION, locationInfo)
            intent.putExtra(PARAMS_INDEX, index)
            context.startActivity(intent)
            context.overridePendingTransition(0, 0)
        }
    }

    private val imageViewerView by lazy {
        ImageGalleryViewer(this).apply {
            actionListener = object : ImageGalleryViewer.ActionListener {
                override fun close() {
                    finish()
                    overridePendingTransition(0, 0)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(imageViewerView)
        val index = intent.getIntExtra(PARAMS_INDEX, 0)
        val images = intent.getSerializableExtra(PARAMS_LOCATIONS)
        if (images != null) {
            imageViewerView.showImages(images as ArrayList<ImageViewImageInfo>, index)
        } else {
            val image = intent.getSerializableExtra(PARAMS_LOCATION) ?: return
            imageViewerView.showImage(image as ImageViewImageInfo)
        }
    }

    override fun onBackPressed() {
        imageViewerView.closeWithAnimate()
    }

}
