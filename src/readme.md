# Sierpinski's Carpet Audio Visualizer

An interactive Java application that visualizes audio files using fractal images based on Sierpinski's Carpet. Users can select an audio file and customize the number of frequency bands (1-20) for the visualization.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Demo](#demo)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Clone the Repository](#clone-the-repository)
  - [Compile the Source Code](#compile-the-source-code)
- [Usage](#usage)
  - [Running the Application](#running-the-application)
  - [User Interface](#user-interface)
  - [Supported Audio Formats](#supported-audio-formats)
  - [Example](#example)
  - [Exiting the Application](#exiting-the-application)
- [Code Overview](#code-overview)
- [Troubleshooting](#troubleshooting)
  - [Common Issues](#common-issues)
  - [Solutions](#solutions)
- [Contributing](#contributing)
  - [Code Style](#code-style)
  - [Reporting Issues](#reporting-issues)
  - [Pull Requests](#pull-requests)
- [License](#license)
- [Author](#author)
- [Contact](#contact)

---

## Introduction

The **Sierpinski's Carpet Audio Visualizer** is a Java application that creates a visual representation of audio files using fractal images. Each frequency band is represented by a fractal whose depth changes according to the volume of that frequency in the audio file.

This project combines the mathematical beauty of fractals with audio processing techniques, providing an interactive and visually appealing way to experience audio files.

## Features

- **User-Friendly Interface**: Select audio files using your OS's file explorer.
- **Customizable Frequency Bands**: Choose the number of frequency bands (1-20) to visualize.
- **Real-Time Visualization**: Fractal images update in real-time as the audio plays.
- **Fractal Generation**: Uses Sierpinski's Carpet fractals to represent audio frequencies.
- **Multithreading**: Efficient processing using Java's concurrency utilities.

## Demo

*(Include screenshots or GIFs of the application in action here.)*

## Installation

### Prerequisites

Ensure you have the following installed on your system:

- **Java Development Kit (JDK) 8 or higher**: Required for compilation and execution.
  - Download from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) or use OpenJDK from your package manager.
- **Git** (optional): For cloning the repository.
  - Download from [Git](https://git-scm.com/downloads) or install via your package manager.

### Clone the Repository

Open a terminal and run:

```bash
git clone https://github.com/khosraw/Sierpinski-Carpet-Audio-Visualizer.git
cd Sierpinski-Carpet-Audio-Visualizer
```

Alternatively, download the ZIP file from GitHub and extract it:

1. Go to the repository page.
2. Click on the "Code" button.
3. Select "Download ZIP".
4. Extract the ZIP file to your desired location.

### Compile the Source Code

Compile all Java files in the source directory:

```bash
javac *.java
```

This will generate `.class` files for each Java source file.

---

## Usage

### Running the Application

After compilation, run the application using:

```bash
java SierpinskiAudioVisualizerApp
```

If you encounter any classpath issues, you can specify the current directory:

```bash
java -cp . SierpinskiAudioVisualizerApp
```

### User Interface

- **Choose Audio File**: Opens a file dialog to select a WAV audio file.
- **Number of Bands**: Use the spinner to select the number of frequency bands (1-20).
- **Start Visualization**: Begins processing the audio file and starts the visualization.

### Supported Audio Formats

- **WAV Files**: The application currently supports WAV audio files. Ensure your audio file is in WAV format.

### Example

1. **Select an Audio File**:

   - Click on "Choose Audio File".
   - Navigate to your desired WAV file and select it.
   - The file path will appear in the text field next to the button.

2. **Set Number of Bands**:

   - Use the spinner to select a number between 1 and 20.
   - This determines how many frequency bands will be visualized.

3. **Start Visualization**:

   - Click on "Start Visualization".
   - The application will preprocess the audio file, which may take a few moments depending on the file size.
   - The visualization window will display, showing fractal images corresponding to each frequency band.

4. **Enjoy the Visualization**:

   - Watch as the fractal images update in real-time with the audio playback.
   - Each fractal represents a frequency band, and its depth changes according to the audio volume in that band.

### Exiting the Application

- Close the window or terminate the application using your OS's standard method (e.g., Alt+F4 on Windows, Command+Q on macOS).

---

## Code Overview

The application is structured into several classes, each handling different aspects of the functionality.

### SierpinskiAudioVisualizerApp.java

**Description**: Entry point for the application. It sets up the GUI, handles user interactions, and starts the audio processing.

### VisualizerPanel.java

**Description**: Responsible for rendering the fractal images corresponding to each frequency band.

### AudioProcessor.java

**Description**: Handles audio processing tasks including reading the audio file, performing FFT, and updating the visualizer.

### AudioUtils.java

**Description**: Utility class for audio-related operations.

### FFT.java

**Description**: Performs Fast Fourier Transform on audio samples to obtain frequency magnitudes.

### FractalGenerator.java

**Description**: Generates fractal images based on the Sierpinski's Carpet algorithm.

### Constants.java

**Description**: Holds configuration constants and allows dynamic setting of the number of frequency bands.

---

## Troubleshooting

### Common Issues

- **Audio Playback Issues**:

  - The audio file does not play or the visualization does not respond.
  - Ensure your audio file is a valid WAV file and that your system supports audio playback.

- **Performance Lag**:

  - The application runs slowly, especially with a high number of bands.
  - Processing large audio files with many bands may cause performance issues.

- **Java Exceptions**:

  - Errors such as `UnsupportedAudioFileException`, `LineUnavailableException`, or `OutOfMemoryError` appear.
  - Check the console output for stack traces.

### Solutions

- **Unsupported Audio File**:

  - Convert your audio file to WAV format using an audio converter.
  - Ensure the file is not corrupted and is accessible.

- **Reduce Number of Bands**:

  - Try reducing the number of frequency bands to improve performance.

- **Increase Java Heap Size**:

  - If you encounter `OutOfMemoryError`, increase the Java heap size using the `-Xmx` option:

    ```bash
    java -Xmx2048m SierpinskiAudioVisualizerApp
    ```

- **Update Java Version**:

  - Ensure you are using an updated version of Java (JDK 8 or higher).

- **Check Audio Drivers**:

  - Ensure your system's audio drivers are up-to-date and functioning properly.

---

## Contributing

Contributions are welcome! Please follow these guidelines:

### Code Style

- **Java Conventions**: Follow standard Java coding conventions.
- **Naming**: Use meaningful variable and method names.
- **Comments**: Include comments and Javadoc where appropriate.
- **Formatting**: Ensure code is properly indented and formatted.

### Reporting Issues

- **Issue Tracker**: Use the GitHub issue tracker to report bugs or suggest enhancements.
- **Provide Details**: Include steps to reproduce the issue, expected behavior, and any relevant logs or screenshots.

### Pull Requests

1. **Fork the Repository**:

   - Click on the 'Fork' button to create your own copy.

2. **Clone the Fork**:

   ```bash
   git clone https://github.com/your-username/Sierpinski-Carpet-Audio-Visualizer.git
   cd Sierpinski-Carpet-Audio-Visualizer
   ```

3. **Create a Branch**:

   ```bash
   git checkout -b feature/YourFeature
   ```

4. **Make Changes**:

   - Implement your feature or fix.
   - Ensure your code builds and runs correctly.

5. **Commit Changes**:

   ```bash
   git commit -am 'Add new feature'
   ```

6. **Push to Branch**:

   ```bash
   git push origin feature/YourFeature
   ```

7. **Create a Pull Request**:

   - Go to the original repository on GitHub.
   - Click on "New Pull Request".
   - Select your branch and submit the PR.

**Note**: Ensure your branch is up-to-date with the main branch and that there are no merge conflicts.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Author

**Khosraw Azizi**

- **Email**: [khosraw.azizi@gmail.com](mailto:khosraw.azizi@gmail.com)
- **GitHub**: [github.com/khosraw](https://github.com/khosraw)
- **LinkedIn**: [linkedin.com/in/khosraw-azizi](https://linkedin.com/in/khosraw-azizi)

---

## Contact

For any inquiries, suggestions, or issues, please contact me via email or open an issue on GitHub.

---