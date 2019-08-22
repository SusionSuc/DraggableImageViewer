package com.drawable.viewer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTvSimpleImage.setOnClickListener {
            startActivity(Intent(this, SimpleImageSampleActivity::class.java))
        }
    }
}
