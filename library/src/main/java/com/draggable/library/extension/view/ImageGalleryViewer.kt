package com.draggable.library.extension.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.draggable.library.extension.entities.DraggableImageInfo
import com.drawable.library.R
import kotlinx.android.synthetic.main.view_image_viewr.view.*

/**
 * Created by susion on 2019/08/15
 * 图片浏览容器
 */
class ImageGalleryViewer(context: Context) : FrameLayout(context) {

    private val TAG = javaClass.simpleName
    private val mImageList = ArrayList<DraggableImageInfo>()
    var actionListener: ActionListener? = null
    val imageViewList = ArrayList<DraggableImageView>()
    var showEnterAnimate = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_image_viewr, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mImageViewerViewPage.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.CENTER
        }
        background = ColorDrawable(Color.TRANSPARENT)
        initAdapter()
    }

    fun showImagesWithAnimator(imageList: List<DraggableImageInfo>, index: Int = 0) {
        showEnterAnimate = true
        mImageList.clear()
        mImageList.addAll(imageList)
        mImageViewerViewPage.adapter?.notifyDataSetChanged()
        setCurrentImgIndex(index)
    }

    private fun initAdapter() {
        mImageViewerViewPage.adapter = object : PagerAdapter() {

            override fun isViewFromObject(view: View, any: Any) = view == any

            override fun getCount() = mImageList.size

            override fun instantiateItem(container: ViewGroup, position: Int): DraggableImageView {
                val imageView = getDraggableImageView()
                imageViewList.add(imageView)
                container.addView(imageView)
                showImage(imageView, mImageList[position])
                return imageView
            }

            override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                val imageView = any as View
                container.removeView(imageView)
            }
        }

        mImageViewerViewPage.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                setCurrentImgIndex(position)
            }
        })
    }

    private fun showImage(draggableImageView: DraggableImageView, draggableImageInfo: DraggableImageInfo) {
        post {
            if (showEnterAnimate) {
                showEnterAnimate = false // 只展示一次入场动画
                draggableImageView.enterWithFixedSizeparamsInfo(
                    this@ImageGalleryViewer.width,
                    this@ImageGalleryViewer.height,
                    draggableImageInfo,
                    true
                )
            } else {
                draggableImageView.enterWithFixedSizeparamsInfo(
                    this@ImageGalleryViewer.width,
                    this@ImageGalleryViewer.height,
                    draggableImageInfo,
                    false
                )
            }
        }
    }

    private fun setCurrentImgIndex(index: Int) {
        mImageViewerViewPage.setCurrentItem(index, false)
        mImageViewerTvIndicator.text = "${index + 1}/${mImageList.size}"
    }

    private fun getDraggableImageView(): DraggableImageView {
        return DraggableImageView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            actionListenr = object : DraggableImageView.ActionListener {
                override fun onExit() {
                    actionListener?.closeViewer()
                }
            }
        }
    }


    fun closeWithAnimate() {

    }

    interface ActionListener {
        fun closeViewer()
    }

}