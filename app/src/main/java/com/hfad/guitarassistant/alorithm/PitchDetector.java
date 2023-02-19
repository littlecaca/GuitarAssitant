package com.hfad.guitarassistant.alorithm;


public abstract class PitchDetector {
    abstract public PitchDetectionResult getPitch(final float[] audioBuffer);
    public PitchDetectionResult getPitch(final short[] audioBuffer) {
        float[] targeted_buffer = new float[audioBuffer.length];
        for(int i = 0; i < audioBuffer.length; i++) {
            targeted_buffer[i] = audioBuffer[i];
        }
        return getPitch(targeted_buffer);
    }
}
