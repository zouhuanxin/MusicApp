package com.demo.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 配合AudioTrack使用静音播放模式,使用Visualizer可视化工具来实现fft快速算法来表达动画
 */
public class MainActivity extends AppCompatActivity {
    Button btnRecord, btnStop, btnExit;
    MusicUI musicUI;
    boolean isRecording = false;//是否录放的标记
    static final int frequency = 44100;
    static final int channelConfiguration = AudioFormat.CHANNEL_OUT_STEREO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int recBufSize, playBufSize;
    AudioRecord audioRecord;
    AudioTrack audioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new PermissionUtils().verifyStoragePermissions(this, null);
        recBufSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        playBufSize = AudioTrack.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        // MediaRecorder.AudioSource.VOICE_COMMUNICATION
        // MediaRecorder.AudioSource.MIC
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, frequency, channelConfiguration, audioEncoding, recBufSize);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, frequency, channelConfiguration, audioEncoding, playBufSize, AudioTrack.MODE_STREAM);
        musicUI = findViewById(R.id.musicui);
        btnRecord = (Button) this.findViewById(R.id.btnRecord);
        btnRecord.setOnClickListener(new ClickEvent());
        btnStop = (Button) this.findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new ClickEvent());
        btnExit = (Button) this.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new ClickEvent());
        audioTrack.setStereoVolume(0.0011f, 0.0011f);//设置当前音量大小
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btnRecord) {
                isRecording = true;
                new RecordPlayThread().start();// 开一条线程边录边放
            } else if (v == btnStop) {
                isRecording = false;
            } else if (v == btnExit) {
                isRecording = false;
                finish();
            }
        }
    }

    class RecordPlayThread extends Thread {
        public void run() {
            try {
                byte[] buffer = new byte[recBufSize];
                audioRecord.startRecording();//开始录制
                audioTrack.play();//开始播放
                musicUI.startMusic(audioTrack.getAudioSessionId());
                while (isRecording) {
                    //从MIC保存数据到缓冲区
                    int bufferReadResult = audioRecord.read(buffer, 0, recBufSize);
                    byte[] tmpBuf = new byte[bufferReadResult];
                    System.arraycopy(buffer, 0, tmpBuf, 0, bufferReadResult);
                    //写入数据即播放
                    audioTrack.write(tmpBuf, 0, tmpBuf.length);
                }
                audioTrack.stop();
                audioRecord.stop();
            } catch (Throwable t) {
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}