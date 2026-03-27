package com.neotuner.app;

public class PitchDetector {
    private static final int SAMPLE_RATE = 44100;
    private static final int FFT_SIZE = 4096;
    private static final double MIN_FREQUENCY = 27.5;
    private static final double MAX_FREQUENCY = 4186.0;
    private double[] window;
    private double lastFrequency = -1;
    private static final double MAX_FREQUENCY_CHANGE = 0.3;

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

        int minIndex = (int) Math.ceil(MIN_FREQUENCY * FFT_SIZE / SAMPLE_RATE);
        int maxIndex = (int) Math.floor(MAX_FREQUENCY * FFT_SIZE / SAMPLE_RATE);
        minIndex = Math.max(minIndex, 10);
        maxIndex = Math.min(maxIndex, FFT_SIZE / 2 - 1);

        class Peak {
            int index;
            double magnitude;
            double frequency;
            Peak(int index, double magnitude) {
                this.index = index;
                this.magnitude = magnitude;
                this.frequency = index * SAMPLE_RATE / (double)FFT_SIZE;
            }
        }

        java.util.List<Peak> peaks = new java.util.ArrayList<>();
        for (int i = minIndex; i <= maxIndex; i++) {
            if (magnitude[i] > magnitude[i-1] && magnitude[i] > magnitude[i+1]) {
                peaks.add(new Peak(i, magnitude[i]));
            }
        }

        if (peaks.isEmpty()) {
            return -1;
        }

        peaks.sort((a, b) -> Double.compare(b.magnitude, a.magnitude));
        int topN = Math.min(7, peaks.size());
        java.util.List<Peak> topPeaks = new java.util.ArrayList<>(peaks.subList(0, topN));
        topPeaks.sort((a, b) -> Integer.compare(a.index, b.index));

        Peak bestPeak = null;
        double bestScore = -1;

        for (Peak peak : topPeaks) {
            double score = 0;
            
            score += 1200.0 / (peak.index + 10);
            score += peak.magnitude * 100;
            
            double freq = peak.frequency;
            for (Peak lowerPeak : topPeaks) {
                if (lowerPeak.index < peak.index) {
                    double ratio = freq / lowerPeak.frequency;
                    int nearestHarmonic = (int)Math.round(ratio);
                    if (nearestHarmonic >= 2 && nearestHarmonic <= 6) {
                        double harmonicRatio = ratio / nearestHarmonic;
                        if (harmonicRatio > 0.97 && harmonicRatio < 1.03) {
                            score -= 150;
                            break;
                        }
                    }
                }
            }
            
            if (lastFrequency > 0) {
                double ratio = Math.abs(freq - lastFrequency) / lastFrequency;
                if (ratio < 0.2) {
                    score += 400;
                } else if (ratio < 0.35) {
                    score += 100;
                }
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestPeak = peak;
            }
        }

        if (bestPeak == null || bestPeak.magnitude < 0.01) {
            return -1;
        }

        int peakIndex = bestPeak.index;
        double interpolatedIndex = peakIndex;
        if (peakIndex > 0 && peakIndex < FFT_SIZE / 2 - 1) {
            double y1 = magnitude[peakIndex - 1];
            double y2 = magnitude[peakIndex];
            double y3 = magnitude[peakIndex + 1];
            interpolatedIndex = peakIndex + (y3 - y1) / (2 * (2 * y2 - y1 - y3));
        }

        double frequency = interpolatedIndex * SAMPLE_RATE / FFT_SIZE;

        if (lastFrequency > 0) {
            double ratio = Math.abs(frequency - lastFrequency) / lastFrequency;
            if (ratio > 0.25) {
                frequency = lastFrequency + (frequency - lastFrequency) * 0.35;
            }
        }

        lastFrequency = frequency;
        return frequency;
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
