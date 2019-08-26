package com.drawable.viewer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_STORAGE =
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mTvSimpleImage.setOnClickListener {
            startActivity(Intent(this, SimpleImageSampleActivity::class.java))
        }

        mTvMutilImage.setOnClickListener {
            startActivity(Intent(this, ImageListSampleActivity::class.java))
        }

        mTvNormalImage.setOnClickListener {
            startActivity(Intent(this, NormalImageActivity::class.java))
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
            }
        }
    }

}
