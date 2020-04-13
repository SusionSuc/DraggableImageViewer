package com.drawable.viewer

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_photo_view_test_acyivity.*

class PhotoViewTestAcyivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view_test_acyivity)

        val url  = "https://upload-bbs.mihoyo.com/upload/2019/08/08/10982654/fe2e9243c4e6ea7e489f81ae3814ed08_3279663480817048245.jpg"

        Glide.with(this)
            .load(url)
            .into(mPhotoViewTestPV)

        findViewById<View>(android.R.id.content).postDelayed({
            Log.d("PhotoViewTestAcyivity", "width : ${mPhotoViewTestPV.width}  height : ${mPhotoViewTestPV.height}")
        },1000)

    }
}
