import androidx.compose.material.icons.automirrored.filled.*

// ... (inside Composable)

            FeatureCard(
                title = "Advanced Analytics",
                description = "Deep insights into your habit patterns",
                icon = Icons.AutoMirrored.Filled.Insights,
                onClick = {
                    navController.navigate(NavRoutes.ADVANCED_ANALYTICS)
                }
            )

// ...

            FeatureCard(
                title = "Biometric Visualization",
                description = "Visualize your biometric data",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                onClick = {
                    navController.navigate(NavRoutes.BIOMETRIC_INTEGRATION_GLOBAL)
                }
            )

// ...

            FeatureCard(
                title = "Predictive ML",
                description = "ML-powered predictions about your habits",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                onClick = {
                    navController.navigate(NavRoutes.PREDICTIVE_ML)
                }
            )

// ...

            FeatureCard(
                title = "Spatial Computing",
                description = "Explore habits in 3D space",
                icon = Icons.Filled.ViewInAr, // Explicit Filled
                onClick = {
                    navController.navigate(NavRoutes.SPATIAL_COMPUTING)
                }
            )
            
            FeatureCard(
                title = "Quantum Visualization",
                description = "Visualize your habits with quantum-inspired algorithms",
                icon = Icons.Filled.Biotech, // Explicit Filled
                onClick = {
                    navController.navigate(NavRoutes.QUANTUM_VISUALIZATION)
                }
            )
            
            FeatureCard(
                title = "Multi-Modal Learning",
                description = "Learn from images, text, and sensor data",
                icon = Icons.Filled.PhotoCamera, // Explicit Filled
                onClick = {
                    navController.navigate(NavRoutes.MULTI_MODAL_LEARNING)
                }
            )

            FeatureCard(
                title = "Meta-Learning",
                description = "Learn how to learn better",
                icon = Icons.Filled.Psychology, // Explicit Filled
                onClick = {
                    navController.navigate(NavRoutes.META_LEARNING)
                }
            )

            FeatureCard(
                title = "Neural Network",
                description = "Visualize and train neural networks",
                icon = Icons.Filled.Memory, // Explicit Filled
                onClick = {
                    navController.navigate(NavRoutes.NEURAL_NETWORK)
                }
            )
