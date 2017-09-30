package com.liu.mytimer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.liu.mytimer.R;

/**
 * Created by kunming.liu on 2017/9/28.
 */

public class Fragment2 extends Fragment {
    private static Fragment2 instance = null;

    public static Fragment2 getInstance(){
        if(instance == null){
            synchronized (Fragment2.class) {
                if (instance == null) {
                    instance = new Fragment2();
                }
            }
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view= inflater.inflate(R.layout.layout2, container, false);

        //对View中控件的操作方法
        Button btn = (Button)view.findViewById(R.id.fragment1_btn);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(getActivity(), "点击了第一个fragment的BTN", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
