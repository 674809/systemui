package com.android.systemui.statusbarview;

import android.app.StatusBarManager;
import android.content.Context;


import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.systemui.statusbar.ControllerStatusBar;
import com.android.systemui.R;


/**
 * @author ybf
 * @brief description
 */
public class MainView extends FrameLayout implements PanelView.IUAlpha, PanelView.IUPDateUI {

    private static final String TAG = MainView.class.getSimpleName();
    private PanelView mPanelView;
    private int mDisable;
    private  ControllerStatusBar mControllerStatusBar;
    private View mShadeView; //阴影view

    public MainView(Context context) {
        super(context);
    //   initView(context);

        initPan(context);
    }

    public void initPan(Context context){
        mPanelView = new PanelView(context);
        addShadeView();
        addView(mPanelView);
        mPanelView.setVisibility(GONE);
        mControllerStatusBar = new ControllerStatusBar(mPanelView);
        mPanelView.findViewById(R.id.wifi);
        mPanelView.setAninationAlpha(this);
        mPanelView.setAninationState(this);
    }

    public PanelView getPanelView(){
        if(mPanelView == null){
            mPanelView = new PanelView(getContext());
        }
        return mPanelView ;
    }

    /**
     * 加入阴影View
     */
    private void addShadeView() {
        mShadeView = new View(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mShadeView.setLayoutParams(params);
        //设置阴影颜色
        mShadeView.setBackgroundColor(getResources().getColor(R.color.black_99));
        addView(mShadeView);
        mShadeView.layout(0, 0, getWidth(), getHeight());
        setShadeViewAlpha(0);
    }

    /**
     * 设置阴影VIew背景颜色
     *
     * @param p 透明度 0-1
     */
    private void setShadeViewAlpha(float p) {
        Log.i(TAG,"setShadeViewAlpha ="+p);
        if (p < 0.1)
            p = 0;
        if (p > 0.9)
            p = 1;

        mShadeView.setAlpha(p);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!canExpand()) {
            return false;
        }
        mPanelView.handleTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private boolean canExpand() {
        return (mDisable & StatusBarManager.DISABLE_EXPAND) == 0 ;
    }


    public void disable(int arg1) {
        Log.i(TAG,"canExpand ="+canExpand());
       if (!canExpand()) {
           mPanelView.animClose();
       }
       mDisable = arg1;
    }

    public void disable() {
        mPanelView.animClose();
    }

    public void finisPanView(){
        mPanelView.finishClose();
        if (mPanelView.expended){

        }
    }

    @Override
    public void onBgAlpha(float alpha) {
        Log.i(TAG,"onBgAlpha isFlinger="+ mPanelView.isFlinger);
        if(mPanelView.isFlinger){
            setShadeViewAlpha(alpha);
        }
    }

    @Override
    public void onAninationState(float type) {
        Log.i(TAG,"onAninationState ="+type);
        setShadeViewAlpha(type);
    }
}
