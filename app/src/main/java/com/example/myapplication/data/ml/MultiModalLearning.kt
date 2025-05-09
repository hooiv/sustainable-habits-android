package com.example.myapplication.data.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.Log
import com.example.myapplication.data.model.Habit
import com.example.myapplication.data.model.HabitCompletion
import com.example.myapplication.data.model.MultiModalFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

/**
 * Implements multi-modal learning for habit data
 * Processes text, images, and sensor data
 */
@Singleton
class MultiModalLearning @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "MultiModalLearning"
        
        // Feature extraction parameters
        private const val IMAGE_SIZE = 224 // Size for image features
        private const val TEXT_EMBEDDING_SIZE = 64 // Size for text embeddings
        private const val SENSOR_FEATURE_SIZE = 32 // Size for sensor features
        
        // Fusion parameters
        private const val FUSION_FEATURE_SIZE = 128 // Size for fused features
    }
    
    // Extracted features
    private val _imageFeatures = MutableStateFlow<FloatArray?>(null)
    val imageFeatures: StateFlow<FloatArray?> = _imageFeatures.asStateFlow()
    
    private val _textFeatures = MutableStateFlow<FloatArray?>(null)
    val textFeatures: StateFlow<FloatArray?> = _textFeatures.asStateFlow()
    
    private val _sensorFeatures = MutableStateFlow<FloatArray?>(null)
    val sensorFeatures: StateFlow<FloatArray?> = _sensorFeatures.asStateFlow()
    
    private val _fusedFeatures = MutableStateFlow<FloatArray?>(null)
    val fusedFeatures: StateFlow<FloatArray?> = _fusedFeatures.asStateFlow()
    
    /**
     * Extract features from an image
     */
    suspend fun extractImageFeatures(imageUri: Uri): FloatArray? = withContext(Dispatchers.IO) {
        try {
            // Load image from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode image from URI: $imageUri")
                return@withContext null
            }
            
            // Resize image to square
            val size = min(bitmap.width, bitmap.height)
            val squareBitmap = ThumbnailUtils.extractThumbnail(bitmap, size, size)
            
            // Resize to target size
            val scaledBitmap = Bitmap.createScaledBitmap(squareBitmap, IMAGE_SIZE, IMAGE_SIZE, true)
            
            // Extract features (in a real app, this would use a pre-trained model)
            val features = extractFeaturesFromBitmap(scaledBitmap)
            
            // Update state
            _imageFeatures.value = features
            
            Log.d(TAG, "Extracted image features: ${features.size} features")
            
            return@withContext features
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting image features: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
    
    /**
     * Extract features from text
     */
    suspend fun extractTextFeatures(text: String): FloatArray = withContext(Dispatchers.Default) {
        try {
            // In a real app, this would use a pre-trained text embedding model
            // For this demo, we'll use a simple bag-of-words approach
            
            // Normalize text
            val normalizedText = text.lowercase().replace(Regex("[^a-z0-9\\s]"), "")
            
            // Split into words
            val words = normalizedText.split(Regex("\\s+"))
            
            // Create a simple embedding
            val features = FloatArray(TEXT_EMBEDDING_SIZE) { 0f }
            
            // Hash each word and update features
            for (word in words) {
                val hash = word.hashCode() and 0x7FFFFFFF
                val index = hash % TEXT_EMBEDDING_SIZE
                features[index] += 1f
            }
            
            // Normalize features
            val norm = features.map { it * it }.sum().let { Math.sqrt(it.toDouble()).toFloat() }
            if (norm > 0) {
                for (i in features.indices) {
                    features[i] /= norm
                }
            }
            
            // Update state
            _textFeatures.value = features
            
            Log.d(TAG, "Extracted text features: ${features.size} features")
            
            return@withContext features
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting text features: ${e.message}")
            e.printStackTrace()
            return@withContext FloatArray(TEXT_EMBEDDING_SIZE) { 0f }
        }
    }
    
    /**
     * Extract features from sensor data
     */
    suspend fun extractSensorFeatures(sensorData: FloatArray): FloatArray = withContext(Dispatchers.Default) {
        try {
            // In a real app, this would process raw sensor data
            // For this demo, we'll just use the data as-is
            
            // Ensure the feature size is correct
            val features = FloatArray(SENSOR_FEATURE_SIZE) { 0f }
            
            // Copy sensor data to features
            for (i in 0 until min(sensorData.size, SENSOR_FEATURE_SIZE)) {
                features[i] = sensorData[i]
            }
            
            // Update state
            _sensorFeatures.value = features
            
            Log.d(TAG, "Extracted sensor features: ${features.size} features")
            
            return@withContext features
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting sensor features: ${e.message}")
            e.printStackTrace()
            return@withContext FloatArray(SENSOR_FEATURE_SIZE) { 0f }
        }
    }
    
    /**
     * Fuse features from multiple modalities
     */
    suspend fun fuseFeatures(
        imageFeatures: FloatArray?,
        textFeatures: FloatArray?,
        sensorFeatures: FloatArray?
    ): FloatArray = withContext(Dispatchers.Default) {
        try {
            // Create fused feature vector
            val fusedFeatures = FloatArray(FUSION_FEATURE_SIZE) { 0f }
            
            // Simple concatenation and projection
            // In a real app, this would use a more sophisticated fusion method
            
            var offset = 0
            
            // Add image features
            if (imageFeatures != null) {
                val size = min(imageFeatures.size, FUSION_FEATURE_SIZE / 3)
                for (i in 0 until size) {
                    fusedFeatures[offset + i] = imageFeatures[i]
                }
                offset += size
            }
            
            // Add text features
            if (textFeatures != null) {
                val size = min(textFeatures.size, FUSION_FEATURE_SIZE / 3)
                for (i in 0 until size) {
                    fusedFeatures[offset + i] = textFeatures[i]
                }
                offset += size
            }
            
            // Add sensor features
            if (sensorFeatures != null) {
                val size = min(sensorFeatures.size, FUSION_FEATURE_SIZE / 3)
                for (i in 0 until size) {
                    fusedFeatures[offset + i] = sensorFeatures[i]
                }
            }
            
            // Update state
            _fusedFeatures.value = fusedFeatures
            
            Log.d(TAG, "Fused features: ${fusedFeatures.size} features")
            
            return@withContext fusedFeatures
        } catch (e: Exception) {
            Log.e(TAG, "Error fusing features: ${e.message}")
            e.printStackTrace()
            return@withContext FloatArray(FUSION_FEATURE_SIZE) { 0f }
        }
    }
    
    /**
     * Process multi-modal data for a habit completion
     */
    suspend fun processMultiModalData(
        habit: Habit,
        completion: HabitCompletion,
        imageUri: Uri?,
        notes: String?,
        sensorData: FloatArray?
    ): MultiModalFeature = withContext(Dispatchers.Default) {
        try {
            // Extract features from each modality
            val imageFeatures = if (imageUri != null) {
                extractImageFeatures(imageUri)
            } else {
                null
            }
            
            val textFeatures = if (!notes.isNullOrBlank()) {
                extractTextFeatures(notes)
            } else {
                null
            }
            
            val extractedSensorFeatures = if (sensorData != null) {
                extractSensorFeatures(sensorData)
            } else {
                null
            }
            
            // Fuse features
            val fusedFeatures = fuseFeatures(imageFeatures, textFeatures, extractedSensorFeatures)
            
            // Create multi-modal feature
            val feature = MultiModalFeature(
                id = UUID.randomUUID().toString(),
                habitId = habit.id,
                completionId = completion.id,
                timestamp = System.currentTimeMillis(),
                imageFeatures = imageFeatures?.let { compressFeatures(it) },
                textFeatures = textFeatures?.let { compressFeatures(it) },
                sensorFeatures = extractedSensorFeatures?.let { compressFeatures(it) },
                fusedFeatures = compressFeatures(fusedFeatures)
            )
            
            Log.d(TAG, "Processed multi-modal data for habit: ${habit.name}")
            
            return@withContext feature
        } catch (e: Exception) {
            Log.e(TAG, "Error processing multi-modal data: ${e.message}")
            e.printStackTrace()
            
            // Return empty feature
            return@withContext MultiModalFeature(
                id = UUID.randomUUID().toString(),
                habitId = habit.id,
                completionId = completion.id,
                timestamp = System.currentTimeMillis(),
                imageFeatures = null,
                textFeatures = null,
                sensorFeatures = null,
                fusedFeatures = ByteArray(0)
            )
        }
    }
    
    /**
     * Extract features from a bitmap
     */
    private fun extractFeaturesFromBitmap(bitmap: Bitmap): FloatArray {
        // In a real app, this would use a pre-trained model
        // For this demo, we'll use a simple color histogram
        
        val features = FloatArray(IMAGE_SIZE) { 0f }
        
        // Calculate color histograms
        val redHistogram = IntArray(16) { 0 }
        val greenHistogram = IntArray(16) { 0 }
        val blueHistogram = IntArray(16) { 0 }
        
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val red = (pixel shr 16) and 0xFF
                val green = (pixel shr 8) and 0xFF
                val blue = pixel and 0xFF
                
                redHistogram[red / 16]++
                greenHistogram[green / 16]++
                blueHistogram[blue / 16]++
            }
        }
        
        // Normalize histograms
        val pixelCount = bitmap.width * bitmap.height
        
        for (i in 0 until 16) {
            features[i] = redHistogram[i].toFloat() / pixelCount
            features[i + 16] = greenHistogram[i].toFloat() / pixelCount
            features[i + 32] = blueHistogram[i].toFloat() / pixelCount
        }
        
        // Add some edge detection features
        // (In a real app, this would be more sophisticated)
        var edgeCount = 0
        for (y in 1 until bitmap.height - 1) {
            for (x in 1 until bitmap.width - 1) {
                val center = bitmap.getPixel(x, y)
                val left = bitmap.getPixel(x - 1, y)
                val right = bitmap.getPixel(x + 1, y)
                val top = bitmap.getPixel(x, y - 1)
                val bottom = bitmap.getPixel(x, y + 1)
                
                val centerGray = (center and 0xFF) + ((center shr 8) and 0xFF) + ((center shr 16) and 0xFF)
                val leftGray = (left and 0xFF) + ((left shr 8) and 0xFF) + ((left shr 16) and 0xFF)
                val rightGray = (right and 0xFF) + ((right shr 8) and 0xFF) + ((right shr 16) and 0xFF)
                val topGray = (top and 0xFF) + ((top shr 8) and 0xFF) + ((top shr 16) and 0xFF)
                val bottomGray = (bottom and 0xFF) + ((bottom shr 8) and 0xFF) + ((bottom shr 16) and 0xFF)
                
                val dx = Math.abs(leftGray - rightGray)
                val dy = Math.abs(topGray - bottomGray)
                
                if (dx > 100 || dy > 100) {
                    edgeCount++
                }
            }
        }
        
        // Add edge density feature
        features[48] = edgeCount.toFloat() / pixelCount
        
        return features
    }
    
    /**
     * Compress features to save space
     */
    private fun compressFeatures(features: FloatArray): ByteArray {
        // Convert to bytes
        val buffer = ByteBuffer.allocate(features.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        
        for (feature in features) {
            buffer.putFloat(feature)
        }
        
        // Compress using simple run-length encoding
        val bytes = buffer.array()
        val outputStream = ByteArrayOutputStream()
        
        var count = 1
        var current = bytes[0]
        
        for (i in 1 until bytes.size) {
            if (bytes[i] == current && count < 255) {
                count++
            } else {
                outputStream.write(count)
                outputStream.write(current.toInt())
                current = bytes[i]
                count = 1
            }
        }
        
        outputStream.write(count)
        outputStream.write(current.toInt())
        
        return outputStream.toByteArray()
    }
    
    /**
     * Decompress features
     */
    fun decompressFeatures(compressed: ByteArray, featureSize: Int): FloatArray {
        // Decompress using run-length decoding
        val decompressed = ByteArray(featureSize * 4)
        var index = 0
        
        var i = 0
        while (i < compressed.size - 1) {
            val count = compressed[i].toInt() and 0xFF
            val value = compressed[i + 1]
            
            for (j in 0 until count) {
                if (index < decompressed.size) {
                    decompressed[index++] = value
                }
            }
            
            i += 2
        }
        
        // Convert to floats
        val buffer = ByteBuffer.wrap(decompressed)
        buffer.order(ByteOrder.nativeOrder())
        
        val features = FloatArray(featureSize)
        for (j in features.indices) {
            features[j] = buffer.getFloat()
        }
        
        return features
    }
}
