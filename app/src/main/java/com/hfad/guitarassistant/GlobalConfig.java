package com.hfad.guitarassistant;

import android.media.AudioFormat;

public class GlobalConfig {

    static public class RecorderConfig {
        /**
         * 采样频率，44.1khz
         */
        public static final int SAMPLE_RATE_INHZ = 22050;

        /**
         * 声道数，CHANNEL_IN_MONO单声道 CHANNEL_IN_STEREO立体声
         */
        public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;

        /**
         * 返回音频数据的格式
         */
        public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

        /**
         * 音高识别上限
         */
        public static final int PITCH_UPPER_LIMIT = 1200;

        /**
         * 音高识别下限
         */
        public static final int PITCH_LOWER_LIMIT = 40;
    }

    static public class TunerConfig {
        /**
         * 音频分析结果更新间隔时间，以毫秒为单位
         */
        public static final int UPDATE_DURATION = 500;
    }

}
