package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.model.*

@Database(
    entities = [
        Habit::class,
        HabitNeuralNetwork::class,
        NeuralNode::class,
        NeuralConnection::class,
        NeuralActivation::class,
        NeuralTrainingSession::class,
        NeuralTrainingEpoch::class,
        NeuralPrediction::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun neuralNetworkDao(): NeuralNetworkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This is a fallback migration - since we don't know the exact schema changes,
                // we're just updating the version number without making schema changes.
                // In a production app, you would add the appropriate ALTER TABLE statements here.
            }
        }

        // Migration from version 2 to 3: Adding lastUpdatedTimestamp
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new lastUpdatedTimestamp column to the habits table
                database.execSQL("ALTER TABLE habits ADD COLUMN lastUpdatedTimestamp INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 3 to 4: To resolve identity hash mismatch
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // This migration is added to address a potential schema mismatch.
            }
        }

        // Migration from version 4 to 5: Adding neural network tables
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create habit_neural_networks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS habit_neural_networks (
                        id TEXT NOT NULL PRIMARY KEY,
                        habitId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)

                // Create index on habitId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_habit_neural_networks_habitId ON habit_neural_networks(habitId)")

                // Create neural_nodes table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS neural_nodes (
                        id TEXT NOT NULL PRIMARY KEY,
                        networkId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        label TEXT,
                        positionX REAL NOT NULL,
                        positionY REAL NOT NULL,
                        activationLevel REAL NOT NULL,
                        bias REAL NOT NULL,
                        metadata TEXT,
                        FOREIGN KEY (networkId) REFERENCES habit_neural_networks(id) ON DELETE CASCADE
                    )
                """)

                // Create index on networkId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_nodes_networkId ON neural_nodes(networkId)")

                // Create neural_connections table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS neural_connections (
                        id TEXT NOT NULL PRIMARY KEY,
                        networkId TEXT NOT NULL,
                        sourceNodeId TEXT NOT NULL,
                        targetNodeId TEXT NOT NULL,
                        weight REAL NOT NULL,
                        enabled INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY (networkId) REFERENCES habit_neural_networks(id) ON DELETE CASCADE,
                        FOREIGN KEY (sourceNodeId) REFERENCES neural_nodes(id) ON DELETE CASCADE,
                        FOREIGN KEY (targetNodeId) REFERENCES neural_nodes(id) ON DELETE CASCADE
                    )
                """)

                // Create indices for neural_connections
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_connections_networkId ON neural_connections(networkId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_connections_sourceNodeId ON neural_connections(sourceNodeId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_connections_targetNodeId ON neural_connections(targetNodeId)")

                // Create neural_activations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS neural_activations (
                        id TEXT NOT NULL PRIMARY KEY,
                        nodeId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        activationLevel REAL NOT NULL,
                        source TEXT,
                        FOREIGN KEY (nodeId) REFERENCES neural_nodes(id) ON DELETE CASCADE
                    )
                """)

                // Create index on nodeId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_activations_nodeId ON neural_activations(nodeId)")

                // Create neural_training_sessions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS neural_training_sessions (
                        id TEXT NOT NULL PRIMARY KEY,
                        networkId TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER,
                        epochs INTEGER NOT NULL,
                        learningRate REAL NOT NULL,
                        finalLoss REAL,
                        finalAccuracy REAL,
                        status TEXT NOT NULL,
                        FOREIGN KEY (networkId) REFERENCES habit_neural_networks(id) ON DELETE CASCADE
                    )
                """)

                // Create index on networkId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_training_sessions_networkId ON neural_training_sessions(networkId)")

                // Create neural_training_epochs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS neural_training_epochs (
                        id TEXT NOT NULL PRIMARY KEY,
                        sessionId TEXT NOT NULL,
                        epochNumber INTEGER NOT NULL,
                        loss REAL NOT NULL,
                        accuracy REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY (sessionId) REFERENCES neural_training_sessions(id) ON DELETE CASCADE
                    )
                """)

                // Create index on sessionId
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_training_epochs_sessionId ON neural_training_epochs(sessionId)")

                // Create neural_predictions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS neural_predictions (
                        id TEXT NOT NULL PRIMARY KEY,
                        networkId TEXT NOT NULL,
                        habitId TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        predictionType TEXT NOT NULL,
                        probability REAL NOT NULL,
                        confidence REAL NOT NULL,
                        metadata TEXT,
                        FOREIGN KEY (networkId) REFERENCES habit_neural_networks(id) ON DELETE CASCADE,
                        FOREIGN KEY (habitId) REFERENCES habits(id) ON DELETE CASCADE
                    )
                """)

                // Create indices for neural_predictions
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_predictions_networkId ON neural_predictions(networkId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_neural_predictions_habitId ON neural_predictions(habitId)")
            }
        }

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Add all migrations
                .fallbackToDestructiveMigration() // As a last resort, recreate the database
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
