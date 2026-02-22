package com.example.myapplication.core.data.repository

import com.example.myapplication.core.data.model.SpatialObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory repository for spatial objects.
 * Spatial objects are purely session-scoped visualisation artefacts and do not
 * need to survive app restarts, so no Room persistence is required.
 */
@Singleton
class SpatialRepository @Inject constructor() {

    private val objects = mutableListOf<SpatialObject>()

    fun getSpatialObjects(): List<SpatialObject> = objects.toList()

    fun addSpatialObject(obj: SpatialObject) {
        objects.add(obj)
    }

    fun updateSpatialObject(obj: SpatialObject) {
        val index = objects.indexOfFirst { it.id == obj.id }
        if (index >= 0) objects[index] = obj
    }

    fun deleteSpatialObject(objectId: String) {
        objects.removeAll { it.id == objectId }
    }
}
