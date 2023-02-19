package com.hfad.guitarassistant.alorithm;


import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.SystemClock;
import static com.hfad.guitarassistant.GlobalConfig.RecorderConfig.*;

import android.util.Log;
/**
 * 负责录音，并返回频率结果
 */
public class Recorder {

    private boolean isRecording;
    private AudioRecord audioRecord;
    final private short[] data;
    final private int recordBufSize;
    final private Runnable handlerCallback;
    final private Handler handler= new Handler();
    final Thread calculateThread;
    private PitchDetectionResult resultTemp;
    final Object lock = new Object();

    public interface RecorderListener {
        void onUpdate(PitchDetectionResult result);
    }

    public Recorder(RecorderListener listener, int duration) {

        recordBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT) * 2;
        int dataBufLength = 8192;
        // 用于存储读出的数据
        data = new short[dataBufLength];


        handlerCallback = () -> listener.onUpdate(resultTemp);

        // 利用多线程能力
        calculateThread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    if (isRecording) {
                        try {
                            int read_count = audioRecord.read(data, 0, dataBufLength);
                            Yin detector = new Yin(SAMPLE_RATE_INHZ, read_count);
                            resultTemp = filter(detector.getPitch(data));

                            // 加锁同步
                            synchronized (lock) {
                                handler.post(handlerCallback);
                            }
                            if(resultTemp.getPitch() != -1) {
                                SystemClock.sleep(duration);
                            } else {
                                SystemClock.sleep(duration / 3);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        SystemClock.sleep(1000);
                    }
                }
            }
        };
    }


    /**
     * 开始录音，由上层检查录音权限
     */
    public void startRecord() throws SecurityException {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG,
                AUDIO_FORMAT, recordBufSize);
        audioRecord.startRecording();
        isRecording = true;

        if(calculateThread.getState() == Thread.State.NEW) {
            calculateThread.start();
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        isRecording = false;
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
    }

    /**
     * 过滤异常频率
     */
    public PitchDetectionResult filter(PitchDetectionResult result) {
        float rawFreq = result.getPitch();
        result.setPitch(rawFreq >= PITCH_LOWER_LIMIT && rawFreq <= PITCH_UPPER_LIMIT ? rawFreq : -1);
        return result;
    }
}
