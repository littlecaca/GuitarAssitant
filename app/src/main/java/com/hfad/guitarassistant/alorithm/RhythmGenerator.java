package com.hfad.guitarassistant.alorithm;
import android.media.SoundPool;
import android.os.SystemClock;
import android.content.Context;
import com.hfad.guitarassistant.R;


public class RhythmGenerator {
    int duration;
    RhythmListener listener;
    final Thread generateThread;
    boolean state;
    SoundPool knockPlayer;
    Context context;
    int soundId;


    public RhythmGenerator(RhythmListener listener, Context context) {
        this.listener = listener;

        this.context = context;
        duration = rhythmToMillsec(90);

        generateThread = new Thread() {
            @Override
            public void run() {
                while(true) {
                    if(state) {
                        try {
                            knockPlayer.play(soundId, 1, 1, 100, 0, 1 );
                        } catch (NullPointerException ignored) {

                        }
                        listener.onTwinkle();
                        SystemClock.sleep(duration);
                    } else {
                        SystemClock.sleep(300);
                    }
                }
            }
        };
    }

    public void setRhythm(int rhythm) {
        this.duration = rhythmToMillsec(rhythm);
    }

    public void startGenerator() {
        state = true;
        if(generateThread.getState() == Thread.State.NEW) {
            generateThread.start();
        }
    }

    public void stopGenerator() {
        state = false;
    }

    public interface RhythmListener {
        void onTwinkle();
    }

    private int rhythmToMillsec(int rhythm) {
        return (int) (60. / rhythm * 1000);
    }

    public void setListener(RhythmListener listener) {
        this.listener = listener;
    }

    // 获取音频数据
    public void prepare() {
        knockPlayer = new SoundPool.Builder().build();
        soundId = knockPlayer.load(context, R.raw.knock1, 100);
    }

    // 释放资源
    public void release() {
        knockPlayer.release();
        knockPlayer = null;
    }
}
