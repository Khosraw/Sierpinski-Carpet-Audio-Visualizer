public class Constants {
    public static final int BUFFER_SIZE = 4096;
    private static int numBands = 1;
    private static double[] bandFreqLow;
    private static double[] bandFreqHigh;

    // Original frequency ranges for 20 bands
    private static final double[] ORIGINAL_BAND_FREQ_LOW = {
        20, 50, 100, 150, 200, 250, 300, 350, 400, 500,
        600, 800, 1000, 2000, 3000, 4000, 5000, 7000,
        10000, 15000
    };
    private static final double[] ORIGINAL_BAND_FREQ_HIGH = {
        50, 100, 150, 200, 250, 300, 350, 400, 500, 600,
        800, 1000, 2000, 3000, 4000, 5000, 7000, 10000,
        15000, 20000
    };

    public static void setNumBands(int n) {
        numBands = n;
        // Initialize band frequencies based on selected number of bands
        bandFreqLow = new double[numBands];
        bandFreqHigh = new double[numBands];
        System.arraycopy(ORIGINAL_BAND_FREQ_LOW, 0, bandFreqLow, 0, numBands);
        System.arraycopy(ORIGINAL_BAND_FREQ_HIGH, 0, bandFreqHigh, 0, numBands);
    }

    public static int getNumBands() {
        return numBands;
    }

    public static double[] getBandFreqLow() {
        return bandFreqLow;
    }

    public static double[] getBandFreqHigh() {
        return bandFreqHigh;
    }
}
