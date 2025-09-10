package com.example.myapplication.core.network.ml

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.myapplication.core.data.model.HabitCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages federated learning without requiring cloud services
 * Uses peer-to-peer model sharing via local file exports/imports
 */
@Singleton
class FederatedLearningManager @Inject constructor(
    private val context: Context,
    private val modelManager: ModelManager
) {
    companion object {
        private const val TAG = "FederatedLearning"
        private const val FEDERATED_DIR = "federated"
        private const val EXPORT_DIR = "exports"
        private const val IMPORT_DIR = "imports"
        private const val AGGREGATED_DIR = "aggregated"
        private const val FILE_PROVIDER_AUTHORITY = "com.example.myapplication.fileprovider"
    }
    
    private val federatedDir: File by lazy {
        File(context.filesDir, FEDERATED_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    private val exportDir: File by lazy {
        File(federatedDir, EXPORT_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    private val importDir: File by lazy {
        File(federatedDir, IMPORT_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    private val aggregatedDir: File by lazy {
        File(federatedDir, AGGREGATED_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Export a trained model for sharing
     * Returns a content URI that can be shared with other users
     */
    suspend fun exportModel(
        habitId: String,
        category: HabitCategory?,
        modelBuffer: ByteBuffer
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            // Create a unique filename
            val timestamp = System.currentTimeMillis()
            val categoryName = category?.name?.lowercase() ?: "uncategorized"
            val filename = "model_${categoryName}_${timestamp}.tflite"
            val exportFile = File(exportDir, filename)
            
            // Write model to file
            val outputStream = FileOutputStream(exportFile)
            val fileChannel = outputStream.channel
            
            // Prepare buffer for writing
            modelBuffer.rewind()
            
            // Write buffer to file
            fileChannel.write(modelBuffer)
            fileChannel.close()
            outputStream.close()
            
            // Create content URI for sharing
            val uri = FileProvider.getUriForFile(
                context,
                FILE_PROVIDER_AUTHORITY,
                exportFile
            )
            
            Log.d(TAG, "Exported model to: $uri")
            return@withContext uri
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting model: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
    
    /**
     * Import a model from a content URI
     * Returns true if import was successful
     */
    suspend fun importModel(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            // Create a unique filename for the imported model
            val timestamp = System.currentTimeMillis()
            val filename = "imported_${timestamp}.tflite"
            val importFile = File(importDir, filename)
            
            // Copy content from URI to file
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(importFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            Log.d(TAG, "Imported model to: ${importFile.absolutePath}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error importing model: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }
    
    /**
     * Aggregate imported models with local models
     * This implements a simple federated averaging algorithm
     */
    suspend fun aggregateModels(category: HabitCategory?): Boolean = withContext(Dispatchers.IO) {
        try {
            // Get all imported models
            val importedModels = importDir.listFiles()?.filter { it.name.endsWith(".tflite") } ?: emptyList()
            
            if (importedModels.isEmpty()) {
                Log.d(TAG, "No imported models to aggregate")
                return@withContext false
            }
            
            // Load all models into memory
            val modelBuffers = mutableListOf<ByteBuffer>()
            
            for (modelFile in importedModels) {
                val inputStream = FileInputStream(modelFile)
                val fileChannel = inputStream.channel
                val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length())
                modelBuffers.add(modelBuffer)
                fileChannel.close()
                inputStream.close()
            }
            
            // Simple model averaging (in a real implementation, this would be more sophisticated)
            val aggregatedModel = averageModels(modelBuffers)
            
            // Save the aggregated model
            val categoryName = category?.name?.lowercase() ?: "general"
            val timestamp = System.currentTimeMillis()
            val filename = "aggregated_${categoryName}_${timestamp}.tflite"
            val aggregatedFile = File(aggregatedDir, filename)
            
            val outputStream = FileOutputStream(aggregatedFile)
            val fileChannel = outputStream.channel
            
            // Write aggregated model to file
            fileChannel.write(aggregatedModel)
            fileChannel.close()
            outputStream.close()
            
            // If category is provided, save as category model
            if (category != null) {
                modelManager.saveCategoryModel(category.name, aggregatedModel)
            }
            
            // Clean up imported models
            for (modelFile in importedModels) {
                modelFile.delete()
            }
            
            Log.d(TAG, "Aggregated ${importedModels.size} models for category: ${category?.name ?: "general"}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error aggregating models: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }
    
    /**
     * Average multiple model buffers
     * This is a simplified implementation of federated averaging
     */
    private fun averageModels(modelBuffers: List<ByteBuffer>): ByteBuffer {
        if (modelBuffers.isEmpty()) {
            throw IllegalArgumentException("No models to average")
        }
        
        // For simplicity, we'll assume all models have the same size
        val firstModel = modelBuffers.first()
        val modelSize = firstModel.capacity()
        
        // Create a buffer for the averaged model
        val averagedModel = ByteBuffer.allocateDirect(modelSize)
        averagedModel.order(ByteOrder.nativeOrder())
        
        // Reset all buffers to the beginning
        modelBuffers.forEach { it.rewind() }
        
        // Average the weights
        val numModels = modelBuffers.size.toFloat()
        
        // Process 4 bytes (one float) at a time
        for (i in 0 until modelSize / 4) {
            var sum = 0f
            
            // Sum the weights from all models
            for (modelBuffer in modelBuffers) {
                sum += modelBuffer.getFloat()
            }
            
            // Calculate the average and add to the result
            val average = sum / numModels
            averagedModel.putFloat(average)
        }
        
        // Reset the buffer to the beginning
        averagedModel.rewind()
        
        return averagedModel
    }
    
    /**
     * Get the number of available imported models
     */
    fun getImportedModelCount(): Int {
        return importDir.listFiles()?.filter { it.name.endsWith(".tflite") }?.size ?: 0
    }
    
    /**
     * Get the number of aggregated models
     */
    fun getAggregatedModelCount(): Int {
        return aggregatedDir.listFiles()?.filter { it.name.endsWith(".tflite") }?.size ?: 0
    }
}
