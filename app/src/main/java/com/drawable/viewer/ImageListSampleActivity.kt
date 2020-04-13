package com.drawable.viewer

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.draggable.library.extension.DraggableImageViewerHelper
import kotlinx.android.synthetic.main.activity_image_list_sample.*

class ImageListSampleActivity : AppCompatActivity() {

    private val imags = ArrayList<DraggableImageViewerHelper.ImageInfo>().apply {
        add(DraggableImageViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/21/73766616/4d09b6b94491d3921344be906aa7971a_4136353673894269217.png"))
        add(DraggableImageViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/12/50600998/1543e13e5cba414a1e4d396d8e6bdbb0_4959259236143228277.jpg"))
        add(DraggableImageViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/02/03/74582189/ede10255b2a99cfcf33868d1afd81a92_6059341049122226062.png"))
        add(DraggableImageViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/06/75158229/53c6eb0e1c4bb8db97cbd9c8db631423_3306524819178217982.jpg"))
        add(DraggableImageViewerHelper.ImageInfo("https://upload-bbs.mihoyo.com/upload/2019/08/08/10982654/fe2e9243c4e6ea7e489f81ae3814ed08_3279663480817048245.jpg"))
        add(DraggableImageViewerHelper.ImageInfo("https://upload-bbs.mihayo.com/upload/2019/03/01/73565430/82a40083d95800c553d036b8c0689323_4849126433310918291.png"))
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

        mTvShow4.setOnClickListener {
            showImages(3)
        }

        mTvShowLast.setOnClickListener {
            showImages(imags.size - 1)
        }
    }

    private fun showImages(index: Int) {
        DraggableImageViewerHelper.showImages(this, listOf(mImagesIv1, mImagesIv2, mImagesIv3), imags, index)
    }

    private fun loadImage(url: String, iv: ImageView) {
        Glide.with(this)
            .load(url)
            .into(iv)
    }

}
