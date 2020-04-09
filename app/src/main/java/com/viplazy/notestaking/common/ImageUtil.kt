package com.viplazy.notestaking.common

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.viplazy.notestaking.R
import com.viplazy.notestaking.ui.main.NoteDetailFragment.Companion.THUMBNAIL_SIZE
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


object ImageUtil {
    @Throws(IllegalArgumentException::class)
    fun convert(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(
            base64Str.substring(base64Str.indexOf(",") + 1),
            Base64.DEFAULT
        )
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    fun convert(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(
            outputStream.toByteArray(),
            Base64.DEFAULT
        )
    }

    @Throws(IOException::class)
    fun saveBitmap(context: Context, fileName: String,  bitmap: Bitmap): File {

        val appPath =
            context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.dataDir

        val f = File(appPath + File.separator + fileName)
        f.createNewFile()

        val fo = FileOutputStream(f)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fo)

        fo.flush()
        fo.close()

        return f
    }

    @Throws(IOException::class)
    fun loadBitmap(context: Context, filePath: String): Bitmap? {
        var result : Bitmap?

        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        result = BitmapFactory.decodeFile(filePath, options)

        if (result == null) {
            result = BitmapFactory.decodeResource(
                context.resources,
                R.drawable.ic_no_photos
            )
        }
        return result
    }

    @Suppress("DEPRECATION")
    fun getThumbnail(activity: Activity, uri: Uri): Bitmap? {
        var inputStream = activity.contentResolver.openInputStream(uri)

        val onlyBoundsOptions = BitmapFactory.Options()

        onlyBoundsOptions.inJustDecodeBounds = true
        onlyBoundsOptions.inDither = true//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888//optional

        BitmapFactory.decodeStream(inputStream, null, onlyBoundsOptions)
        inputStream?.close()

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null
        }

        val originalSize =
            if (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) onlyBoundsOptions.outHeight else onlyBoundsOptions.outWidth

        val ratio = if (originalSize > THUMBNAIL_SIZE) (originalSize / THUMBNAIL_SIZE) else 1.0

        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inSampleSize = ratio.toInt()

        bitmapOptions.inDither = true //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888//
        inputStream = activity.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOptions)
        inputStream?.close()

        return bitmap
    }
}