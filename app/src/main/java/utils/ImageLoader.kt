package cl.duoc.level_up_mobile.utils

import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import java.io.IOException

object ImageLoader {
    fun loadImageFromAssets(context: Context, imagePath: String): ImageBitmap? {
        return try {
            val cleanPath = imagePath.removePrefix("assets/")
            context.assets.open(cleanPath).use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.asImageBitmap()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}