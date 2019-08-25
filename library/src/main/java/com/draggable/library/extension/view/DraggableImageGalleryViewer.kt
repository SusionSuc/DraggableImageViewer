package com.draggable.library.extension.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.Image
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.draggable.library.Utils
import com.draggable.library.core.DraggableZoomCore
import com.draggable.library.core.photoview.PhotoView
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
    var actionListener: ActionListener? = null
    private val mImageList = ArrayList<DraggableImageInfo>()
    private var draggableZoomCore: DraggableZoomCore? = null
    private var draggableZoomActionListener = object : DraggableZoomCore.ActionListener {
        override fun currentAlphaValue(alpha: Int) {
            background = ColorDrawable(Color.argb(alpha, 0, 0, 0))
        }

        override fun onExit() {
            actionListener?.closeViewer()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_image_viewr, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mImageViewerViewPage.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.CENTER
        }
        background = ColorDrawable(Color.TRANSPARENT)
        initAdapter()
        mImageGalleryViewOriginDownloadImg.setOnClickListener {
            val currentImg = mImageList[mImageViewerViewPage.currentItem]
            GlideHelper.downloadPicture(context, currentImg.originImg)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (draggableZoomCore?.dispatchTouchEvent(ev) == true) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    //是否可以拦截事件
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val isIntercept = super.onInterceptTouchEvent(ev)
        return draggableZoomCore?.onInterceptTouchEvent(isIntercept, ev) ?: false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        draggableZoomCore?.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun showImagesWithAnimator(imageList: List<DraggableImageInfo>, index: Int = 0) {
        mImageViewerViewPage.layoutParams = LayoutParams(0, 0)
        mImageList.clear()
        mImageList.addAll(imageList)
        mImageViewerViewPage.adapter?.notifyDataSetChanged()
        setCurrentImgIndex(index)
        post {
            startEnterAnimator(index)
        }
    }

    private fun startEnterAnimator(index: Int) {
        val paramsInfo = mImageList[index]
        if (paramsInfo.draggableInfo.isValid()) {
            draggableZoomCore = DraggableZoomCore(
                paramsInfo.draggableInfo,
                mImageViewerViewPage,
                width,
                height,
                draggableZoomActionListener
            )
            draggableZoomCore?.enterWithAnimator()
        } else {
            mImageViewerViewPage.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun initAdapter() {
        mImageViewerViewPage.adapter = object : PagerAdapter() {

            override fun isViewFromObject(view: View, any: Any) = view == any

            override fun getCount() = mImageList.size

            override fun instantiateItem(container: ViewGroup, position: Int): ImageView {
                Log.d(TAG, "instantiateItem position : $position")
                val imageView = getImageViewFromCacheContainer()
                loadImage(mImageList[position], imageView)
                container.addView(imageView)
                return imageView
            }

            override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                container.removeView(any as View)
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

    private fun setCurrentImgIndex(index: Int) {
        mImageViewerViewPage.setCurrentItem(index, false)
        mImageViewerTvIndicator.text = "${index + 1}/${mImageList.size}"
        val draggableImageInfo = mImageList[index]
        draggableZoomCore?.adjustView(draggableImageInfo)
    }

    private fun loadImage(imgInfo: DraggableImageInfo, imageVeiew: ImageView, isLoadOriginImage: Boolean = false) {

        val targetUrl: String
        //如果是wifi网络 or 原图已经加载过则直接加载原图
        if (GlideHelper.imageIsInCache(context, imgInfo.originImg) || Utils.isWifiConnected(context)) {
            targetUrl = imgInfo.originImg
            mImageGalleryViewOriginImage.visibility = View.GONE
        } else {
            targetUrl = imgInfo.thumbnailImg
            mImageGalleryViewOriginImage.visibility = View.VISIBLE
        }

        Glide.with(this).load(targetUrl)
            .into(object : ImageViewTarget<Drawable>(imageVeiew) {
                override fun setResource(resource: Drawable?) {
                    mImageViewerProgress.visibility = View.GONE
                    if (resource != null) {
                        setDrawable(resource)
                    }
                }

                override fun onLoadStarted(placeholder: Drawable?) {
                    super.onLoadStarted(placeholder)
                    if (!isLoadOriginImage) {
                        mImageViewerProgress.visibility = View.VISIBLE
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    mImageViewerProgress.visibility = View.GONE
                }
            })
    }

    fun close(): Boolean {
        draggableZoomCore?.exitWithAnimator()
        return true
    }

    private val vpContentViewList = ArrayList<PhotoView>()
    private fun getImageViewFromCacheContainer(): PhotoView {
        var availableImageView: PhotoView? = null
        if (vpContentViewList.isNotEmpty()) {
            vpContentViewList.forEach {
                if (it.parent == null) {
                    availableImageView = it
                }
            }
        }

        if (availableImageView == null) {
            availableImageView = PhotoView(context).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    if (scale != 1f) {
                        setScale(1f, true)
                    } else {
                        draggableZoomCore?.exitWithAnimator()
                    }
                }
            }
            vpContentViewList.add(availableImageView!!)
        }

        return availableImageView!!
    }

    interface ActionListener {
        fun closeViewer()
    }

}