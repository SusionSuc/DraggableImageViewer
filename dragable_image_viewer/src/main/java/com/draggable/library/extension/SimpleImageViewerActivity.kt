package com.draggable.library.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import com.draggable.library.core.DraggableImageView
import com.draggable.library.extension.entities.DraggableImageInfo

//查看单张图片
@Deprecated("")
class SimpleImageViewerActivity : AppCompatActivity() {

    companion object {
        private const val PARAMS = "params"
        fun start(context: Context, draggableInfo: DraggableImageInfo) {
            val intent = Intent(context, SimpleImageViewerActivity::class.java)
            intent.putExtra(PARAMS, draggableInfo)
            context.startActivity(intent)
            if (context is Activity) {
                context.overridePendingTransition(0, 0)
            }
        }
    }

    private val draggableImageView by lazy {
        DraggableImageView(this).apply {
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            actionListener = object : DraggableImageView.ActionListener {
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
        setContentView(draggableImageView)
        val info = intent.getSerializableExtra(PARAMS) as? DraggableImageInfo
        if (info != null) {
            draggableImageView.showImageWithAnimator(info)
        }
    }

    override fun onBackPressed() {
        draggableImageView.closeWithAnimator()
    }

}
