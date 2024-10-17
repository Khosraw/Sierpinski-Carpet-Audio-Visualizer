public class FFT {

    public static double[] computeFFT(double[] samples) {
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

    private static void fft(double[] real, double[] imag) {
        int n = real.length;
        int bits = (int) (Math.log(n) / Math.log(2));

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

    private static int bitReverse(int n, int bits) {
        int reversedN = 0;
        for (int i = 0; i < bits; i++) {
            reversedN = (reversedN << 1) | (n & 1);
            n >>= 1;
        }
        return reversedN;
    }
}
