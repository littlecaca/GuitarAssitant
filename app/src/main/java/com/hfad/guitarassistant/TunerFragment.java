package com.hfad.guitarassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.media.SoundPool;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.hfad.guitarassistant.alorithm.DialAdapter;
import com.hfad.guitarassistant.alorithm.PitchDetectionResult;
import com.hfad.guitarassistant.components.DialView;
import com.hfad.guitarassistant.alorithm.Recorder;
import com.hfad.guitarassistant.utils.Scale;

import static com.hfad.guitarassistant.GlobalConfig.TunerConfig.*;

import java.util.HashMap;
import java.util.Map;


public class TunerFragment extends Fragment implements Recorder.RecorderListener{

    ImageView controlButton;
    private int buttonState = 0;
    DialView dial;
    View layout;
    private boolean isFreeMode = true;
    private Scale curScale;             // 用于与dialAdapter发送消息
    final private Map<ImageView, WireInfo> alphaInfos = new HashMap<>();
    final private DialAdapter dialAdapter = new DialAdapter();
    Recorder recorder = new Recorder(this, UPDATE_DURATION);
    SwitchCompat modeToggle;
    Context context;
    SoundPool tipSound;
    int soundId;

    // 录音器的回调函数
    public void onUpdate(PitchDetectionResult result) {
        Log.w("listener", "listening" + result.getPitch());

        if(result.getPitch() != -1) {
            DialAdapter.Result outValueResult;
            Log.e("listener", "start adapt!");
            if(isFreeMode) {
                outValueResult = dialAdapter.getResult(result.getPitch());
                lightLabelByScale(outValueResult.getScale());
            } else {
                outValueResult = dialAdapter.getResult(result.getPitch(), curScale);
            }
            Log.e("outValueResult", outValueResult.getScale().scaleText + outValueResult.getAngle());
            dial.setText(result.getPitch() + "", outValueResult.getScale().scaleText);
            int angle = outValueResult.getAngle();
            if(angle <= 6 && angle >= -6) {
                new Thread(() -> {
                    try {
                        recorder.stopRecord();
                        tipSound.play(soundId, 1, 1, 100, 0, 1 );
                        SystemClock.sleep(1000);
                        recorder.startRecord();
                    } catch (NullPointerException ignored) {

                    }
                }).start();
            }
            dial.rotatePointer(outValueResult.getAngle());
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 获取所需的reference
        layout = inflater.inflate(R.layout.fragment_tuner, container, false);
        controlButton = layout.findViewById(R.id.control_button);
        Drawable e = ResourcesCompat.getDrawable(getResources(), R.drawable.e, null),
        a = ResourcesCompat.getDrawable(getResources(), R.drawable.a, null),
        d = ResourcesCompat.getDrawable(getResources(), R.drawable.d, null),
        g = ResourcesCompat.getDrawable(getResources(), R.drawable.g, null),
        b = ResourcesCompat.getDrawable(getResources(), R.drawable.b, null),
        e_s = ResourcesCompat.getDrawable(getResources(), R.drawable.e_s, null),
        a_s = ResourcesCompat.getDrawable(getResources(), R.drawable.a_s, null),
        d_s = ResourcesCompat.getDrawable(getResources(), R.drawable.d_s, null),
        g_s = ResourcesCompat.getDrawable(getResources(), R.drawable.g_s, null),
        b_s = ResourcesCompat.getDrawable(getResources(), R.drawable.b_s, null);
        alphaInfos.put(layout.findViewById(R.id.e), new WireInfo(
                e, e_s, "E", 0, layout.findViewById(R.id.round_1)
        ));
        alphaInfos.put(layout.findViewById(R.id.a), new WireInfo(
                a, a_s, "A", 0,layout.findViewById(R.id.round_2)
        ));
        alphaInfos.put(layout.findViewById(R.id.d), new WireInfo(
                d, d_s, "D", 0,layout.findViewById(R.id.round_3)
        ));
        alphaInfos.put(layout.findViewById(R.id.g), new WireInfo(
                g, g_s, "G", 1,layout.findViewById(R.id.round_4)
        ));
        alphaInfos.put(layout.findViewById(R.id.b), new WireInfo(
                b, b_s, "B", 1,layout.findViewById(R.id.round_5)
        ));
        alphaInfos.put(layout.findViewById(R.id.e_high), new WireInfo(
                e, e_s, "E", 2,layout.findViewById(R.id.round_6)
        ));

        dial = layout.findViewById(R.id.dial);
        modeToggle = layout.findViewById(R.id.mode_switch);
        modeToggle.setEnabled(false);
        setAlphaListener();
        setButtonListener();
        return layout;
    }

    private void setButtonListener() {
        controlButton.setOnClickListener(
                view -> {
                    if(buttonState == 0) {
                        start();
                    } else {
                        stop();
                        darkAll();
                    }
                }
        );
        modeToggle.setOnCheckedChangeListener(
                (compoundButton, b) -> {
                    if(b) {
                        setHelpMode();
                    } else {
                        setFreeMode();
                    }
                }
        );
    }

    // 开启录音
    private void start() {
        controlButton.setImageResource(R.drawable.pause);
        buttonState = 1;
        // 申请音效资源
        tipSound = new SoundPool.Builder().build();
        soundId = tipSound.load(context, R.raw.tip1, 100);
        dial.setTipSounder(tipSound, soundId);
        // 开启表盘，伸展指针
        dial.startPointer();
        recorder.startRecord();
    }

    // 关闭录音
    private void stop() {
        controlButton.setImageResource(R.drawable.start);
        buttonState = 0;
        modeToggle.setEnabled(false);
        modeToggle.setChecked(false);
        // 释放音效资源
        tipSound.release();
        tipSound = null;
        // 收回表盘指针
        recorder.stopRecord();
        dial.stopPointer();
    }

    // 设置字母点击监听者
    private void setAlphaListener() {
        View.OnClickListener listener = view -> {
            ImageView imageView = (ImageView)view;
            if(!isLight(imageView)) {
                // 如果用户直接点击字母，则直接开启录音
                if(buttonState == 0) {
                    start();
                }
                if(isFreeMode) {
                    modeToggle.setChecked(true);
                }
                darkAll();
                lightLabel(imageView);
                // 设置curScale
                curScale = getScale(imageView);
                dial.setText(null, getScale(imageView).scaleText);
            } else {
                darkLabel(imageView);
                if(isAllDark()) {
                    modeToggle.setChecked(false);
                }
            }
        };
        for(ImageView image : alphaInfos.keySet()) {
            image.setOnClickListener(listener);
        }
    }

    // 开启自由模式
    private void setFreeMode() {
        modeToggle.setEnabled(false);
        isFreeMode = true;
        dial.setMode(true);
        darkAll();
    }

    // 开启辅助模式
    private void setHelpMode() {
        isFreeMode = false;
        dial.setMode(false);
        darkAll();
        modeToggle.setEnabled(true);
    }

    // 判断字母是否已经全部熄灭
    private boolean isAllDark() {
        for(ImageView image : alphaInfos.keySet()) {
            if(isLight(image)) {
                return false;
            }
        }
        return true;
    }

    // 根据mute字符串点亮相应alpha
    private void lightLabelByScale(Scale scale) {
        for(ImageView alphaView : alphaInfos.keySet()) {
            Scale sc = getScale(alphaView);
            if(sc.scaleText.equals(scale.scaleText) &&
            sc.scaleRange == scale.scaleRange) {
                lightLabel(alphaView);
                break;
            }
        }
    }

    // 点亮字母及其round
    private void lightLabel(ImageView imageView) {
        marchRound(imageView).setImageResource(R.drawable.round_s);
        imageView.setImageDrawable(getLightAlpha(imageView));
    }

    // 熄灭字母及其round
    private void darkLabel(ImageView imageView) {
        marchRound(imageView).setImageResource(R.drawable.round);
        imageView.setImageDrawable(getDarkAlpha(imageView));
    }

    // 熄灭所有字母及其round
    private void darkAll() {
        for(ImageView image : alphaInfos.keySet()) {
            if(isLight(image)) {
                image.setImageDrawable(getDarkAlpha(image));
                marchRound(image).setImageResource(R.drawable.round);
            }
        }
    }

    // 判断字母是否已被点亮
    private boolean isLight(ImageView alphaView) {
        return alphaView.getDrawable() == getLightAlpha(alphaView);
    }

    // 根据imageView匹配对应的round
    private ImageView marchRound(ImageView imageView) {
        WireInfo wireInfo = alphaInfos.get(imageView);
        assert wireInfo != null;
        return wireInfo.roundImage;
    }

    // 获取点亮英文字母
    private Drawable getLightAlpha(ImageView alphaView) {
        WireInfo wireInfo = alphaInfos.get(alphaView);
        assert wireInfo != null;
        return wireInfo.lightImage;
    }

    // 获取黑暗英文字母
    private Drawable getDarkAlpha(ImageView alphaView) {
        WireInfo wireInfo = alphaInfos.get(alphaView);
        assert wireInfo != null;
        return wireInfo.darkImage;
    }

    // 获取音符音高文字形式
    private Scale getScale(ImageView alphaView) {
        WireInfo wireInfo = alphaInfos.get(alphaView);
        assert wireInfo != null;
        return wireInfo.scale;
    }

    // 用于存储六根琴弦的对应信息
    static private class WireInfo {
        final Drawable darkImage;
        final Drawable lightImage;
        final Scale scale = new Scale();
        final ImageView roundImage;


        WireInfo(Drawable darkImage, Drawable lightImage,
                 String scaleText, int scaleRange, ImageView roundImage) {
            this.darkImage = darkImage;
            this.lightImage = lightImage;
            this.scale.scaleText = scaleText;
            this.scale.scaleRange = scaleRange;
            this.roundImage = roundImage;
        }
    }
}