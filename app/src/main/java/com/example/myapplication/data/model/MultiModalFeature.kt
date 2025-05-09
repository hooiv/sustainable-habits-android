package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents multi-modal features extracted from habit completions
 */
@Entity(
    tableName = "multi_modal_features",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HabitCompletion::class,
            parentColumns = ["id"],
            childColumns = ["completionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("habitId"),
        Index("completionId")
    ]
)
data class MultiModalFeature(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val completionId: String,
    val timestamp: Long,
    val imageFeatures: ByteArray?,
    val textFeatures: ByteArray?,
    val sensorFeatures: ByteArray?,
    val fusedFeatures: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MultiModalFeature

        if (id != other.id) return false
        if (habitId != other.habitId) return false
        if (completionId != other.completionId) return false
        if (timestamp != other.timestamp) return false
        if (imageFeatures != null) {
            if (other.imageFeatures == null) return false
            if (!imageFeatures.contentEquals(other.imageFeatures)) return false
        } else if (other.imageFeatures != null) return false
        if (textFeatures != null) {
            if (other.textFeatures == null) return false
            if (!textFeatures.contentEquals(other.textFeatures)) return false
        } else if (other.textFeatures != null) return false
        if (sensorFeatures != null) {
            if (other.sensorFeatures == null) return false
            if (!sensorFeatures.contentEquals(other.sensorFeatures)) return false
        } else if (other.sensorFeatures != null) return false
        if (!fusedFeatures.contentEquals(other.fusedFeatures)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + habitId.hashCode()
        result = 31 * result + completionId.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (imageFeatures?.contentHashCode() ?: 0)
        result = 31 * result + (textFeatures?.contentHashCode() ?: 0)
        result = 31 * result + (sensorFeatures?.contentHashCode() ?: 0)
        result = 31 * result + fusedFeatures.contentHashCode()
        return result
    }
}
