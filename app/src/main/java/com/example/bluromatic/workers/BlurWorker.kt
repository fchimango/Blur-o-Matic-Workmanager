package com.example.bluromatic.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import com.example.bluromatic.KEY_BLUR_LEVEL
import com.example.bluromatic.KEY_IMAGE_URI
private const val TAG = "BlurWorker"

class BlurWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        makeStatusNotification("Blurring image", applicationContext)

        return withContext(Dispatchers.IO) {
            return@withContext try {
                val resourceUri = inputData.getString(KEY_IMAGE_URI)
                val blurLevel = inputData.getInt(KEY_BLUR_LEVEL, 1)

                require(!resourceUri.isNullOrBlank()) { "Invalid input uri" }
                val resolver = applicationContext.contentResolver

                val picture = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )
                val output = blurBitmap(picture, blurLevel)
                val outputUri = writeBitmapToFile(applicationContext, output)
                makeStatusNotification("Output is $outputUri", applicationContext)

                Result.success()
            } catch (throwable: Throwable) {
                Log.e(TAG, "Error applying blur", throwable)
                Result.failure()
            }
        }
    }
}