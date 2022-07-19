package com.android.systemui.statusbarview;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.SeekBar;

import com.android.systemui.R;


/**
 * @author ybf
 * @brief description
 */
public class MSeekBar extends SeekBar {
    // 比例对应的原点分辨率
    private Drawable thumb;
    private Resources res;
    private Paint paint;
    private Paint painttext;
    private Bitmap bmp;
    private Drawable mThumb;
    private int type = 3;


    public MSeekBar(Context context) {
        this(context, null);
    }

    @SuppressWarnings("deprecation")
    public MSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(getResources().getColor(R.color.white));
        res = context.getResources();

        painttext = new Paint();
        painttext.setTextAlign(Paint.Align.CENTER);
        painttext.setColor(getResources().getColor(R.color.white_60));
        painttext.setTextSize(17);
        painttext.setAntiAlias(true);

        bmp = BitmapFactory.decodeResource(res, R.drawable.slidebtn);
        Bitmap mbmp = zoomImg(bmp, 1, 1);
        thumb = new BitmapDrawable(mbmp);

//		paint.setTextSize(0);
//		// 设置拖动的图片
//
    //    setThumb(thumb);
//		// 图片的位置
        setThumbOffset(0);
    }

    /*等比缩放图片*/
    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    @Override
    public void setThumb(Drawable thumb) {
        // TODO Auto-generated method stub
        super.setThumb(thumb);
        this.mThumb = thumb;
    }

    public Drawable getSeekBarThumb() {
        return mThumb;
    }

    // 设置thumb的偏移数值
    @Override
    public void setThumbOffset(int thumbOffset) {
        // TODO Auto-generated method stub
        super.setThumbOffset(thumbOffset / 4);
    }


    String temp_str = "0";
    Rect rect;
    String data_str = "";
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        canvas.save();
        int data = Integer.parseInt(temp_str);
        rect = getSeekBarThumb().getBounds();
        float fontwidth = paint.measureText(temp_str);
        if (type == 0) {
            if (data < 2) {
                canvas.drawCircle(rect.left + (rect.width()) / 2.0F, rect.top - paint.ascent() - 10, 0, paint);
            } else if (data >=10 && data <70) {
                canvas.drawCircle(rect.left + (rect.width()) + 44, rect.top - paint.ascent() - 10, 15, paint);
            } else{
                canvas.drawCircle(rect.left + (rect.width()) / 2.0F, rect.top - paint.ascent() - 10, 15, paint);
            }
            data_str = (int)((float) data/15*100) +"%";
            Log.i("ybf", "data_str 0=" + data_str);
        } else if (type == 1) {
            if (data == 0) {
                canvas.drawCircle(rect.left + (rect.width()) / 2.0F, rect.top - paint.ascent() - 10, 0, paint);
            } else if (data > 0 && data <= 20) {
                canvas.drawCircle(rect.left + (rect.width()) + 44, rect.top - paint.ascent() - 10, 15, paint);
            } else {
                canvas.drawCircle(rect.left + (rect.width()) / 2.0F, rect.top - paint.ascent() - 10, 15, paint);
            }
            data_str = (int)((float) data/255*100) +"%";
            Log.i("ybf", "data_str 1 =" + data_str);
        } else {
            canvas.drawCircle(rect.left + (rect.width()) / 2.0F, rect.top - paint.ascent() - 10, 0, paint);
        }
        canvas.drawText(data_str,410 ,rect.top - paint.ascent()-5,painttext);
        canvas.restore();
    }

    public void SetValue(String value,int max) {
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        temp_str = sb.toString();
        int data = Integer.parseInt(temp_str);
        data_str = (int)((float) data/max*100) +"%";
        invalidate();
    }


    public void setType(int type) {
        this.type = type;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setOnSeekBarChangeListener(final SeekBar.OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.i("ybf", "progress =" + progress);
                if (l != null) {
                    l.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (l != null) {
                    l.onStartTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (l != null) {
                    l.onStopTrackingTouch(seekBar);
                }
            }
        });
    }
}
