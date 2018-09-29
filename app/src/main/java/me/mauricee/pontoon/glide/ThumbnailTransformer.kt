package me.mauricee.pontoon.glide

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.ByteBuffer
import java.security.MessageDigest

class ThumbnailTransformer(positionMs: Long) : BitmapTransformation() {

    private val framePosition: Int = (positionMs / FrameDuration).toInt()

    override fun transform(pool: BitmapPool, toTransform: Bitmap,
                           outWidth: Int, outHeight: Int): Bitmap {
        val width = toTransform.width
        val height = toTransform.height
        return Bitmap.createBitmap(toTransform, framePosition * width, height, width, height)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val data = ByteBuffer.allocate(8).putInt(framePosition).array()
        messageDigest.update(data)
    }

    override fun hashCode(): Int = framePosition.toString().hashCode()


    companion object {
        private const val FrameDuration = 4800
    }
}