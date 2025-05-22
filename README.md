# Sustainable Habits Android App

A modern Android application for tracking and building sustainable habits with advanced features including biometric integration, AR visualization, voice/NLP, neural networks, and more.

## Features

### Core Features
- Habit tracking and management
- Streak tracking
- Goal setting and progress visualization
- Reminders and notifications
- Statistics and insights

### Advanced Features
- **Biometric Integration**: Monitor heart rate, respiration, stress level, and energy using device sensors
- **AR Visualization**: View habits in augmented reality with 3D models
- **Voice/NLP**: Control the app with voice commands and natural language processing
- **Quantum-Inspired Visualization**: Visualize habits using quantum-inspired particle systems
- **Spatial Computing**: Place habits in 3D space for intuitive organization
- **Three.js Visualization**: Advanced 3D visualizations using Three.js
- **Anime.js Animation**: Smooth, modern animations using Anime.js
- **Multi-Modal Learning**: Learn from images, text, and sensor data
- **Meta-Learning**: Adapt to user habits over time
- **Neural Network**: Visualize and train neural networks for habit prediction

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt
- **Database**: Room
- **Concurrency**: Kotlin Coroutines and Flow
- **JavaScript Bridges**: WebView integration for Three.js and Anime.js

## JavaScript Bridge Setup

This project uses JavaScript bridges to integrate advanced visualization libraries like Three.js and Anime.js. Here's how to set up the JavaScript environment:

1. **Install Node.js dependencies**:
   ```bash
   npm install
   ```
   or if using Yarn:
   ```bash
   yarn install
   ```

2. **Build JavaScript bundles**:
   ```bash
   npm run build
   ```
   or with Yarn:
   ```bash
   yarn build
   ```

3. **JavaScript files location**:
   - The JavaScript source files are located in `app/src/main/assets/js/`
   - Built bundles are automatically placed in the correct assets directory

> **Note**: The `node_modules` directory is intentionally excluded from version control. Always run the install command after cloning the repository to ensure all dependencies are properly installed.

## Development Setup

1. Clone the repository
   ```bash
   git clone https://github.com/hooiv/sustainable-habits-android.git
   ```

2. Open the project in Android Studio

3. Install JavaScript dependencies (if working with JS features)
   ```bash
   npm install
   ```

4. Build and run the app

## Building the App

To build the debug version of the app:
```bash
./gradlew assembleDebug
```

To build the release version:
```bash
./gradlew assembleRelease
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

