package com.hfad.guitarassistant.alorithm;

import android.util.Log;

import com.hfad.guitarassistant.utils.Scale;

/**
 * 表盘适配器
 * 该类的作用是把频率值转换为适合在表盘中显示的角度值
 * 除此之外，还要计算该频率对应的音的scale
 */
public class DialAdapter {

    static final private String[] scales = new String[]{
            "E", "F", "#F", "G", "#G", "A", "#A", "B", "C", "#C", "D", "#D"
    };

    static final private double RATE = Math.pow(2, 1./12);
    static final private double STANDARD = 110;  // 标准音la

    static final private int MAX_OUT_VALUE = 50;
    static final private int MIN_OUT_VALUE = -50;

    final private double[] pitches = new double[14];
    private double lowerThreshold;
    private double upperThreshold;

    private void initPitch() {
        double lowest = STANDARD / Math.pow(RATE, 6);
        for(int i = 0; i < 14; i++) {
            pitches[i] = lowest * Math.pow(RATE, i);
        }
        double e_high = pitches[pitches.length - 2];

        lowerThreshold = (lowest * RATE - lowest) / 2 + lowest;
        upperThreshold = (e_high * RATE - e_high) / 2 + e_high;
    }

    public DialAdapter() {
        initPitch();
    }

    /**
     * 用于辅助模式，该方法会返回相对于该音阶的角度值
     * @param freq 频率
     * @param scale 指定的音阶
     */
    public Result getResult(double freq, Scale scale) {
        int outValue;
        Result result = new Result();
        double frequency = standard(freq, result);
        int pitchIndex = findPitchByScale(scale) + 1;
        Log.e("getResult", result.scale.scaleRange + "==" + scale.scaleRange);
        if(result.scale.scaleRange == scale.scaleRange) {
            outValue = regulateOutValue(getOutValue(frequency, pitchIndex));
        } else if(result.scale.scaleRange < scale.scaleRange) {
            outValue = MIN_OUT_VALUE;
        } else {
            outValue = MAX_OUT_VALUE;
        }
        result.setAngle(outValue);
        return result;
    }

    /**
     * 用于自由模式，该方法会自动分析该频率最近的音阶
     * @param freq 频率
     */
    public Result getResult(double freq) {
        Result result = new Result();
        double frequency = standard(freq, result);
        // 这里-1， 是因为pitches与scales并不一一对应
        int pitchIndex = findPitchByFreq(frequency);
        result.angle = regulateOutValue(getOutValue(frequency, pitchIndex));
        result.scale.scaleText = scales[pitchIndex-1];
        return result;
    }

    // 将频率值转换在一个八度内，并记录其原本的scaleRange
    private double standard(double frequency, Result result) {
        double convertedFre = frequency;
        while(convertedFre<lowerThreshold || convertedFre>upperThreshold) {
            if(convertedFre < lowerThreshold) {
                result.scale.scaleRange -= 1;
                convertedFre *= 2;
            } else {
                result.scale.scaleRange += 1;
                convertedFre /= 2;
            }
        }
        return convertedFre;
    }

    // 计算偏移百分比，以整数形式输出
    private int getOutValue(double frequency, int pitchIndex) {
        // 找到frequency的在pitches中的两端的距离
        double distance;
        double result;
        if(frequency > pitches[pitchIndex]) {
            distance = pitches[pitchIndex + 1] - pitches[pitchIndex];
        } else {
            distance = pitches[pitchIndex] - pitches[pitchIndex - 1];
        }
        result =  (frequency - pitches[pitchIndex]) / distance / 2;
        return (int)(result * 100);
    }

    private int findPitchByFreq(double frequency) {
        double last = pitches[0];
        for(int i = 1; i < pitches.length; i++) {
            if(frequency > pitches[i]) {
                last = pitches[i];
            } else if(frequency < pitches[i]) {
                if(frequency - last > pitches[i] - frequency) {
                    return i;
                } else return i-1;
            } else {
                return i;
            }
        }
        return -1;
    }

    private int findPitchByScale(Scale scale) {
        for(int i = 0; i < scales.length; i++) {
            if(scale.scaleText.equals(scales[i])) {
                return i;
            }
        }
        return -1;
    }

    private int regulateOutValue(double rawValue) {
        if(rawValue > MAX_OUT_VALUE) {
            return MAX_OUT_VALUE;
        }
        if(rawValue < MIN_OUT_VALUE) {
            return MIN_OUT_VALUE;
        }
        return (int)rawValue;
    }

    static public class Result {
        private int angle;
        final private Scale scale = new Scale();

        private Result() { }

        public void setAngle(int angle) {
            this.angle = angle;
        }

        public int getAngle() {
            return angle;
        }

        public Scale getScale() {
            return scale;
        }
    }
}
