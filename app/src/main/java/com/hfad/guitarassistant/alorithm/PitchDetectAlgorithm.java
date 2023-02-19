package com.hfad.guitarassistant.alorithm;

public class PitchDetectAlgorithm extends PitchDetector {
    // 测量结果
    PitchDetectionResult result;

    // 采样频率
    int sampleRate;

    // 缓存大小
    int bufferSize;

    public PitchDetectionResult getPitch(float[] audioBuffer) {
        result = new PitchDetectionResult();
        result.setPitch(0);
        return result;
    }

    // 构造函数
    public PitchDetectAlgorithm(int sampleRate, int bufferSize) {
        this.sampleRate = sampleRate;
        this.bufferSize = bufferSize;
    }

    // 自相关函数

}
