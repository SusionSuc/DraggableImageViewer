package com.drawable.viewer

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.draggable.library.extension.ImageViewerHelper
import kotlinx.android.synthetic.main.activity_sample_simple_image.*
import kotlin.random.Random

class SimpleImageSampleActivity : AppCompatActivity() {

    class SampleInfo(val url: String, val view: ImageView)

    private val TAG = javaClass.simpleName
    private val sampleViews = ArrayList<SampleInfo>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_simple_image)
        initSampleInfo()
    }

    private fun initSampleInfo() {
        sampleViews.add(
            SampleInfo(
                "https://upload-bbs.mihayo.com/upload/2019/03/02/73715441/86bf8594bca8685e580c29f037ce99b3_4973701406132738096.jpg",
                mIv4
            )
        )
        sampleViews.add(
            SampleInfo(
                "https://upload-bbs.mihayo.com/upload/2019/03/05/73565533/544d624816aa2f39e885fd253a48ed24_990415953277272574.jpg",
                mIv5
            )
        )
        sampleViews.add(
            SampleInfo(
                "https://upload-bbs.mihayo.com/upload/2019/03/01/73565430/82a40083d95800c553d036b8c0689323_4849126433310918291.png",
                mIv6
            )
        )

        sampleViews.forEach { sampleInfo ->
            sampleInfo.view.setOnClickListener {
                ImageViewerHelper.showSimpleImage(
                    this,
                    sampleInfo.url,
                    view = sampleInfo.view,
                    showDownLoadBtn = false
                )
            }
            loadImage(sampleInfo.url, sampleInfo.view)
        }
    }

    private fun loadImage(url: String, iv: ImageView) {
        Glide.with(this)
            .load(url)
            .into(iv)
    }

}
