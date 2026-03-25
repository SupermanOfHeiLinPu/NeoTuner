package com.neotuner.app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class AudioRecorder {
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE_MULTIPLIER = 4;

    private AudioRecord audioRecord;
    private int bufferSize;
    private boolean isRecording = false;
    private Thread recordingThread;
    private AudioDataListener listener;

    public interface AudioDataListener {
        void onAudioData(short[] buffer);
    }

    public AudioRecorder(AudioDataListener listener) {
        this.listener = listener;
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        this.bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER;
    }

    public boolean startRecording() {
        if (isRecording) {
            return true;
        }

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                return false;
            }

            audioRecord.startRecording();
            isRecording = true;

            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    short[] buffer = new short[bufferSize];
                    while (isRecording) {
                        int readSize = audioRecord.read(buffer, 0, buffer.length);
                        if (readSize > 0 && listener != null) {
                            short[] data = new short[readSize];
                            System.arraycopy(buffer, 0, data, 0, readSize);
                            listener.onAudioData(data);
                        }
                    }
                }
            });
            recordingThread.start();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopRecording() {
        isRecording = false;

        if (recordingThread != null) {
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;
        }

        if (audioRecord != null) {
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                audioRecord.stop();
            }
            audioRecord.release();
            audioRecord = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}
