package com.group18.gosell.data.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.group18.gosell.BuildConfig // Import BuildConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Manager class for handling Cloudinary image uploads
 * Usage:
 * 1. Initialize once in your Application class:
 *    CloudinaryManager.init(applicationContext)
 * 
 * 2. Upload images:
 *    val imageUrl = CloudinaryManager.uploadImage(uri, context)
 */
object CloudinaryManager {
    private var isInitialized = false
    
    /**
     * Initialize Cloudinary with your configuration
     * This should be called once in your Application class
     */
    fun init(context: Context) {
        if (isInitialized) return
        
        try {
            // Use keys from BuildConfig
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET,
                "secure" to true
            )
            
            MediaManager.init(context, config)
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Upload an image to Cloudinary from a Uri
     * Returns the public URL of the uploaded image or null if upload failed
     */
    suspend fun uploadImage(imageUri: Uri, context: Context): String? = withContext(Dispatchers.IO) {
        val deferred = CompletableDeferred<String?>()
        
        try {
            // Convert Uri to File if needed
            val file = getFileFromUri(imageUri, context) ?: return@withContext null
            
            // Generate a unique filename
            val timestamp = System.currentTimeMillis()
            val uniqueFilename = "gosell_$timestamp"
            
            // Upload the file
            MediaManager.get().upload(file.absolutePath)
                .option("public_id", uniqueFilename)
                .option("folder", "gosell_app")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val url = resultData?.get("secure_url") as? String
                        deferred.complete(url)
                    }
                    
                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        deferred.complete(null)
                    }
                    
                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        deferred.complete(null)
                    }
                })
                .dispatch()
        } catch (e: Exception) {
            deferred.complete(null)
        }
        
        return@withContext deferred.await()
    }
    
    /**
     * Upload multiple images to Cloudinary
     * Returns a list of URLs for the uploaded images
     */
    suspend fun uploadImages(imageUris: List<Uri>, context: Context): List<String> = withContext(Dispatchers.IO) {
        val uploadedUrls = mutableListOf<String>()
        
        for (uri in imageUris) {
            val url = uploadImage(uri, context)
            if (url != null) {
                uploadedUrls.add(url)
            }
        }
        
        return@withContext uploadedUrls
    }
    
    /**
     * Convert a Uri to a File
     */
    private fun getFileFromUri(uri: Uri, context: Context): File? {
        try {
            // For content URIs, we need to create a temporary file
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                val outputFile = File.createTempFile("temp_image_", ".jpg", context.cacheDir)
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                    return outputFile
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}