package com.hfad.guitarassistant.alorithm;


import android.util.Log;

import com.hfad.guitarassistant.alorithm.fft.Fourier;
import com.hfad.guitarassistant.alorithm.fft.ComplexNumberArray;


public class MyAlgorithm extends PitchDetector {

    private static final float THRESHOLD = 50;

    private final int sampleRate;

    private final float threshold;

    private final float[] tempBuffer;

    private final PitchDetectionResult result;

    public PitchDetectionResult getPitch(float[] audioBuffer) {

        // step1:中心削波
        clearWave(audioBuffer);

        // step2:自相关
        autoCorrelation(audioBuffer);

        // 添加0值增加分辨率
        float[] lenTempBuffer = new float[tempBuffer.length * 32];
        System.arraycopy(tempBuffer, 0, lenTempBuffer, 0, tempBuffer.length);

        for(int i = tempBuffer.length - 1; i < lenTempBuffer.length; i++)
            lenTempBuffer[i] = 0;
        // step3:fft
//        Fourier fft = new Fourier(tempBuffer.length, sampleRate);
//        ComplexNumberArray output = fft.fft(tempBuffer);

        Fourier fft = new Fourier(lenTempBuffer.length, sampleRate);
        ComplexNumberArray output = fft.fft(lenTempBuffer);

        Fourier.Analyzer analyzer = new Fourier.Analyzer(fft);
        double pitch = analyzer.getFrequencyAtMaxAmplitude(output);
        Log.i("pitch", String.valueOf(pitch));
        result.setPitch((float) pitch);
        result.setPitched(true);
        return result;
    }

    public MyAlgorithm(final int audioSampleRate, final int bufferSize, final float threshold) {
        this.sampleRate = audioSampleRate;
        this.threshold = threshold;
        this.tempBuffer = new float[bufferSize / 2];
        result = new PitchDetectionResult();
    }

    public MyAlgorithm(final int audioSampleRate, final int bufferSize) {
        this(audioSampleRate, bufferSize, THRESHOLD);
    }

    // 中心削波
    private void clearWave(float[] audioBuffer) {
        for (int i = 0; i < audioBuffer.length; i++) {
            float value = audioBuffer[i];
            if (value < -threshold) {
                audioBuffer[i] = -1;
            } else if (value > threshold) {
                audioBuffer[i] = 1;
            } else {
                audioBuffer[i] = 0;
            }
        }
    }

    // 自相关运算
    private void autoCorrelation(float[] audioBuffer) {
        for (int tau = 0; tau < tempBuffer.length; tau++) {
            float sum = 0;
            for (int i = 0; i < tempBuffer.length; i++) {
                sum += audioBuffer[i] * audioBuffer[i + tau];
            }
            tempBuffer[tau] = sum;
        }
    }
}


class Complex {
    public double i;
    public double j;// 虚数
    public Complex(double i, double j) {
        this.i = i;
        this.j = j;
    }

    public double getMod() {// 求复数的模
        return Math.sqrt(i * i + j * j);
    }

    public static Complex Add(Complex a, Complex b) {
        return new Complex(a.i + b.i, a.j + b.j);
    }

    public static Complex Subtract(Complex a, Complex b) {
        return new Complex(a.i - b.i, a.j - b.j);
    }

    public static Complex Mul(Complex a, Complex b) {// 乘法
        return new Complex(a.i * b.i - a.j * b.j, a.i * b.j + a.j * b.i);
    }

    public static Complex GetW(int k, int N) {
        return new Complex(Math.cos(-2 * Math.PI * k / N), Math.sin(-2 * Math.PI * k / N));
    }

    public static Complex[] butterfly(Complex a, Complex b, Complex w) {
        return new Complex[] { Add(a, Mul(w, b)), Subtract(a, Mul(w, b)) };
    }

    public static Double[] toModArray(Complex[] complex) {
        Double[] res = new Double[complex.length];
        for (int i = 0; i < complex.length; i++) {
            res[i] = complex[i].getMod();
        }
        return res;
    }
}


class FFT {
    public static Complex[] getFFT(Complex[] input, int N) {
        if ((N / 2) % 2 == 0) {
            Complex[] even = new Complex[N / 2];// 偶数
            Complex[] odd = new Complex[N / 2];// 奇数
            for (int i = 0; i < N / 2; i++) {
                even[i] = input[2 * i];
                odd[i] = input[2 * i + 1];
            }
            even = getFFT(even, N / 2);
            odd = getFFT(odd, N / 2);
            for (int i = 0; i < N / 2; i++) {
                Complex[] res = Complex.butterfly(even[i], odd[i], Complex.GetW(i, N));
                input[i] = res[0];
                input[i + N / 2] = res[1];
            }
            return input;
        } else {// 两点DFT,直接进行碟形运算
            Complex[] res = Complex.butterfly(input[0], input[1], Complex.GetW(0, N));
            return res;
        }
    }
}
