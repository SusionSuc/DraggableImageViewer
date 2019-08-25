package com.drawable.viewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.draggable.library.core.DraggableViewerHelper
import kotlinx.android.synthetic.main.activity_image_list_sample.*

class ImageListSampleActivity : AppCompatActivity() {

    private val imags = ArrayList<DraggableViewerHelper.ImageInfo>().apply {
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/21/73766616/4d09b6b94491d3921344be906aa7971a_4136353673894269217.png"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/12/50600998/1543e13e5cba414a1e4d396d8e6bdbb0_4959259236143228277.jpg"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/02/03/74582189/ede10255b2a99cfcf33868d1afd81a92_6059341049122226062.png"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/06/75158229/53c6eb0e1c4bb8db97cbd9c8db631423_3306524819178217982.jpg"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/08/10982654/fe2e9243c4e6ea7e489f81ae3814ed08_3279663480817048245.jpg"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_list_sample)

        loadImage(imags[0].thumbnailUrl, mImagesIv1)
        loadImage(imags[1].thumbnailUrl, mImagesIv2)
        loadImage(imags[2].thumbnailUrl, mImagesIv3)

        mImagesIv1.setOnClickListener {
            showImages(0)
        }


        mImagesIv2.setOnClickListener {
            showImages(1)
        }


        mImagesIv3.setOnClickListener {
            showImages(2)
        }
    }

    private fun showImages(index: Int) {
        DraggableViewerHelper.showImages(this, listOf(mImagesIv1, mImagesIv2, mImagesIv3), imags, index)
    }

    private fun loadImage(url: String, iv: ImageView) {
        Glide.with(this)
            .load(url)
            .into(iv)
    }


}
