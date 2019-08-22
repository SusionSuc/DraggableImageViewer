package com.draggable.library.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.drawable.library.R
import com.draggable.library.ImageViewImageInfo
import kotlinx.android.synthetic.main.view_image_viewr.view.*

/**
 * Created by susion on 2019/08/15
 * 图片浏览容器
 */
class ImageGalleryViewer(context: Context) : FrameLayout(context) {

    private val TAG = javaClass.simpleName
    private val mImageList = ArrayList<ImageViewImageInfo>()
    var actionListener: ActionListener? = null
    val imageViewList = ArrayList<MiHoYoBigImageView>()
    var showEnterAnimate = true

    init {
        LayoutInflater.from(context).inflate(R.layout.view_image_viewr, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        mImageViewerViewPage.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
            gravity = Gravity.CENTER
        }
        background = ColorDrawable(Color.TRANSPARENT)
        initAdapter()
    }

    fun showImages(imageList: List<ImageViewImageInfo>, index: Int = 0) {
        mImageList.clear()
        mImageList.addAll(imageList)
        mImageViewerViewPage.adapter?.notifyDataSetChanged()
        setCurrentImgIndex(index)
    }

    fun showImage(image: ImageViewImageInfo) {
        mImageList.clear()
        mImageList.addAll(listOf(image))
        mImageViewerViewPage.adapter?.notifyDataSetChanged()
        setCurrentImgIndex(0)
    }

    private fun initAdapter() {
        mImageViewerViewPage.adapter = object : PagerAdapter() {

            override fun isViewFromObject(view: View, any: Any) = view == any

            override fun getCount() = mImageList.size

            override fun instantiateItem(container: ViewGroup, position: Int): MiHoYoBigImageView {
                val imageView = MiHoYoBigImageView(context).apply {
                    actionListener = object : MiHoYoBigImageView.ActionListener {
                        override fun onExit() {
                            this@ImageGalleryViewer.actionListener?.close()
                        }
                    }
                }

                imageViewList.add(imageView)

                container.addView(imageView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
                val imgInfo = mImageList[position]

                if (imgInfo.isValid()){
                    if (showEnterAnimate){
                        Log.d(TAG, "load img with animate : ${imgInfo.originImg}")
                        showEnterAnimate = false
                        imageView.setImageInfoWithAnimate(imgInfo)
                    }else{
                        Log.d(TAG, "load img without animate : ${imgInfo.originImg}")
                        imageView.setImageInfo(imgInfo)
                    }
                }else{
                    Log.d(TAG, "load simple img  : ${imgInfo.originImg}")
                    imageView.setImageWithoutInfo(imgInfo)
                }

                Log.d("thumbnailUrl", "image viewer : ${ imgInfo?.thumbnailImg}")
                Glide.with(context).load(imgInfo.thumbnailImg).into(object : ImageViewTarget<Drawable>(imageView.photoView){
                    override fun setResource(resource: Drawable?) {
                        mImageViewerProgress.visibility = View.GONE
                        setDrawable(resource)
                    }

                    override fun onLoadStarted(placeholder: Drawable?) {
                        super.onLoadStarted(placeholder)
                        mImageViewerProgress.visibility = View.VISIBLE
                    }
                })

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

    private fun setCurrentImgIndex(index: Int) {
        mImageViewerViewPage.setCurrentItem(index, false)
        mImageViewerTvIndicator.text = "${index + 1}/${mImageList.size}"
    }

    fun closeWithAnimate() {
        val currentView = imageViewList[mImageViewerViewPage.currentItem]
        currentView.startExitAnimator()
    }

    interface ActionListener {
        fun close()
    }

}