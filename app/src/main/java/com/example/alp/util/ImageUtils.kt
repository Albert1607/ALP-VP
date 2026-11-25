package com.example.alp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    private const val MAX_SIZE_BYTES = 6 * 1024 * 1024L // 6 MB
    private const val MAX_SIDE = 1024

    fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        requireNotNull(inputStream) { "Failed to open input stream from URI" }
        val bytes = inputStream.readBytes()
        inputStream.close()

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        var inSampleSize = 1
        val height = options.outHeight
        val width = options.outWidth
        if (height > MAX_SIDE || width > MAX_SIDE) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= MAX_SIDE && (halfWidth / inSampleSize) >= MAX_SIDE) {
                inSampleSize *= 2
            }
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        requireNotNull(bitmap) { "Failed to decode bitmap from image" }

        val longest = maxOf(bitmap.width, bitmap.height)
        if (longest > MAX_SIDE) {
            val scale = MAX_SIDE.toFloat() / longest
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }

        try {
            val exifInputStream = context.contentResolver.openInputStream(uri)
            if (exifInputStream != null) {
                val exif = ExifInterface(exifInputStream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotationAngle = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                if (rotationAngle != 0f) {
                    val matrix = Matrix().apply { postRotate(rotationAngle) }
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                }
                exifInputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val compressedBytes = outputStream.toByteArray()

        require(compressedBytes.size <= MAX_SIZE_BYTES) {
            "Image size exceeds maximum allowed size of 6 MB"
        }

        return Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
    }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val compressedBytes = outputStream.toByteArray()
        require(compressedBytes.size <= MAX_SIZE_BYTES) {
            "Image size exceeds maximum allowed size of 6 MB"
        }
        return Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
    }
}
