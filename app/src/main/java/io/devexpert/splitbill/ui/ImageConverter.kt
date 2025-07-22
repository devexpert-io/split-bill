package io.devexpert.splitbill.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream

object ImageConverter {

    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun toResizedByteArray(
        bitmap: Bitmap,
        maxWidth: Int = 1280,
        quality: Int = 90
    ): ByteArray {
        val resizedBitmap = resizeBitmap(bitmap, maxWidth)

        val stream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

        return stream.toByteArray()
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int): Bitmap {
        if (bitmap.width <= maxWidth) return bitmap
        val aspectRatio = bitmap.height.toFloat() / bitmap.width
        val newWidth = maxWidth
        val newHeight = (maxWidth * aspectRatio).toInt()
        return bitmap.scale(newWidth, newHeight)
    }
}