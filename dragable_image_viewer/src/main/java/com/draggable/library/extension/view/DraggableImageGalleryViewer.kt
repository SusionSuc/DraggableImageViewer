package com.draggable.library.extension.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import com.draggable.library.core.DraggableImageView
import com.draggable.library.extension.entities.DraggableImageInfo
import com.draggable.library.extension.glide.GlideHelper
import com.drawable.library.R
import kotlinx.android.synthetic.main.view_image_viewr.view.*

/**
 * Created by susion on 2019/08/15
 * 可拖拽图片浏览容器
 */
class DraggableImageGalleryViewer(context: Context) : FrameLayout(context) {

    private val TAG = javaClass.simpleName
    private val TAG_PREGIX = "DraggableImageGalleryViewer_"
    var actionListener: ActionListener? = null
    private val mImageList = ArrayList<DraggableImageInfo>()
    private var showWithAnimator: Boolean = true

    init {
        LayoutInflater.from(context).inflate(R.layout.view_image_viewr, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        background = ColorDrawable(Color.TRANSPARENT)
        initAdapter()
        mImageGalleryViewOriginDownloadImg.setOnClickListener {
            val currentImg = mImageList[mImageViewerViewPage.currentItem]
            GlideHelper.downloadPicture(context, currentImg.originImg)
        }
    }

    fun showImagesWithAnimator(imageList: List<DraggableImageInfo>, index: Int = 0) {
        mImageList.clear()
        mImageList.addAll(imageList)
        mImageViewerViewPage.adapter?.notifyDataSetChanged()
        setCurrentImgIndex(index)
    }

    private fun initAdapter() {
        mImageViewerViewPage.adapter = object : androidx.viewpager.widget.PagerAdapter() {

            override fun isViewFromObject(view: View, any: Any) = view == any

            override fun getCount() = mImageList.size

            override fun instantiateItem(container: ViewGroup, position: Int): DraggableImageView {
                val imageInfo = mImageList[position]
                val imageView = getImageViewFromCacheContainer()
                container.addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                if (showWithAnimator) {
                    showWithAnimator = false
                    imageView.showImageWithAnimator(imageInfo)
                } else {
                    imageView.showImage(imageInfo)
                }
                imageView.setTag("$TAG_PREGIX$position")
                mImageGalleryViewOriginDownloadImg.visibility = if (imageInfo.imageCanDown) View.VISIBLE else View.GONE
                return imageView
            }

            override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                container.removeView(any as View)
            }
        }

        mImageViewerViewPage.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                setCurrentImgIndex(position)
            }
        })
    }

    private fun setCurrentImgIndex(index: Int) {
        mImageViewerViewPage.setCurrentItem(index, false)
        mImageViewerTvIndicator.text = "${index + 1}/${mImageList.size}"
    }

    private val vpContentViewList = ArrayList<DraggableImageView>()
    private fun getImageViewFromCacheContainer(): DraggableImageView {
        var availableImageView: DraggableImageView? = null
        if (vpContentViewList.isNotEmpty()) {
            vpContentViewList.forEach {
                if (it.parent == null) {
                    availableImageView = it
                }
            }
        }

        if (availableImageView == null) {
            availableImageView = DraggableImageView(context).apply {
                actionListener = object : DraggableImageView.ActionListener {
                    override fun onExit() {
                        this@DraggableImageGalleryViewer.actionListener?.closeViewer()
                    }
                }
            }
            vpContentViewList.add(availableImageView!!)
        }

        return availableImageView!!
    }

    fun closeWithAnimator(): Boolean {
        val currentView = findViewWithTag<DraggableImageView>("$TAG_PREGIX${mImageViewerViewPage.currentItem}")
        val imageInfo = mImageList[mImageViewerViewPage.currentItem]
        if (imageInfo.draggableInfo.isValid()) {
            currentView?.closeWithAnimator()
        } else {
            actionListener?.closeViewer()
        }
        return true
    }

    interface ActionListener {
        fun closeViewer()
    }

}