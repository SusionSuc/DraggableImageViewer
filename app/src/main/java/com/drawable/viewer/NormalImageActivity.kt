package com.drawable.viewer

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.draggable.library.core.DraggableViewerHelper
import kotlinx.android.synthetic.main.activity_normal_image.*

class NormalImageActivity : AppCompatActivity() {

    private val imags = ArrayList<DraggableViewerHelper.ImageInfo>().apply {
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/21/73766616/4d09b6b94491d3921344be906aa7971a_4136353673894269217.png"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/12/50600998/1543e13e5cba414a1e4d396d8e6bdbb0_4959259236143228277.jpg"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/02/03/74582189/ede10255b2a99cfcf33868d1afd81a92_6059341049122226062.png"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/06/75158229/53c6eb0e1c4bb8db97cbd9c8db631423_3306524819178217982.jpg"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/08/10982654/fe2e9243c4e6ea7e489f81ae3814ed08_3279663480817048245.jpg"))
        add(DraggableViewerHelper.ImageInfo("https://upload-bbs.mihayo.com/upload/2019/03/01/73565430/82a40083d95800c553d036b8c0689323_4849126433310918291.png"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_normal_image)

        loadImage(imags[0].thumbnailUrl, mImagesIv1)
        loadImage(imags[1].thumbnailUrl, mImagesIv2)
        loadImage(imags[2].thumbnailUrl, mImagesIv3)

        mImagesIv1.setOnClickListener {
            DraggableViewerHelper.showSimpleImage(this, imags[0].thumbnailUrl)
        }

        mImagesIv2.setOnClickListener {
            DraggableViewerHelper.showSimpleImage(this, imags[1].thumbnailUrl)
        }

        mImagesIv3.setOnClickListener {
            DraggableViewerHelper.showSimpleImage(this, imags[2].thumbnailUrl)
        }
    }

    private fun loadImage(url: String, iv: ImageView) {
        Glide.with(this)
            .load(url)
            .into(iv)
    }
}
