package com.neotuner.app;

public class PitchDetector {
    private static final int SAMPLE_RATE = 44100;
    private static final int FFT_SIZE = 4096;
    private double[] window;

    public PitchDetector() {
        createHanningWindow();
    }

    private void createHanningWindow() {
        window = new double[FFT_SIZE];
        for (int i = 0; i < FFT_SIZE; i++) {
            window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (FFT_SIZE - 1)));
        }
    }

    public double detectPitch(short[] audioBuffer) {
        if (audioBuffer == null || audioBuffer.length < FFT_SIZE) {
            return -1;
        }

        double[] real = new double[FFT_SIZE];
        double[] imag = new double[FFT_SIZE];

        for (int i = 0; i < FFT_SIZE; i++) {
            real[i] = audioBuffer[i] * window[i] / 32768.0;
            imag[i] = 0;
        }

        fft(real, imag);

        double[] magnitude = new double[FFT_SIZE / 2];
        for (int i = 0; i < FFT_SIZE / 2; i++) {
            magnitude[i] = Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }

        int peakIndex = -1;
        double maxMagnitude = 0;
        for (int i = 10; i < FFT_SIZE / 2; i++) {
            if (magnitude[i] > maxMagnitude) {
                maxMagnitude = magnitude[i];
                peakIndex = i;
            }
        }

        if (peakIndex == -1 || maxMagnitude < 0.01) {
            return -1;
        }

        double interpolatedIndex = peakIndex;
        if (peakIndex > 0 && peakIndex < FFT_SIZE / 2 - 1) {
            double y1 = magnitude[peakIndex - 1];
            double y2 = magnitude[peakIndex];
            double y3 = magnitude[peakIndex + 1];
            interpolatedIndex = peakIndex + (y3 - y1) / (2 * (2 * y2 - y1 - y3));
        }

        return interpolatedIndex * SAMPLE_RATE / FFT_SIZE;
    }

    private void fft(double[] real, double[] imag) {
        int n = real.length;
        if (n <= 1) return;

        double[] evenReal = new double[n / 2];
        double[] evenImag = new double[n / 2];
        double[] oddReal = new double[n / 2];
        double[] oddImag = new double[n / 2];

        for (int i = 0; i < n / 2; i++) {
            evenReal[i] = real[2 * i];
            evenImag[i] = imag[2 * i];
            oddReal[i] = real[2 * i + 1];
            oddImag[i] = imag[2 * i + 1];
        }

        fft(evenReal, evenImag);
        fft(oddReal, oddImag);

        for (int k = 0; k < n / 2; k++) {
            double t = -2 * Math.PI * k / n;
            double cos = Math.cos(t);
            double sin = Math.sin(t);
            double re = oddReal[k] * cos - oddImag[k] * sin;
            double im = oddReal[k] * sin + oddImag[k] * cos;
            real[k] = evenReal[k] + re;
            imag[k] = evenImag[k] + im;
            real[k + n / 2] = evenReal[k] - re;
            imag[k + n / 2] = evenImag[k] - im;
        }
    }
}
