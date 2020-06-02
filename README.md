
一款类似 微信/B站 的图片浏览组件, 主要具有以下特点:

1. 类似共享元素的入场&退场动画(图片在入场时给人一种渐渐展开的效果)
2. 拖拽&双击退出图片查看
3. 支持缩放手势
4. 支持查看长图
5. 支持查看原图 & 下载原图
6. 支持显示GIF & 可复用Glide的Bitmap内存缓存,避免OOM

# 快速使用

### 引入

```
dependencies {
    implementation 'com.susion:image-viewer:1.0.2'
}
```

### 展示一张图片:

```
ImageViewerHelper.showSimpleImage(context, url, imageView)
```

### 展示多张图片:

```
val imags = ArrayList<ImageViewerHelper.ImageInfo>()
imags.add(ImageViewerHelper.ImageInfo(url1))
imags.add(ImageViewerHelper.ImageInfo(url2))
ImageViewerHelper.showImages(this, listOf(mImagesIv1, mImagesIv2, mImagesIv3), imags, index)
```

### 缩略图的展示

支持优先展示缩略图，然后缓慢加载原图:

```
ImageViewerHelper.showSimpleImage(this, ImageViewerHelper.ImageInfo(thumbnailUrl, imgSize), imageView)
```

# 效果展示

![gif1](pic/gif1.gif)

![gif1](pic/gif2.gif)

![gif1](pic/gif3.gif)


# [实现原理分析](https://juejin.im/post/5d6b9a93f265da03970bd19f)


















