package com.hfad.guitarassistant.components;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hfad.guitarassistant.R;
import com.hfad.guitarassistant.alorithm.RhythmGenerator;



public class TwinkleFragment extends Fragment implements RhythmGenerator.RhythmListener {

    private int blockNum = 4;
    LinearLayout layout;
    private int curIndex = 0;
    static private final int duration = 300;
    final Handler handler = new Handler();
    Drawable twinkleLight;
    Drawable twinkleDark;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = (LinearLayout) inflater.inflate(R.layout.fragment_twinkle, container, false);

        display();
        twinkleDark = ResourcesCompat.getDrawable(getResources(), R.drawable.twinkle_bg, null);
        twinkleLight = ResourcesCompat.getDrawable(getResources(), R.drawable.twinklle_light, null);
        return layout;
    }

    public TwinkleFragment() {
    }

    // 实现接口
    public void onTwinkle() {
        int index = curIndex;
        handler.post(
                () -> lightOne(index)
        );
        handler.postDelayed(
                () -> darkOne(index),
                duration
        );
        curIndex = (curIndex + 1) % blockNum;
    }

    // 调整block数量
    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
        display();
        reset();
    }

    public void reset() {
        curIndex = 0;
    }

    private void display() {
        layout.removeAllViews();
        for(int i = 0; i < blockNum; i++) {
            LayoutInflater.from(getContext()).inflate(R.layout.twinkle, layout);
        }
    }

    private void lightOne(int index) {
        try {
            layout.getChildAt(index).setBackground(twinkleLight);
        } catch (Exception ignored) {

        }

    }

    private void darkOne(int index) {
        try {
            layout.getChildAt(index).setBackground(twinkleDark);
        } catch (Exception ignored) {

        }
    }
}

