package com.hfad.guitarassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HomeFragment.CallbackListener {

    private static final int MY_REQUEST_CODE = 12;

    private List<Fragment> fragmentPages;
    private ViewPager pager;
    TextView barTitle;

    /**
     * 需要申请的权限列表
     */
    private final String[] permissions = new String[] {
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WAKE_LOCK
    };

    /**
     * 被用户拒绝的权限列表
     */
    private final List<String> missedPermissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 强制关闭夜间模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // 检查所需权限
        checkPermissions();
        // 初始化所需数据，例如fragments
        initData();
        // 构建viewpager
        pager = findViewById(R.id.pager);
        FragmentPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        // 获取底部导航栏碎片
        HomeFragment homeFragment =
            (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.home_frag);
        // 获取标题栏
        barTitle = findViewById(R.id.bar_title);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                assert homeFragment != null;
                homeFragment.setItem(position);
                barTitle.setText(getTitleByPosition(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        // 初始化显示中间tuner
        assert homeFragment != null;
        homeFragment.setItem(1);
        pager.setCurrentItem(1);
    }

    private void initData() {
        fragmentPages = new ArrayList<>();
        fragmentPages.add(new MetronomeFragment());
        fragmentPages.add(new TunerFragment());
        fragmentPages.add(new ScaleFragment());
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return fragmentPages.size();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentPages.get(position);
        }
    }

    // 根据page的position获取标题
    private int getTitleByPosition(int position) {
        switch (position) {
            case 0:
                return R.string.desc_metronome;
            case 1:
                return R.string.desc_tuner;
            case 2:
                return R.string.desc_scale;
        }
        return -1;
    }

    // 设置CallbackListener
    @Override
    public void onClick(int position) {
        pager.setCurrentItem(position);
        barTitle.setText(getTitleByPosition(position));
    }

    /**
     * 对权限请求结果做处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        // 仅仅报告，不作其它处理
        if (requestCode == MY_REQUEST_CODE) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Permission Denied", "权限获取失败！");
                }
            }
        }
    }

    /**
     * 检查并获取权限
     */
    private void checkPermissions() {
        for (String per : permissions) {
            if (ActivityCompat.checkSelfPermission(this, per)
                != PackageManager.PERMISSION_GRANTED) {
                missedPermissions.add(per);
            }
        }

        if (!missedPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, missedPermissions.toArray(new String[0]),
                MY_REQUEST_CODE);
        }
    }
}
