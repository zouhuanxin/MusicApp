package com.demo.musicapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * 音乐气氛灯
 */
public class MusicAtmosphereLamp extends View {
    private Visualizer visualizer;
    private float[] model = new float[2];
    private String[] colors = {"#FFB6C1", "#DC143C", "#DB7093", "#FF1493", "#DA70D6", "#8B008B", "#9400D3", "#9370DB", "#7B68EE", "#0000FF", "#6495ED", "#1E90FF", "#00BFFF", "#5F9EA0", "#AFEEEE", "#7FFFAA", "#3CB371", "#FFD700"};
    private int sta = 0;
    private int lastIndex = 0;

    public MusicAtmosphereLamp(Context context) {
        super(context, null);
    }

    public MusicAtmosphereLamp(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {

    }

    public void startMusic(int id) {
        if (visualizer != null) {
            visualizer.setEnabled(false);
        }
        visualizer = new Visualizer(id);
        visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                model = new float[fft.length / 2 + 1];
                model[0] = (byte) Math.abs(fft[1]);
                int j = 1;
                for (int i = 2; i < fft.length / 2; ) {
                    model[j] = (float) Math.hypot(fft[i], fft[i + 1]);
                    i += 2;
                    j++;
                    model[j] = (float) Math.abs(fft[j]);
                }
                //model即为最终用于绘制的数据
                sta = 1;
                invalidate();
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true);
        visualizer.setEnabled(true);
    }

    public void stopMusic() {
        visualizer.setEnabled(false);
    }

    public void updateDraw(float[] result) {
        model = new float[result.length];
        for (int i = 0; i < result.length; i = i + 2) {
            model[i] = result[i];
        }
        sta = 2;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int h = getHeight() / 12;
        Paint paint = new Paint();
        if (sta == 0) {
            //首次
            paint.setColor(Color.parseColor("#000000"));
            for (int i = 0; i < 12; i++) {
                canvas.drawRect(new RectF(20, h * i + 10, getWidth() - 20, h + h * i), paint);
            }
        } else {
            int mSpectrumCount = model.length;
            for (int i = 0; i < mSpectrumCount; i++) {
                float value = model[i];
                int max = 0;
                if (sta == 1) {
                    max = getSum(value, 2, 10);
                } else if (sta == 2) {
                    max = getSum(value, 40, 40);
                }
                if (max > lastIndex) {
                    //本次需要显示到灯光大于上一次，则开始从上一次到位置开始增加
                    for (int j = (12 - lastIndex); j >= max; j--) {
                        paint.setColor(Color.parseColor(colors[j]));
                        canvas.drawRect(new RectF(20, h * j + 10, getWidth() - 20, h + h * j), paint);
                    }
                } else if (max < lastIndex) {
                    //本次需要显示到灯光小于上一次，则开始从上一次到位置开始减少
                    for (int j = getIndex(lastIndex); j < getIndex(max); j++) {
                        paint.setColor(Color.parseColor("#000000"));
                        canvas.drawRect(new RectF(20, h * j + 10, getWidth() - 20, h + h * j), paint);
                    }
                } else {
                    //如果是等于则不执行本次操作
                }
                lastIndex = max;
            }
        }
    }

    private int getSum(float value, float filter, float interval) {
        int max = 0;
        //判断要冲到第几个
        if (value < filter) {
            max = 0;
        } else if (value < filter + interval * 1) {
            max = 1;
        } else if (value < filter + interval * 2) {
            max = 2;
        } else if (value < filter + interval * 3) {
            max = 3;
        } else if (value < filter + interval * 4) {
            max = 4;
        } else if (value < filter + interval * 5) {
            max = 5;
        } else if (value < filter + interval * 6) {
            max = 6;
        } else if (value < filter + interval * 7) {
            max = 7;
        } else if (value < filter + interval * 8) {
            max = 8;
        } else if (value < filter + interval * 9) {
            max = 9;
        } else if (value < filter + interval * 10) {
            max = 10;
        } else if (value < filter + interval * 11) {
            max = 11;
        } else if (value < filter + interval * 12) {
            max = 12;
        }
        return max;
    }


    //返回对应下标
    private int getIndex(int value) {
        if (value >= 1 && value <= 12) {
            return (12 - value + 1);
        }
        return 0;
    }
}

