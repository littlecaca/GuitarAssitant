package com.hfad.guitarassistant;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatSeekBar;
import com.hfad.guitarassistant.components.TwinkleFragment;
import com.hfad.guitarassistant.alorithm.RhythmGenerator;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MetronomeFragment extends Fragment {
    View layout;
    ImageView control_button;
    Button add_button;
    Button minus_button;
    AppCompatSeekBar seekBar;
    Spinner spinner;
    RhythmGenerator rhythmGenerator;
    int curRhythm = 90;
    TwinkleFragment twinkleFragment;
    TextView showBoard;
    int curState = 0;
    Context context;

    static final int MIN_RHYTHM = 30;       // 最慢节奏
    static final int MAX_RHYTHM = 210;      // 最快节奏
    static final double RATE = (MAX_RHYTHM - MIN_RHYTHM) / 100.;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_metronome, container, false);

        control_button = layout.findViewById(R.id.control_button_metronome);
        minus_button = layout.findViewById(R.id.add);
        add_button = layout.findViewById(R.id.minus);
        seekBar = layout.findViewById(R.id.seek_bar);
        spinner = layout.findViewById(R.id.twinkle_spin);
        showBoard = layout.findViewById(R.id.twinkle_speed);
        twinkleFragment =
            (TwinkleFragment) getChildFragmentManager().findFragmentById(R.id.twinkle_frag);

        rhythmGenerator = new RhythmGenerator(twinkleFragment, context);
        setListener();
        return layout;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        rhythmGenerator.prepare();
    }

    @Override
    public void onStop() {
        super.onStop();
        stop();
        rhythmGenerator.release();
    }

    // 设置各个控件的监听器
    private void setListener() {
        control_button.setOnClickListener(
            view -> {
                if (curState == 0) {
                    start();
                } else {
                    stop();
                }
            }
        );

        seekBar.setOnSeekBarChangeListener(
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        curRhythm = progressToRhythm(i);
                        showBoard.setText(String.valueOf(curRhythm));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    adjustRhythm(curRhythm);
                }
            }
        );

        add_button.setOnClickListener(
            view -> {
                curRhythm += 1;
                adjustRhythm(curRhythm);
                showBoard.setText(String.valueOf(curRhythm));
                seekBar.setProgress(rhythmToProgress(curRhythm));
            }
        );

        minus_button.setOnClickListener(
            view -> {
                curRhythm -= 1;
                adjustRhythm(curRhythm);
                showBoard.setText(String.valueOf(curRhythm));
                seekBar.setProgress(rhythmToProgress(curRhythm));
            }
        );

        spinner.setOnItemSelectedListener(
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    twinkleFragment.setBlockNum(4 - i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            }
        );
    }

    // 开启节拍器
    private void start() {
        control_button.setImageResource(R.drawable.pause);
        rhythmGenerator.startGenerator();
        curState = 1;
    }

    // 关闭节拍器
    public void stop() {
        rhythmGenerator.stopGenerator();
        control_button.setImageResource(R.drawable.start);
        twinkleFragment.reset();
        curState = 0;
    }

    // 调整节奏
    private void adjustRhythm(int rhythm) {
        rhythmGenerator.setRhythm(rhythm);
    }

    // 把seekbar的进度转换为为节奏
    private static int progressToRhythm(int progress) {
        return (int) (progress * RATE + MIN_RHYTHM);
    }

    // 把节奏转换为进度
    private static int rhythmToProgress(int rhythm) {
        int raw = (int) ((rhythm - MIN_RHYTHM) / RATE);
        if (raw < 0) return 0;
        return Math.min(raw, 100);
    }
}