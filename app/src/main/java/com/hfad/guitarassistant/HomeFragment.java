package com.hfad.guitarassistant;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private List<ImageView> images;
    private List<TextView> texts;
    private List<Drawable[]> imageResources;
    private CallbackListener listener;
    View layout;
    int clickedId = -1;    // 当前被点击的选项号

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (CallbackListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.fragment_home, container, false);
        // 为每个选项设置点击监听器
        layout.findViewById(R.id.item_1).setOnClickListener(this);
        layout.findViewById(R.id.item_2).setOnClickListener(this);
        layout.findViewById(R.id.item_3).setOnClickListener(this);
        initData();
        return layout;
    }

    private void initData() {
        // 列表里的view和image的顺序必须一一对应
        images = new ArrayList<>();
        texts = new ArrayList<>();
        imageResources = new ArrayList<>();
        // 图片view
        images.add(layout.findViewById(R.id.image_1));
        images.add(layout.findViewById(R.id.image_2));
        images.add(layout.findViewById(R.id.image_3));
        // 文字
        texts.add(layout.findViewById(R.id.text_1));
        texts.add(layout.findViewById(R.id.text_2));
        texts.add(layout.findViewById(R.id.text_3));
        // 图片源
        imageResources.add(new Drawable[] {
            ResourcesCompat.getDrawable(getResources(), R.drawable.metronome_1, null),
            ResourcesCompat.getDrawable(getResources(), R.drawable.metronome_2, null)
        });
        imageResources.add(new Drawable[] {
            ResourcesCompat.getDrawable(getResources(), R.drawable.tuner_1, null),
            ResourcesCompat.getDrawable(getResources(), R.drawable.tuner_2, null)
        });
        imageResources.add(new Drawable[] {
            ResourcesCompat.getDrawable(getResources(), R.drawable.scale_1, null),
            ResourcesCompat.getDrawable(getResources(), R.drawable.scale_2, null)
        });
    }

    public void setItem(int position) {
        // 恢复选项的初始状态
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setImageDrawable(imageResources.get(i)[0]);
            texts.get(i).setTextColor(getResources().getColor(R.color.black));
        }
        // 设置position对应选项的状态
        images.get(position).setImageDrawable(imageResources.get(position)[1]);
        texts.get(position).setTextColor(getResources().getColor(R.color.selected));
    }

    // 设置对该fragment的点击监听器
    @Override
    public void onClick(View view) {
        int clicked = view.getId();
        if (clicked != clickedId) {
            switch (clicked) {
                case R.id.item_1:
                    setItem(0);
                    listener.onClick(0);
                    break;
                case R.id.item_2:
                    setItem(1);
                    listener.onClick(1);
                    break;
                case R.id.item_3:
                    setItem(2);
                    listener.onClick(2);
                    break;
            }
        }
    }

    public interface CallbackListener {
        void onClick(int position);
    }
}