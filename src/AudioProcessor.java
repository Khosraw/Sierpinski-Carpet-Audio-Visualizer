import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class AudioProcessor {

    private final double[] smoothedVolumeLevels;
    private final double[] maxVolumes;
    private final double[] minVolumes;
    private final VisualizerPanel visualizerPanel;

    public AudioProcessor(VisualizerPanel visualizerPanel) {
        this.visualizerPanel = visualizerPanel;
        int numBands = Constants.getNumBands();
        smoothedVolumeLevels = new double[numBands];
        maxVolumes = new double[numBands];
        minVolumes = new double[numBands];
    }

    public void preprocessAudio(String filename) throws UnsupportedAudioFileException, IOException {
        File audioFile = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();

        int numBands = Constants.getNumBands();
        double[] bandFreqLow = Constants.getBandFreqLow();
        double[] bandFreqHigh = Constants.getBandFreqHigh();

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
        File audioFile = new File(filename);
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        AudioFormat format = audioStream.getFormat();

        SourceDataLine audioLine = AudioSystem.getSourceDataLine(format);
        audioLine.open(format);
        audioLine.start();

        int numBands = Constants.getNumBands();
        double[] bandFreqLow = Constants.getBandFreqLow();
        double[] bandFreqHigh = Constants.getBandFreqHigh();

        byte[] bytesBuffer = new byte[Constants.BUFFER_SIZE];
        int bytesRead;
        double alpha = 0.1;

        ExecutorService executor = Executors.newFixedThreadPool(numBands);

        while ((bytesRead = audioStream.read(bytesBuffer)) != -1) {
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

        executor.shutdown();
        audioLine.drain();
        audioLine.close();
        audioStream.close();
    }
}
