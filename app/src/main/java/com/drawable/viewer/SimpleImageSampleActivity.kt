package com.drawable.viewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.draggable.library.core.DraggableViewerHelper
import kotlinx.android.synthetic.main.activity_sample_simple_image.*

class SimpleImageSampleActivity : AppCompatActivity() {

    class SampleInfo(val url: String, val view: ImageView, val whRadio: Float = 1f)

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
                mIv4,
                0.7f
            )
        )
        sampleViews.add(
            SampleInfo(
                "https://upload-bbs.mihayo.com/upload/2019/03/05/73565533/544d624816aa2f39e885fd253a48ed24_990415953277272574.jpg",
                mIv5,
                1.65f
            )
        )
        sampleViews.add(
            SampleInfo(
                "https://upload-bbs.mihayo.com/upload/2019/03/01/73565430/82a40083d95800c553d036b8c0689323_4849126433310918291.png",
                mIv6,
                0.142f
            )
        )

        sampleViews.forEach { sampleInfo ->
            sampleInfo.view.setOnClickListener {
                DraggableViewerHelper.showSimpleImage(this, sampleInfo.url, sampleInfo.view, sampleInfo.whRadio)
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
