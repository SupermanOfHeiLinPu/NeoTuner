package com.neotuner.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private AudioRecorder audioRecorder;
    private PitchDetector pitchDetector;
    private OscilloscopeView oscilloscopeView;
    private TextView noteNameTextView;
    private TextView frequencyTextView;
    private TextView centsTextView;
    private TextView targetFrequencyTextView;

    private Handler mainHandler;
    private short[] currentAudioBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initComponents();

        if (checkPermission()) {
            startTuner();
        } else {
            requestPermission();
        }
    }

    private void initViews() {
        oscilloscopeView = findViewById(R.id.oscilloscopeView);
        noteNameTextView = findViewById(R.id.noteNameTextView);
        frequencyTextView = findViewById(R.id.frequencyTextView);
        centsTextView = findViewById(R.id.centsTextView);
        targetFrequencyTextView = findViewById(R.id.targetFrequencyTextView);
    }

    private void initComponents() {
        mainHandler = new Handler(Looper.getMainLooper());
        pitchDetector = new PitchDetector();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTuner();
            } else {
                Toast.makeText(this, "需要录音权限才能使用调音器", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startTuner() {
        audioRecorder = new AudioRecorder(new AudioRecorder.AudioDataListener() {
            @Override
            public void onAudioData(short[] buffer) {
                currentAudioBuffer = buffer;
                processAudio(buffer);
            }
        });

        boolean started = audioRecorder.startRecording();
        if (!started) {
            Toast.makeText(this, "无法启动录音", Toast.LENGTH_LONG).show();
        }
    }

    private void processAudio(short[] buffer) {
        double frequency = pitchDetector.detectPitch(buffer);

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                oscilloscopeView.setAudioBuffer(buffer);

                if (frequency > 0) {
                    PianoNote.Note note = PianoNote.findClosestNote(frequency);
                    if (note != null) {
                        double cents = PianoNote.getCentsDifference(frequency, note.frequency);

                        noteNameTextView.setText(note.getFullName());
                        frequencyTextView.setText(String.format(Locale.getDefault(), "%.2f Hz", frequency));
                        targetFrequencyTextView.setText(String.format(Locale.getDefault(), "目标: %.2f Hz", note.frequency));

                        String centsText = String.format(Locale.getDefault(), "%+.1f 音分", cents);
                        centsTextView.setText(centsText);

                        if (Math.abs(cents) < 5) {
                            centsTextView.setTextColor(0xFF00FF00);
                        } else if (Math.abs(cents) < 15) {
                            centsTextView.setTextColor(0xFFFFFF00);
                        } else {
                            centsTextView.setTextColor(0xFFFF0000);
                        }

                        oscilloscopeView.setTargetFrequency(note.frequency);
                    }
                } else {
                    noteNameTextView.setText("--");
                    frequencyTextView.setText("-- Hz");
                    targetFrequencyTextView.setText("目标: -- Hz");
                    centsTextView.setText("0.0 音分");
                    centsTextView.setTextColor(0xFFFFFFFF);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecorder != null) {
            audioRecorder.stopRecording();
        }
    }
}
