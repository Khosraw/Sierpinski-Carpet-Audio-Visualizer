import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class AudioProcessor {

    private final double[] smoothedVolumeLevels = new double[Constants.NUM_BANDS];
    private final double[] maxVolumes = new double[Constants.NUM_BANDS];
    private final double[] minVolumes = new double[Constants.NUM_BANDS];
    private final VisualizerPanel visualizerPanel;

    public AudioProcessor(VisualizerPanel visualizerPanel) {
        this.visualizerPanel = visualizerPanel;
    }

    public void preprocessAudio(String filename) throws UnsupportedAudioFileException, IOException {
        File audioFile = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();

        List<Double>[] bandVolumes = new ArrayList[Constants.NUM_BANDS];
        for (int i = 0; i < Constants.NUM_BANDS; i++) {
            bandVolumes[i] = new ArrayList<>();
        }

        byte[] bytesBuffer = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
            double[] samples = AudioUtils.bytesToSamples(bytesBuffer, bytesRead, format);
            double[] magnitudes = FFT.computeFFT(samples);

            for (int i = 0; i < Constants.NUM_BANDS; i++) {
                double volume = AudioUtils.getVolumeInBand(magnitudes, format.getSampleRate(),
                        Constants.BAND_FREQ_LOW[i], Constants.BAND_FREQ_HIGH[i]);
                bandVolumes[i].add(volume);
            }
        }
        audioStream.close();

        for (int i = 0; i < Constants.NUM_BANDS; i++) {
            List<Double> volumes = bandVolumes[i];
            maxVolumes[i] = Collections.max(volumes);
            Collections.sort(volumes);
            int index = (int) (volumes.size() * 0.3);
            minVolumes[i] = volumes.get(index);
        }
    }

    public void processAudio(String filename) throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InterruptedException {
        File audioFile = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();

        SourceDataLine audioLine = AudioSystem.getSourceDataLine(format);
        audioLine.open(format);
        audioLine.start();

        byte[] bytesBuffer = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        double alpha = 0.1;

        ExecutorService executor = Executors.newFixedThreadPool(Constants.NUM_BANDS);

        while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
            double[] samples = AudioUtils.bytesToSamples(bytesBuffer, bytesRead, format);
            double[] magnitudes = FFT.computeFFT(samples);

            CountDownLatch latch = new CountDownLatch(Constants.NUM_BANDS);

            for (int i = 0; i < Constants.NUM_BANDS; i++) {
                final int bandIndex = i;
                executor.submit(() -> {
                    double volume = AudioUtils.getVolumeInBand(magnitudes, format.getSampleRate(),
                            Constants.BAND_FREQ_LOW[bandIndex],
                            Constants.BAND_FREQ_HIGH[bandIndex]);
                    smoothedVolumeLevels[bandIndex] = alpha * volume + (1 - alpha) *
                            smoothedVolumeLevels[bandIndex];
                    int depth = AudioUtils.mapVolumeToDepth(smoothedVolumeLevels[bandIndex],
                            minVolumes[bandIndex], maxVolumes[bandIndex]);

                    visualizerPanel.updateFractalImage(bandIndex, depth);

                    latch.countDown();
                });
            }

            latch.await();
            audioLine.write(bytesBuffer, 0, bytesRead);
        }

        executor.shutdown();
        audioLine.drain();
        audioLine.close();
        audioStream.close();
    }
}
