package com.example.myapplication.data.ml

import android.content.Context
import android.util.Log
import com.example.myapplication.data.model.HabitCategory
import com.example.myapplication.data.model.HabitFrequency
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
 * Manages neural network models, including transfer learning capabilities
 */
@Singleton
class ModelManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ModelManager"
        private const val BASE_MODEL_PATH = "neural_network_model.tflite"
        private const val MODELS_DIR = "models"
        
        // Model categories
        private val EXERCISE_CATEGORIES = setOf(
            "Exercise", "Fitness", "Workout", "Running", "Gym", "Sports"
        )
        
        private val HEALTH_CATEGORIES = setOf(
            "Health", "Nutrition", "Diet", "Meditation", "Sleep", "Wellness"
        )
        
        private val PRODUCTIVITY_CATEGORIES = setOf(
            "Productivity", "Work", "Study", "Reading", "Learning", "Career"
        )
    }
    
    private val modelsDir: File by lazy {
        File(context.filesDir, MODELS_DIR).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Get the best model for a habit based on its properties
     */
    suspend fun getBestModelForHabit(
        habitId: String,
        category: HabitCategory?,
        frequency: HabitFrequency
    ): MappedByteBuffer = withContext(Dispatchers.IO) {
        try {
            // First, check if there's a model specifically for this habit
            val habitModel = getHabitSpecificModel(habitId)
            if (habitModel != null) {
                Log.d(TAG, "Using habit-specific model for habit: $habitId")
                return@withContext habitModel
            }
            
            // Next, check if there's a model for this category
            if (category != null) {
                val categoryModel = getCategoryModel(category.name)
                if (categoryModel != null) {
                    Log.d(TAG, "Using category model for category: ${category.name}")
                    return@withContext categoryModel
                }
            }
            
            // Next, check if there's a model for this frequency
            val frequencyModel = getFrequencyModel(frequency)
            if (frequencyModel != null) {
                Log.d(TAG, "Using frequency model for frequency: $frequency")
                return@withContext frequencyModel
            }
            
            // Finally, fall back to the base model
            Log.d(TAG, "Using base model")
            return@withContext loadBaseModel()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting best model: ${e.message}")
            e.printStackTrace()
            return@withContext createFallbackModel()
        }
    }
    
    /**
     * Load the base model from assets
     */
    private fun loadBaseModel(): MappedByteBuffer {
        try {
            val fileDescriptor = context.assets.openFd(BASE_MODEL_PATH)
            val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading base model: ${e.message}")
            e.printStackTrace()
            return createFallbackModel()
        }
    }
    
    /**
     * Get a model specific to a habit
     */
    private fun getHabitSpecificModel(habitId: String): MappedByteBuffer? {
        val modelFile = File(modelsDir, "habit_$habitId.tflite")
        if (!modelFile.exists()) {
            return null
        }
        
        return loadModelFromFile(modelFile)
    }
    
    /**
     * Get a model for a specific category
     */
    private fun getCategoryModel(category: String): MappedByteBuffer? {
        // Determine the category group
        val categoryGroup = when {
            EXERCISE_CATEGORIES.any { it.equals(category, ignoreCase = true) } -> "exercise"
            HEALTH_CATEGORIES.any { it.equals(category, ignoreCase = true) } -> "health"
            PRODUCTIVITY_CATEGORIES.any { it.equals(category, ignoreCase = true) } -> "productivity"
            else -> null
        }
        
        if (categoryGroup == null) {
            return null
        }
        
        val modelFile = File(modelsDir, "category_$categoryGroup.tflite")
        if (!modelFile.exists()) {
            return null
        }
        
        return loadModelFromFile(modelFile)
    }
    
    /**
     * Get a model for a specific frequency
     */
    private fun getFrequencyModel(frequency: HabitFrequency): MappedByteBuffer? {
        val modelFile = File(modelsDir, "frequency_${frequency.name.lowercase()}.tflite")
        if (!modelFile.exists()) {
            return null
        }
        
        return loadModelFromFile(modelFile)
    }
    
    /**
     * Load a model from a file
     */
    private fun loadModelFromFile(file: File): MappedByteBuffer? {
        try {
            val inputStream = FileInputStream(file)
            val fileChannel = inputStream.channel
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            fileChannel.close()
            inputStream.close()
            return modelBuffer
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model from file: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Save a trained model for a specific habit
     */
    suspend fun saveHabitModel(habitId: String, modelBuffer: ByteBuffer): Boolean = withContext(Dispatchers.IO) {
        try {
            val modelFile = File(modelsDir, "habit_$habitId.tflite")
            val outputStream = FileOutputStream(modelFile)
            val fileChannel = outputStream.channel
            
            // Prepare buffer for writing
            modelBuffer.rewind()
            
            // Write buffer to file
            fileChannel.write(modelBuffer)
            fileChannel.close()
            outputStream.close()
            
            Log.d(TAG, "Saved model for habit: $habitId")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving habit model: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }
    
    /**
     * Save a trained model for a category
     */
    suspend fun saveCategoryModel(category: String, modelBuffer: ByteBuffer): Boolean = withContext(Dispatchers.IO) {
        try {
            // Determine the category group
            val categoryGroup = when {
                EXERCISE_CATEGORIES.any { it.equals(category, ignoreCase = true) } -> "exercise"
                HEALTH_CATEGORIES.any { it.equals(category, ignoreCase = true) } -> "health"
                PRODUCTIVITY_CATEGORIES.any { it.equals(category, ignoreCase = true) } -> "productivity"
                else -> category.lowercase()
            }
            
            val modelFile = File(modelsDir, "category_$categoryGroup.tflite")
            val outputStream = FileOutputStream(modelFile)
            val fileChannel = outputStream.channel
            
            // Prepare buffer for writing
            modelBuffer.rewind()
            
            // Write buffer to file
            fileChannel.write(modelBuffer)
            fileChannel.close()
            outputStream.close()
            
            Log.d(TAG, "Saved model for category: $category")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving category model: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }
    
    /**
     * Create a fallback model in memory
     */
    private fun createFallbackModel(): MappedByteBuffer {
        // Create a simple model with random weights
        val modelSize = 1024 * 10 // 10KB model
        val buffer = ByteBuffer.allocateDirect(modelSize)
        buffer.order(ByteOrder.nativeOrder())
        
        // Fill with random data to simulate a model
        val random = Random()
        for (i in 0 until modelSize / 4) {
            buffer.putFloat(random.nextFloat() * 2 - 1) // Random weights between -1 and 1
        }
        
        // Reset position to beginning
        buffer.rewind()
        
        // Create a MappedByteBuffer from the ByteBuffer
        val field = buffer.javaClass.getDeclaredField("address")
        field.isAccessible = true
        val address = field.getLong(buffer)
        
        val constructor = Class.forName("java.nio.DirectByteBuffer")
            .getDeclaredConstructor(Long::class.java, Int::class.java)
        constructor.isAccessible = true
        
        return constructor.newInstance(address, modelSize) as MappedByteBuffer
    }
}
