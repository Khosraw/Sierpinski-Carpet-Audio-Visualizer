import javax.sound.sampled.AudioFormat;

public class AudioUtils {

    public static double[] bytesToSamples(byte[] buffer, int bytesRead, AudioFormat format) {
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

            sample /= numChannels;
            samples[i] = sample / 32768.0;
        }

        return samples;
    }

    public static double getVolumeInBand(double[] magnitudes, float sampleRate, double freqLow,
                                         double freqHigh) {
        int n = magnitudes.length * 2;
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
        return Math.sqrt(sumSquares / count);
    }

    public static int mapVolumeToDepth(double volume, double minVolume, double maxVolume) {
        int minDepth = 0;
        int maxDepth = 8;

        double scaledVolume = (volume - minVolume) / (maxVolume - minVolume);
        int depth = minDepth + (int) (scaledVolume * (maxDepth - minDepth));

        return Math.max(minDepth, Math.min(maxDepth, depth));
    }
}
