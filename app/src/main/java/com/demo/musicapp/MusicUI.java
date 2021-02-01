package com.demo.musicapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class MusicUI extends View {
    private Visualizer visualizer;
    private float[] model = new float[2];

    public MusicUI(Context context) {
        super(context);
        init(context);
    }

    public MusicUI(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
//        MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse("/sdcard/Download/我的歌声里.mp3"));
//        if (mediaPlayer == null) {
//            System.out.println("mediaPlayer is null");
//            return;
//        }
//        mediaPlayer.setOnErrorListener(null);
//        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                startMusic(mediaPlayer.getAudioSessionId());
//            }
//        });
//        mediaPlayer.start();
    }

    public void startMusic(int id) {
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
                invalidate();
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true);
        visualizer.setEnabled(true);
    }

    public void stopMusic(){
        visualizer.setEnabled(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int mSpectrumCount = model.length;
        Paint mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(3f);
        for (int i = 0; i < mSpectrumCount; i++) {
            float startX = getWidth() * i / mSpectrumCount;
            float startY = getHeight() / 2;
            float stopX = getWidth() * i / mSpectrumCount;
            float stopY = getHeight() / 2 - model[i] * 2;
            canvas.drawLine(startX, startY, stopX, stopY, mPaint);
        }
    }
}

