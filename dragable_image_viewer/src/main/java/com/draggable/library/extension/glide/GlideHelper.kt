package com.draggable.library.extension.glide

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.disklrucache.DiskLruCache
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.draggable.library.core.DraggableParamsInfo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.nio.channels.FileChannel
import java.util.*

object GlideHelper {

    private val TAG = javaClass.simpleName
    private val DOWNLOAD_FILE_NAME = "Download"

    fun retrieveImageWhRadioFromMemoryCache(
        context: Context,
        thumbnailImg: String,
        retrieveCallBack: (inMemCache:Boolean , whRadio: Float, isGif:Boolean ) -> Unit
    ) {
        Glide.with(context).load(thumbnailImg).apply(RequestOptions().onlyRetrieveFromCache(true))
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                ) {
                    if (resource.intrinsicWidth > 0 && resource.intrinsicHeight > 0) {
                        Log.d(TAG, "从内存中检索到图片！！！！$thumbnailImg")
                        retrieveCallBack(true, resource.intrinsicWidth * 1f / resource.intrinsicHeight,resource is GifDrawable)
                    } else {
                        retrieveCallBack(false, DraggableParamsInfo.INVALID_RADIO,false)
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    retrieveCallBack(false,DraggableParamsInfo.INVALID_RADIO,false)
                }
            })
    }

    fun checkImageIsInMemoryCache(
        context: Context,
        url: String,
        callback: (inCache: Boolean) -> Unit
    ) {
        Glide.with(context).load(url).apply(RequestOptions().onlyRetrieveFromCache(true))
            .into(object : SimpleTarget<Drawable>() {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: com.bumptech.glide.request.transition.Transition<in Drawable>?
                ) {
                    callback(true)
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    callback(false)
                }
            })
    }


    //图片是否在 内存缓存 or  磁盘缓存
    fun imageIsInCache(context: Context, url: String): Boolean {
        if (url.isEmpty()) return false
        try {
            //磁盘缓存
            val safeKeyGenerator = SafeKeyGenerator()
            val safeKey = safeKeyGenerator.getSafeKey(GlideUrl(url))
            val file = File(context.cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR)
            val diskLruCache =
                DiskLruCache.open(file, 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong())
            val value = diskLruCache.get(safeKey)
            if (value != null && value.getFile(0).exists()) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun downloadPicture(context: Context, url: String): Disposable? {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            toastInScreenCenter(context, "没有打开存储权限")
            return null
        }

        toastInScreenCenter(context, "开始下载")
        return Observable.create<File> {
            it.onNext(
                Glide.with(context).load(url).downloadOnly(
                    Target.SIZE_ORIGINAL,
                    Target.SIZE_ORIGINAL
                ).get()
            )
            it.onComplete()
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnNext { file ->
            try {
                val downloadFolderName = DOWNLOAD_FILE_NAME
                val path =
                    Environment.getExternalStorageDirectory().toString() + "/" + downloadFolderName + "/"
                var name: String
                try {
                    name = url.substring(url.lastIndexOf("/") + 1, url.length)
                    if (name.contains(".")) {
                        name = name.substring(0, name.lastIndexOf("."))
                    }
                    name = MD5Utils.md5Encode(name)
                } catch (e: Exception) {
                    e.printStackTrace()
                    name = System.currentTimeMillis().toString() + ""
                }

                val mimeType = getImageTypeWithMime(file.absolutePath)
                name = "$name${System.currentTimeMillis()}.$mimeType"
                createFileByDeleteOldFile(path + name)
                Log.d(TAG, "save file : $path$name")
                val result = copyFile(file, path, name)

                if (result) {
                    saveImageToGallery(context, File(path, name))
                    toastInScreenCenter(context, "成功保存到系统相册")
                } else {
                    toastInScreenCenter(context, "保存失败")
                }

            } catch (e: Exception) {
                Log.d(TAG, "exception : ${e.message}")
            }
        }.doOnError {
            toastInScreenCenter(context, "保存失败")
        }.subscribe()

    }

    private fun toastInScreenCenter(context: Context, msg:String){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).apply {
            setGravity(Gravity.CENTER, 0, 0)
        }.show()
    }

    private fun saveImageToGallery(context: Context, file: File) {
        try {
            // 通知图库更新
            MediaScannerConnection.scanFile(context.applicationContext, arrayOf(file.absolutePath), null) { path, uri ->
            }
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.parse(file.absolutePath)
            context.sendBroadcast(mediaScanIntent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param filePath The path of file.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun createFileByDeleteOldFile(filePath: String): Boolean {
        return createFileByDeleteOldFile(getFileByPath(filePath))
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param file The file.
     * @return `true`: success<br></br>`false`: fail
     */
    private fun createFileByDeleteOldFile(file: File?): Boolean {
        if (file == null) return false
        // file exists and unsuccessfully delete then return false
        if (file.exists() && !file.delete()) return false
        if (!createOrExistsDir(file.parentFile)) return false
        try {
            return file.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    private fun getFileByPath(filePath: String): File? {
        return if (isSpace(filePath)) null else File(filePath)
    }

    private fun isSpace(s: String?): Boolean {
        if (s == null) return true
        var i = 0
        val len = s.length
        while (i < len) {
            if (!Character.isWhitespace(s[i])) {
                return false
            }
            ++i
        }
        return true
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return `true`: exists or creates successfully<br></br>`false`: otherwise
     */
    private fun createOrExistsDir(file: File?): Boolean {
        return file != null && if (file.exists()) file.isDirectory else file.mkdirs()
    }

    fun getImageTypeWithMime(path: String): String {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        var type = options.outMimeType
        // ”image/png”、”image/jpeg”、”image/gif”
        if (TextUtils.isEmpty(type)) {
            type = ""
        } else {
            type = type.substring(6)
        }
        return type
    }

    /**
     * 根据文件路径拷贝文件
     *
     * @param resourceFile 源文件
     * @param targetPath 目标路径（包含文件名和文件格式）
     * @return boolean 成功true、失败false
     */
    private fun copyFile(resourceFile: File?, targetPath: String, fileName: String): Boolean {
        var result = false
        if (resourceFile == null || TextUtils.isEmpty(targetPath)) {
            return result
        }
        val target = File(targetPath)
        if (target.exists()) {
            target.delete() // 已存在的话先删除
        } else {
            try {
                target.mkdirs()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        val targetFile = File(targetPath + fileName)
        if (targetFile.exists()) {
            targetFile.delete()
        } else {
            try {
                targetFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        var resourceChannel: FileChannel? = null
        var targetChannel: FileChannel? = null
        try {
            resourceChannel = FileInputStream(resourceFile).channel
            targetChannel = FileOutputStream(targetFile).channel
            resourceChannel!!.transferTo(0, resourceChannel.size(), targetChannel)
            result = true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return result
        } catch (e: IOException) {
            e.printStackTrace()
            return result
        }

        try {
            resourceChannel.close()
            targetChannel!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

}