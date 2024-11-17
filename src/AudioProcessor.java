import javax.sound.sampled.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class AudioProcessor {

    private final double[] smoothedVolumeLevels;
    private final double[] maxVolumes;
    private final double[] minVolumes;
    private final VisualizerPanel visualizerPanel;
    private volatile boolean running = true;

    private final int numBands;
    private final double[] bandFreqLow;
    private final double[] bandFreqHigh;

    public AudioProcessor(VisualizerPanel visualizerPanel) {
        this.visualizerPanel = visualizerPanel;
        this.numBands = Constants.getNumBands();
        this.smoothedVolumeLevels = new double[numBands];
        this.maxVolumes = new double[numBands];
        this.minVolumes = new double[numBands];
        Arrays.fill(minVolumes, Double.MAX_VALUE);
        Arrays.fill(maxVolumes, Double.MIN_VALUE);
        this.bandFreqLow = Constants.getBandFreqLow();
        this.bandFreqHigh = Constants.getBandFreqHigh();
    }

    public void preprocessAudio(String filename) throws UnsupportedAudioFileException, IOException {
        File audioFile = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();

        List<Double>[] bandVolumes = new ArrayList[numBands];
        for (int i = 0; i < numBands; i++) {
            bandVolumes[i] = new ArrayList<>();
        }

        byte[] bytesBuffer = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
            double[] samples = AudioUtils.bytesToSamples(bytesBuffer, bytesRead, format);
            double[] magnitudes = FFT.computeFFT(samples);

            for (int i = 0; i < numBands; i++) {
                double volume = AudioUtils.getVolumeInBand(
                    magnitudes,
                    format.getSampleRate(),
                    bandFreqLow[i],
                    bandFreqHigh[i]
                );
                bandVolumes[i].add(volume);
            }
        }
        audioStream.close();

        for (int i = 0; i < numBands; i++) {
            List<Double> volumes = bandVolumes[i];
            maxVolumes[i] = Collections.max(volumes);
            Collections.sort(volumes);
            int index = (int) (volumes.size() * 0.3);
            minVolumes[i] = volumes.get(index);
        }
    }

    public void processAudio(String filename) throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InterruptedException {
        running = true; // Ensure running is true when starting
        File audioFile = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();

        SourceDataLine audioLine = AudioSystem.getSourceDataLine(format);
        audioLine.open(format);
        audioLine.start();

        byte[] bytesBuffer = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        double alpha = 0.1;

        ExecutorService executor = Executors.newFixedThreadPool(numBands);

        while (running && (bytesRead = audioStream.read(bytesBuffer)) != -1) {
            double[] samples = AudioUtils.bytesToSamples(bytesBuffer, bytesRead, format);
            double[] magnitudes = FFT.computeFFT(samples);

            CountDownLatch latch = new CountDownLatch(numBands);

            for (int i = 0; i < numBands; i++) {
                final int bandIndex = i;
                executor.submit(() -> {
                    double volume = AudioUtils.getVolumeInBand(
                        magnitudes,
                        format.getSampleRate(),
                        bandFreqLow[bandIndex],
                        bandFreqHigh[bandIndex]
                    );
                    smoothedVolumeLevels[bandIndex] = alpha * volume + (1 - alpha) *
                            smoothedVolumeLevels[bandIndex];
                    int depth = AudioUtils.mapVolumeToDepth(
                        smoothedVolumeLevels[bandIndex],
                        minVolumes[bandIndex],
                        maxVolumes[bandIndex]
                    );

                    visualizerPanel.updateFractalImage(bandIndex, depth);

                    latch.countDown();
                });
            }

            latch.await();
            audioLine.write(bytesBuffer, 0, bytesRead);
        }

        executor.shutdownNow();
        audioLine.drain();
        audioLine.close();
        audioStream.close();
    }

    public void processLiveAudio() throws LineUnavailableException, InterruptedException {
        running = true; // Ensure running is true when starting
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Line not supported");
            JOptionPane.showMessageDialog(null, "Live audio capture is not supported on this system.");
            return;
        }

        TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(info);
        targetLine.open(format);
        targetLine.start();

        byte[] bytesBuffer = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        double alpha = 0.1;

        ExecutorService executor = Executors.newFixedThreadPool(numBands);

        while (running) {
            bytesRead = targetLine.read(bytesBuffer, 0, bytesBuffer.length);

            double[] samples = AudioUtils.bytesToSamples(bytesBuffer, bytesRead, format);
            double[] magnitudes = FFT.computeFFT(samples);

            CountDownLatch latch = new CountDownLatch(numBands);

            for (int i = 0; i < numBands; i++) {
                final int bandIndex = i;
                executor.submit(() -> {
                    double volume = AudioUtils.getVolumeInBand(
                        magnitudes,
                        format.getSampleRate(),
                        bandFreqLow[bandIndex],
                        bandFreqHigh[bandIndex]
                    );

                    synchronized (this) {
                        if (volume < minVolumes[bandIndex]) minVolumes[bandIndex] = volume;
                        if (volume > maxVolumes[bandIndex]) maxVolumes[bandIndex] = volume;
                    }

                    smoothedVolumeLevels[bandIndex] = alpha * volume + (1 - alpha) * smoothedVolumeLevels[bandIndex];
                    int depth = AudioUtils.mapVolumeToDepth(
                        smoothedVolumeLevels[bandIndex],
                        minVolumes[bandIndex],
                        maxVolumes[bandIndex]
                    );

                    visualizerPanel.updateFractalImage(bandIndex, depth);

                    latch.countDown();
                });
            }

            latch.await();
        }

        executor.shutdownNow();
        targetLine.stop();
        targetLine.close();
    }

    public void stop() {
        running = false;
    }
}
