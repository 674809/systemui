package com.android.systemui.statusbarview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.renderscript.ScriptC;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.Util.BitmapFillet;
import com.android.systemui.Util.ScreenShotUtil;
import com.android.systemui.caustomView.BlurringView;
import com.android.systemui.statusbar.StatusBarService;
import com.android.systemui.statusbar.SystemUIWindowManager;

import androidx.annotation.NonNull;


public class PanelView extends FrameLayout {
    private String TAG = "PanelView";
    private float mUpX;
    private float mUpY;
    private float mDownX;
    private float mDownY;
    public boolean expended;
    private ObjectAnimator mAnimator;
    private boolean mAnimRunning;
    private float SCREEN_HEIGHT = 600f;
    private float mExpendHeight = 0;
    private float mLastTouchY;
    private Context mContext;
    private StatusBarService mStatusBarService;
    private int isBootGuide_setup = 0;
    private IUPData iupData;
    private IUPDateUI iuAnimation;
    private IUAlpha iuAlpha;
    private ImageView imgview;
    private BlurringView mBlurringView;
    private RelativeLayout blurring;
    private View mTopViewItem;
    public boolean isFlinger = false;
    private float alp=0;


    public PanelView(Context context) {
        super(context);
        mContext = context;
        mTopViewItem = LayoutInflater.from(context).inflate(R.layout.layout_panel, this);
      //  setTranslationY(-SCREEN_HEIGHT);
        SystemUI[] mServices = SystemUIApplication.getInstance().getServices();
        mStatusBarService = (StatusBarService) mServices[1];
        isBootGuide_setup = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0);
        mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(Settings.Secure.USER_SETUP_COMPLETE), true, new ChangeContentObserver());
        mBlurringView = findViewById(R.id.blurring_view);
        blurring = findViewById(R.id.blurred_view);

        imgview = findViewById(R.id.image0);
        //initView();

    }





    private void initView() {
        Bitmap bitmap = ScreenShotUtil.takeScreenShot(SystemUIApplication.getInstance());
        Drawable drawable1 = new BitmapDrawable(SystemUIApplication.getInstance().getResources(), bitmap);
        //   Log.i(TAG,"bitmap ="+bitmap);
        Bitmap cutbitmap = convertHardWareBitmap(bitmap);
        imgview.setImageBitmap(cutbitmap);
        mBlurringView.setBlurredView(blurring);
        mBlurringView.invalidate();
    }


    public static Bitmap convertHardWareBitmap(Bitmap src) {
        if (src.getConfig() != Bitmap.Config.HARDWARE) {
            //return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight());
            return src;
        }
        final int w = src.getWidth();
        final int h = src.getHeight();
        // For hardware bitmaps, use the Picture API to directly create a software bitmap
        Picture picture = new Picture();
        Canvas canvas = picture.beginRecording(w, h);
        canvas.drawBitmap(src, 0, 0, null);
        picture.endRecording();
        return Bitmap.createBitmap(picture, w, h, Bitmap.Config.ARGB_8888);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    public void handleTouchEvent(MotionEvent event) {
      //  Log.i(TAG, "handleTouchEvent");
        if (isBootGuide_setup == 0) { //判断是否是引导设置页面
            return;
        }
        if (mAnimRunning) {
            return;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getRawX();
                mDownY = event.getRawY();
                mLastTouchY = mDownY; //获取按压点的位置
                Log.i(TAG,"ybf ACTION_DOWN ="+mLastTouchY);
                break;

            case MotionEvent.ACTION_MOVE:
                float y = event.getRawY(); //获取移动后的Y位置
                  float diff = y - mLastTouchY; //移动当前位置 - 按压时的位置
                  mExpendHeight += diff; //展开高度 += 移动当前位置 - 按压时的位置

              //  mTopViewItem.setTranslationY(mLastTouchY-mDownY);
                 mLastTouchY = y;
                 float exh = mExpendHeight - SCREEN_HEIGHT;
               //  Log.i(TAG,"ybf isclose ="+(y-mDownY>0));

              //  Log.i(TAG,"exh="+exh);
                if(exh>0 || Math.abs(exh) < 20 ){
                    exh =0;
                }
                if(y-mDownY >40){
                    Show(Math.abs(exh));
                }
             //   Log.i(TAG,"exh="+exh);
                mTopViewItem.setTranslationY(exh);
                isFlinger = true;
                if(iuAlpha !=null){
                    alp = Math.abs(Math.abs(getY()/600f) -1);
                //   Log.i(TAG,"alp ="+alp);
                    iuAlpha.onBgAlpha(alp);
                }
                break;
            case MotionEvent.ACTION_UP:
                mUpX = event.getRawX(); //手指抬起的位置X
                mUpY = event.getY(); // 手指抬起的位置Y
               // mTopViewItem.setTranslationY(0f);
                Log.i(TAG,"ybf ACTION_UP ="+mUpY);
                if (mUpY > 501 && expended && mLastTouchY-mDownY <=  0) {
                   animClose();
                } else {
                    startIfAnimation();
                }
                break;

        }
    }
    private void moveTopView(float my) {
        if (mTopViewItem == null)
            return;

        if (mTopViewItem.getY() + my <= 0) {
            Log.i(TAG,"ybf sety ="+mTopViewItem.getY() + my);
            // mTopViewItem.getLayout().setY(mTopViewItem.getLayout().getY() + my);
            mTopViewItem.setY(mTopViewItem.getY() + my);
        }
    }
    private void startIfAnimation() {

        float diff = mUpY - mDownY; //手指抬起位置 - 手指按压位置 = 移动位移

        if (diff > 40) {
            animExpend();
        } else if (diff <= -40) {
            animClose();
        } /*else {
           if (expended) {
               animExpend();
           } else {

              animClose();
           }
       }*/
    }




    public void Show(float height) {
        setVisibility(VISIBLE);
        mStatusBarService.fullWindow(height);
    }

    public void animExpend() {
        isFlinger = false;
        if (expended) {
            return;
        }
        if (mAnimator != null) {
            Log.i(TAG, "animExpend cancel");
            mAnimator = null;
          //  canAnimate();
        }
        //   float start_open = -284.24667f;//mUpY - SCREEN_HEIGHT; // 抬起位置 -  显示屏幕高度
        float start_open = mUpY - SCREEN_HEIGHT >0 ? 0:mUpY-SCREEN_HEIGHT;//-SCREEN_HEIGHT;//mUpY -
        // SCREEN_HEIGHT; //
        // 抬起位置 -
        // 显示屏幕高度
        float end_open = 0;
        Log.i(TAG, "start_open =" + start_open);
        DecelerateInterpolator interpolator = new DecelerateInterpolator();  //插值器
        mAnimator = ObjectAnimator.ofFloat(this, "translationY", start_open, 0);
       // mAnimator.setInterpolator(interpolator);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(iuAnimation != null){
                    if(animation.getAnimatedFraction() >= alp ){
                        iuAnimation.onAninationState(animation.getAnimatedFraction());
                    }

                }
                Log.i(TAG,"onAnimationUpdate  open="+animation.getAnimatedFraction());
            }
        });

        mAnimator.setDuration(200)
                .addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mAnimRunning = true;
                        Log.i(TAG, "onAnimationStart animExpend");
                    //    SystemUIWindowManager.getInstance().minStatusWindow();
                        //   initView(); //毛玻璃
                        if (iupData != null) {
                            iupData.updataData();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimRunning = false;
                        finishExpended();


                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mAnimRunning = false;
                        Log.i(TAG, "onAnimationCancel animExpend");
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
        mAnimator.start();


    }


    private void finishExpended() {
        expended = true;
        mExpendHeight = SCREEN_HEIGHT;
        Log.i(TAG, "mExpendHeight =" + mExpendHeight);
    }

    public void animClose() {
        Log.i(TAG, "animClose expended=" + expended);
        isFlinger = false;
        if (!expended) {
            return;
        }
        if (mAnimator != null) {
            Log.i(TAG, "animClose cancel");
            mAnimator = null;
         //  canAnimate();
        }
        float start_close = mExpendHeight - SCREEN_HEIGHT; //
        float end_close = -SCREEN_HEIGHT;
        Log.i(TAG, "start_close =" + start_close);
        Log.i(TAG, "end_close =" + end_close);
        DecelerateInterpolator interpolator = new DecelerateInterpolator();  //插值器
        mAnimator = ObjectAnimator//
                .ofFloat(this, "translationY", start_close, -SCREEN_HEIGHT);

        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Log.i(TAG,"ybf onAnimationUpdate close="+(1-animation.getAnimatedFraction()));
                if(iuAnimation != null){
                    Log.i(TAG,"ybf alp = "+alp);
                    if(alp >= 1-animation.getAnimatedFraction()){
                        iuAnimation.onAninationState(1-animation.getAnimatedFraction());
                    }


                }
            }
        });
        mAnimator.setInterpolator(interpolator);
        mAnimator.setDuration(100)
                .addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        mAnimRunning = true;
                        Log.i(TAG, "onAnimationStart animClose");

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        Log.i(TAG, "onAnimationEnd");
                        mAnimRunning = false;
                        finishClose();
                   //     SystemUIWindowManager.getInstance().fullStatusWindow();
                        //   Settings.System.putInt(mContext.getContentResolver(), "ybf_isopen", 0);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mAnimRunning = false;
                        finishClose();
                        Log.i(TAG, "onAnimationCancel animClose");

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
        mAnimator.start();
    }

    public void finishClose() {
        setVisibility(GONE);
        mStatusBarService.minWindow();
        expended = false;
        mExpendHeight = 0;
        Log.i(TAG, "finishClose expended =" + expended);
    }



    class ChangeContentObserver extends ContentObserver {

        public ChangeContentObserver() {
            // TODO Auto-generated constructor stub
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            isBootGuide_setup = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 0);
            Log.i("TAG", "ChangeContentObserver --> onChange(" + isBootGuide_setup + ") ");
        }
    }

    public interface  IUPData{
        void updataData();
    }

    public void setUpData(IUPData iupData){
        this.iupData = iupData;
    }

    public interface IUPDateUI{
        void onAninationState(float type);
    }

    public void setAninationState(IUPDateUI iu){
        this.iuAnimation = iu;
    }

    public interface IUAlpha{
        void onBgAlpha(float alpha);
    }

    public void setAninationAlpha(IUAlpha alpha){
        this.iuAlpha = alpha;
    }

}
