package com.liu.mytimer.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liu.mytimer.utils.Util;

/**
 * Created by kunming.liu on 2017/9/28.
 */

public class BaseFragment extends Fragment{
    private String TAG = this.getClass().getSimpleName();
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.log("%s fragment onCreate",TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.log("%s fragment onResume",TAG);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Util.log("%s fragment onHiddenChanged",TAG);
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Util.log("%s fragment onAttachFragment",TAG);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Util.log("%s fragment onAttach",TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Util.log("%s fragment onDestroyView",TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Util.log("%s fragment onDestroy",TAG);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Util.log("%s fragment onDetach",TAG);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Util.log("%s fragment onActivityCreated",TAG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Util.log("%s fragment onCreateView",TAG);
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Util.log("%s fragment onViewCreated",TAG);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Util.log("%s fragment onViewStateRestored",TAG);
    }

    @Override
    public void onStart() {
        super.onStart();
        Util.log("%s fragment onStart",TAG);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Util.log("%s fragment onSaveInstanceState",TAG);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Util.log("%s fragment onConfigurationChanged",TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        Util.log("%s fragment onPause",TAG);
    }

    @Override
    public void onStop() {
        super.onStop();
        Util.log("%s fragment onStop",TAG);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Util.log("%s fragment onLowMemory",TAG);
    }
}
