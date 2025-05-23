/**
 * Module information for the Sustainable Habits Android application.
 * This enables compatibility with Java 9+ module system.
 */
module com.example.myapplication {
    // Required Java modules
    requires java.base;
    
    // Android modules
    requires android.content;
    requires androidx.activity;
    requires androidx.annotation;
    requires androidx.appcompat;
    requires androidx.compose.animation;
    requires androidx.compose.foundation;
    requires androidx.compose.material;
    requires androidx.compose.material3;
    requires androidx.compose.runtime;
    requires androidx.compose.ui;
    requires androidx.core;
    requires androidx.datastore.preferences;
    requires androidx.hilt.navigation.compose;
    requires androidx.lifecycle.viewmodel;
    requires androidx.navigation.compose;
    requires androidx.room.runtime;
    
    // Dagger/Hilt modules
    requires dagger.hilt.android;
    requires javax.inject;
    
    // Kotlin modules
    requires kotlin.stdlib;
    requires kotlinx.coroutines.core;
    requires kotlinx.coroutines.android;
    
    // Google modules
    requires com.google.gson;
    
    // Exports
    exports com.example.myapplication;
    exports com.example.myapplication.data.ai;
    exports com.example.myapplication.data.database;
    exports com.example.myapplication.data.model;
    exports com.example.myapplication.data.repository;
    exports com.example.myapplication.features.advanced;
    exports com.example.myapplication.features.ai;
    exports com.example.myapplication.features.animation;
    exports com.example.myapplication.features.ar;
    exports com.example.myapplication.features.auth;
    exports com.example.myapplication.features.biometric;
    exports com.example.myapplication.features.demo;
    exports com.example.myapplication.features.habits;
    exports com.example.myapplication.features.ml;
    exports com.example.myapplication.features.neural;
    exports com.example.myapplication.features.quantum;
    exports com.example.myapplication.features.settings;
    exports com.example.myapplication.features.spatial;
    exports com.example.myapplication.features.stats;
    exports com.example.myapplication.features.threejs;
    exports com.example.myapplication.features.voice;
    exports com.example.myapplication.navigation;
    exports com.example.myapplication.ui.theme;
    exports com.example.myapplication.util;
}
