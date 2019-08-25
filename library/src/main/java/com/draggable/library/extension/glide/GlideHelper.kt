package com.draggable.library.extension.glide

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.disklrucache.DiskLruCache
import com.bumptech.glide.load.engine.cache.DiskCache
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.transition.Transition
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.*
import java.nio.channels.FileChannel

object GlideHelper {

    private val TAG = javaClass.simpleName
    private val DOWNLOAD_FILE_NAME = "Download"

    //图片是否在 内存缓存 or  磁盘缓存
    fun imageIsInCache(context: Context, url: String): Boolean {
        try {
            //内存缓存
//            val image = Glide.with(context).downloadOnly().load(thumbnailUrl).apply(RequestOptions().onlyRetrieveFromCache(true)).submit().get()
//            if (image != null){
//                return true
//            }

            //磁盘缓存
            val safeKeyGenerator = SafeKeyGenerator()
            val safeKey = safeKeyGenerator.getSafeKey(GlideUrl(url))
            val file = File(context.cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR)
            val diskLruCache = DiskLruCache.open(file, 1, 1, DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE.toLong())
            val value = diskLruCache.get(safeKey)
            if (value != null && value.getFile(0).exists()) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false
    }

    fun downloadPicture(context: Context, url: String) {

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "没有打开存储权限", Toast.LENGTH_SHORT).show()
            return
        }

        Glide.with(context).downloadOnly().load(url).into(object : FileTarget() {
            override fun onLoadStarted(placeholder: Drawable?) {
                Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show()
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
            }

            override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                Log.d(TAG, "onResourceReady ....")
                try {
                    val downloadFolderName = DOWNLOAD_FILE_NAME
                    val path = Environment.getExternalStorageDirectory().toString() + "/" + downloadFolderName + "/"
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

                    val mimeType = getImageTypeWithMime(resource.absolutePath)
                    name = "$name.$mimeType"
                    createFileByDeleteOldFile(path + name)
                    val result = copyFile(resource, path, name)
                    if (result) {
                        Toast.makeText(context, "成功保存到 $path$name", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.d(TAG, "exception : ${e.message}")
                }
            }
        })
    }


    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param filePath The path of file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun createFileByDeleteOldFile(filePath: String): Boolean {
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
    fun copyFile(resourceFile: File?, targetPath: String, fileName: String): Boolean {
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