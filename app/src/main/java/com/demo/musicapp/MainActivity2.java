package com.demo.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * 不使用AudioTrack和Visualizer，手写的dft算法来实现频谱动画
 */
public class MainActivity2 extends AppCompatActivity {
    MusicAtmosphereLampCopy musicUI;
    boolean isRecording = false;//是否录放的标记
    static final int frequency = 44100;
    //CHANNEL_OUT_STEREO
    static final int channelConfiguration = AudioFormat.CHANNEL_OUT_STEREO;
    //ENCODING_PCM_16BIT
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize, playBufSize;
    AudioRecord audioRecord;
    AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        NavigationManager.setBottomNavigationColor(this);
        new PermissionUtils().verifyStoragePermissions(this, null);
        recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        playBufSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        // MediaRecorder.AudioSource.VOICE_COMMUNICATION
        // MediaRecorder.AudioSource.MIC
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, frequency, channelConfiguration, audioEncoding, recBufSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
        musicUI = findViewById(R.id.musiclamp);
        audioTrack.setStereoVolume(1f, 1f);//设置当前音量大小
        musicUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecording = true;
                new RecordPlayThread().start();// 开一条线程边录边放
            }
        });
    }

    class RecordPlayThread extends Thread {
        public void run() {
            try {
                byte[] buffer = new byte[recBufSize];
                audioRecord.startRecording();//开始录制
//                audioTrack.play();//开始播放
//                musicUI.startMusic(audioTrack.getAudioSessionId());
                while (isRecording) {
//                    //从MIC保存数据到缓冲区
                    int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
                    int N = 16;
                    double PI = 3.1415926;
                    float[] real = new float[N];
                    float[] imag = new float[N];
                    for (int k = 0; k < N; k++) {
                        for (int n = 0; n < N; n++) {
                            real[k] = (float) (real[k] + buffer[n] * Math.cos(2 * PI * k * n / N));
                            imag[k] = (float) (imag[k] - buffer[n] * Math.sin(2 * PI * k * n / N));
                        }
                    }
                    float[] result = new float[real.length];
                    for (int i = 0; i < real.length; i++) {
                        result[i] = (float) Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musicUI.updateDraw(result);
                        }
                    });
                    Thread.sleep(100);
//                    byte[] tmpBuf = new byte[bufferReadResult];
//                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
//                    //写入数据即播放
//                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
                }
                audioTrack.stop();
                audioRecord.stop();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}