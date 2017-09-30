package com.liu.mytimer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.liu.mytimer.adapter.FragmentAdapter;
import com.liu.mytimer.fragment.Fragment2;
import com.liu.mytimer.fragment.TimerFragment;
import com.liu.mytimer.fragment.WorkRecordFragment;
import com.liu.mytimer.utils.Util;
import com.liu.mytimer.view.BelowMenuView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kunming.liu on 2017/9/28.
 */

public class FirstActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private BelowMenuView BelowMenuView;
    private float x, y ;
    private boolean isScrolling = false;
    private int direction = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        viewPager = (ViewPager)findViewById(R.id.viewPager);
        BelowMenuView = (BelowMenuView)findViewById(R.id.myView);
        List<Fragment> fragments=new ArrayList<Fragment>();
        fragments.add(WorkRecordFragment.getInstance());
        fragments.add(TimerFragment.getInstance());
        fragments.add(Fragment2.getInstance());

        FragmentAdapter adapter = new FragmentAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(isScrolling && positionOffset > 0){
                    if(positionOffset >= 0.5){
                        direction = -1;
                    }else{
                        direction = 1;
                    }
                    isScrolling = false;
                }else{
                    if(positionOffset > 0 && positionOffset > 0){
                        if(direction == -1){
                            BelowMenuView.moveToLeft(viewPager.getCurrentItem(),positionOffset);
                        }
                        else if(direction == 1){
                            BelowMenuView.moveToRight(viewPager.getCurrentItem(),positionOffset);
                        }
                    }

                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_DRAGGING){
                    isScrolling = true;
                    direction = 0;
                }
                if(state == ViewPager.SCROLL_STATE_IDLE){
                    BelowMenuView.drawBelowLine(viewPager.getCurrentItem());
                    direction = 0;
                }
            }
        });
    }
}
