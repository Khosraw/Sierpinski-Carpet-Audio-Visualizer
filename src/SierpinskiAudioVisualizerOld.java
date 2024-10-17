import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SierpinskiAudioVisualizerOld extends JPanel {

    private static final int BUFFER_SIZE = 16392;
    private final long transitionDuration = 0; // Duration of transition in milliseconds
    // Audio delay variable
    private final long audioDelay = 0; // Delay in milliseconds before starting audio playback
    private double smoothedVolumeLevel = 0.0;
    // Variables for smooth transitions
    private BufferedImage currentFractalImage;
    private BufferedImage nextFractalImage;
    private volatile int currentDepth = 0;
    private volatile int targetDepth = 1;
    private volatile double transitionProgress = 1.0; // 1.0 means transition is complete
    private long lastDepthChangeTime = 0;
    private static final Color[] squareColors = {Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.PINK};

    public SierpinskiAudioVisualizerOld() {
        // Start a timer to control the frame rate
        Timer drawingTimer = new Timer(5, e -> {
            repaint();
        });
        drawingTimer.start();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Please provide a WAV file.");
            System.exit(1);
        }

        String filename = args[0];
        SierpinskiAudioVisualizerOld visualizer = new SierpinskiAudioVisualizerOld();

        JFrame frame = new JFrame("Sierpinski's Carpet Audio Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(visualizer);
        frame.setSize(1100, 1100);
        frame.setVisible(true);

        new Thread(() -> {
            try {
                visualizer.processAudio(filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        long currentTime = System.currentTimeMillis();
        if (transitionProgress < 1.0) {
            // Update transition progress
            long elapsedTime = currentTime - lastDepthChangeTime;
            transitionProgress = (double) elapsedTime / transitionDuration;
            if (transitionProgress >= 1.0) {
                transitionProgress = 1.0;
                // Transition complete
                currentFractalImage = nextFractalImage;
                currentDepth = targetDepth;
            }
        }

        if (currentFractalImage == null) {
            // Generate the initial fractal image
            currentFractalImage = generateFractalImage(currentDepth);
        }

        Graphics2D g2d = (Graphics2D) g;
        setBackground(Color.BLACK);

        if (transitionProgress < 1.0 && nextFractalImage != null) {
            // Blend the current and next images
            g2d.setComposite(AlphaComposite.SrcOver.derive(1.0f));
            g2d.drawImage(currentFractalImage, 0, 0, null);
            g2d.setComposite(AlphaComposite.SrcOver.derive((float) transitionProgress));
            g2d.drawImage(nextFractalImage, 0, 0, null);
        } else {
            // Draw the current image
            g2d.drawImage(currentFractalImage, 0, 0, null);
        }
    }

    private BufferedImage generateFractalImage(int depth) {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        Color randomColor = new Color((int) (Math.random() * 0x1000000));
        g2d.setColor(randomColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        drawCarpet(g2d, 0, 0, getWidth(), depth, depth);
        g2d.dispose();
        return image;
    }

    private void drawCarpet(Graphics g, int x, int y, int size, int depth, int maxDepth) {
        if (depth == 0) {
            return;
        }
        // Set color based on depth
        g.setColor(getColorForDepth());

        // Draw the square
        g.fillRect(x, y, size, size);

        // Erase the center square
        int newSize = size / 3;
        int normalizedDepthZeroToColorLength = (maxDepth - depth) % squareColors.length;
        g.setColor(squareColors[normalizedDepthZeroToColorLength]);
        g.fillRect(x + newSize, y + newSize, newSize, newSize);

        // Recurse for the 8 surrounding squares
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 1 && j == 1) {
                    // Skip the center square
                    continue;
                }
                drawCarpet(g, x + i * newSize, y + j * newSize, newSize, depth - 1, maxDepth);
            }
        }
    }

    private Color getColorForDepth() {
        // Random color based on 255 RGB values
        int r = (int) (Math.random() * 255);
        int g = (int) (Math.random() * 255);
        int b = (int) (Math.random() * 255);
        return new Color(r, g, b);
    }

    public void processAudio(String filename) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

        AudioFormat format = audioStream.getFormat();

        // Calculate the number of bytes corresponding to the audio delay
        int frameSize = format.getFrameSize();
        float frameRate = format.getFrameRate();
        int bytesPerSecond = (int) (frameSize * frameRate);
        int bytesToDelay = (int) ((audioDelay / 1000.0) * bytesPerSecond);

        // Buffer to store audio data for delayed playback
        ByteArrayOutputStream audioBufferStream = new ByteArrayOutputStream();

        // Create a SourceDataLine for audio playback
        SourceDataLine audioLine = AudioSystem.getSourceDataLine(format);
        audioLine.open(format);
        audioLine.start();

        byte[] bytesBuffer = new byte[BUFFER_SIZE];
        int bytesRead;

        double alpha = 0.1; // Smoothing factor for volume level

        long startTime = System.currentTimeMillis();
        boolean playbackStarted = false;

        while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
            // Process audio data for visualization
            double[] samples = bytesToSamples(bytesBuffer, bytesRead, format);

            // Perform FFT
            double[] magnitudes = computeFFT(samples);

            // Calculate volume in desired frequency band (e.g., 630Hz to 1300Hz)
            double volume = getVolumeInBand(magnitudes, format.getSampleRate(), 60, 130);

            // Smooth the volume level to prevent abrupt changes
            smoothedVolumeLevel = alpha * volume + (1 - alpha) * smoothedVolumeLevel;

            // Map volume to depth
            int depth = mapVolumeToDepth(smoothedVolumeLevel);

            // If the depth has changed, start a transition
            if (depth != targetDepth) {
                targetDepth = depth;

                SwingUtilities.invokeLater(() -> {
                    lastDepthChangeTime = System.currentTimeMillis();
                    transitionProgress = 0.0;

                    // Generate the next fractal image
                    nextFractalImage = generateFractalImage(targetDepth);
                });
            }

            // Debugging output (optional)
            System.out.println("Volume: " + smoothedVolumeLevel + ", Depth: " + depth);

            // Buffer the audio data for delayed playback
            audioBufferStream.write(bytesBuffer, 0, bytesRead);

            // Check if the delay has passed to start playback
            if (!playbackStarted && System.currentTimeMillis() - startTime >= audioDelay) {
                playbackStarted = true;

                // Write the buffered audio data to the audio line
                byte[] bufferedAudio = audioBufferStream.toByteArray();
                audioLine.write(bufferedAudio, 0, bufferedAudio.length);

                // Clear the buffer
                audioBufferStream.reset();
            }

            // If playback has started, write directly to the audio line
            if (playbackStarted) {
                audioLine.write(bytesBuffer, 0, bytesRead);
            }
        }

        audioLine.drain();
        audioLine.close();
        audioStream.close();
    }

    private double[] bytesToSamples(byte[] buffer, int bytesRead, AudioFormat format) {
        int bytesPerSample = format.getSampleSizeInBits() / 8;
        int numChannels = format.getChannels();
        boolean isBigEndian = format.isBigEndian();
        boolean isSigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);

        int numSamples = bytesRead / (bytesPerSample * numChannels);
        double[] samples = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            int sampleStart = i * bytesPerSample * numChannels;
            int sample = 0;

            for (int channel = 0; channel < numChannels; channel++) {
                int channelStart = sampleStart + channel * bytesPerSample;
                int value = 0;

                if (bytesPerSample == 2) {
                    if (isBigEndian) {
                        value = ((buffer[channelStart] << 8) | (buffer[channelStart + 1] & 0xFF));
                    } else {
                        value = ((buffer[channelStart + 1] << 8) | (buffer[channelStart] & 0xFF));
                    }
                    if (!isSigned) {
                        value -= 32768;
                    }
                } else if (bytesPerSample == 1) {
                    value = buffer[channelStart];
                    if (!isSigned) {
                        value -= 128;
                    }
                }

                sample += value;
            }

            // Average across channels
            sample /= numChannels;

            // Normalize sample to range [-1.0, 1.0]
            samples[i] = sample / 32768.0;
        }

        return samples;
    }

    private double[] computeFFT(double[] samples) {
        int n = samples.length;
        int m = 1;
        while (m < n) {
            m <<= 1;
        }

        double[] real = new double[m];
        double[] imag = new double[m];

        System.arraycopy(samples, 0, real, 0, n);

        fft(real, imag);

        double[] magnitudes = new double[m / 2];
        for (int i = 0; i < m / 2; i++) {
            magnitudes[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }

        return magnitudes;
    }

    private void fft(double[] real, double[] imag) {
        int n = real.length;
        int bits = (int) (Math.log(n) / Math.log(2));

        // Bit-reversal permutation
        for (int j = 1; j < n / 2; j++) {
            int swapPos = bitReverse(j, bits);
            if (swapPos > j) {
                double temp = real[j];
                real[j] = real[swapPos];
                real[swapPos] = temp;
                temp = imag[j];
                imag[j] = imag[swapPos];
                imag[swapPos] = temp;
            }
        }

        // Cooley-Tukey FFT
        for (int size = 2; size <= n; size *= 2) {
            double angle = -2 * Math.PI / size;
            double wRealInit = Math.cos(angle);
            double wImagInit = Math.sin(angle);

            for (int m = 0; m < n; m += size) {
                double wReal = 1;
                double wImag = 0;

                for (int k = 0; k < size / 2; k++) {
                    int i = m + k;
                    int j = i + size / 2;

                    double tempReal = wReal * real[j] - wImag * imag[j];
                    double tempImag = wReal * imag[j] + wImag * real[j];

                    real[j] = real[i] - tempReal;
                    imag[j] = imag[i] - tempImag;

                    real[i] += tempReal;
                    imag[i] += tempImag;

                    double tempWReal = wReal * wRealInit - wImag * wImagInit;
                    wImag = wReal * wImagInit + wImag * wRealInit;
                    wReal = tempWReal;
                }
            }
        }
    }

    private int bitReverse(int n, int bits) {
        int reversedN = 0;
        for (int i = 0; i < bits; i++) {
            reversedN = (reversedN << 1) | (n & 1);
            n >>= 1;
        }
        return reversedN;
    }

    private double getVolumeInBand(double[] magnitudes, float sampleRate, double freqLow, double freqHigh) {
        int n = magnitudes.length * 2; // Total number of FFT points
        double freqResolution = sampleRate / n;
        int lowIndex = (int) (freqLow / freqResolution);
        int highIndex = (int) (freqHigh / freqResolution);

        lowIndex = Math.max(0, lowIndex);
        highIndex = Math.min(magnitudes.length - 1, highIndex);

        double sumSquares = 0;
        for (int i = lowIndex; i <= highIndex; i++) {
            sumSquares += magnitudes[i] * magnitudes[i];
        }

        int count = highIndex - lowIndex + 1;
        double rms = Math.sqrt(sumSquares / count);

        return rms;
    }

    private int mapVolumeToDepth(double volume) {
        int minDepth = 0;
        int maxDepth = 6;

        // Adjust minVolume and maxVolume based on observed volume levels
        double minVolume = 150.0;
        double maxVolume = 230.0; // You might need to adjust this value

        // Map volume to depth using a logarithmic scale for smoother transitions
        double scaledVolume = Math.log10(Math.max(volume - minVolume + 1, 1)); // Add 1 to avoid log(0)
        double scaledMaxVolume = Math.log10(maxVolume - minVolume + 1);
        int depth = minDepth + (int) ((scaledVolume / scaledMaxVolume) * (maxDepth - minDepth));

        return Math.max(minDepth, Math.min(maxDepth, depth));
    }
}