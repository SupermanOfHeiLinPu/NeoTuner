package com.neotuner.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String PREFS_NAME = "NeoTunerPrefs";
    private static final String KEY_A4_FREQUENCY = "a4_frequency";

    private AudioRecorder audioRecorder;
    private PitchDetector pitchDetector;
    private OscilloscopeView oscilloscopeView;
    private TextView noteNameTextView;
    private TextView frequencyValueTextView;
    private TextView centsTextView;
    private TextView targetFrequencyTextView;

    private Handler mainHandler;
    private short[] currentAudioBuffer;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadA4Frequency();

        initViews();
        initComponents();

        if (checkPermission()) {
            startTuner();
        } else {
            requestPermission();
        }
    }

    private void loadA4Frequency() {
        double savedFreq = prefs.getFloat(KEY_A4_FREQUENCY, 440.0f);
        PianoNote.setA4Frequency(savedFreq);
    }

    private void saveA4Frequency(double frequency) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_A4_FREQUENCY, (float) frequency);
        editor.apply();
        PianoNote.setA4Frequency(frequency);
    }

    private void initViews() {
        oscilloscopeView = findViewById(R.id.oscilloscopeView);
        noteNameTextView = findViewById(R.id.noteNameTextView);
        frequencyValueTextView = findViewById(R.id.frequencyValueTextView);
        centsTextView = findViewById(R.id.centsTextView);
        targetFrequencyTextView = findViewById(R.id.targetFrequencyTextView);
    }

    private void initComponents() {
        mainHandler = new Handler(Looper.getMainLooper());
        pitchDetector = new PitchDetector();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_more) {
            showMoreMenu(findViewById(R.id.menu_more));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMoreMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.menu_more_options, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                return handleMoreMenuItem(menuItem);
            }
        });

        popupMenu.show();
    }

    private boolean handleMoreMenuItem(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_set_a4_frequency) {
            showA4FrequencyDialog();
            return true;
        } else if (itemId == R.id.menu_about) {
            showAboutDialog();
            return true;
        }

        return false;
    }

    private void showA4FrequencyDialog() {
        final double[] frequencies = {440.0, 441.0, 442.0, 443.0, 444.0};
        final String[] items = new String[frequencies.length];
        for (int i = 0; i < frequencies.length; i++) {
            items[i] = String.format(Locale.getDefault(), "%.0f Hz", frequencies[i]);
        }

        double currentFreq = PianoNote.getA4Frequency();
        int checkedIndex = 0;
        for (int i = 0; i < frequencies.length; i++) {
            if (Math.abs(frequencies[i] - currentFreq) < 0.1) {
                checkedIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择A4频率");
        builder.setSingleChoiceItems(items, checkedIndex, (dialog, which) -> {
            saveA4Frequency(frequencies[which]);
            Toast.makeText(this, "A4频率已设置为 " + items[which], Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.show();
    }

    private void showAboutDialog() {
        String versionName = "1.0";
        String aboutMessage = String.format(Locale.getDefault(),
                "NeoTuner\n\n" +
                        "版本: %s\n\n" +
                        "一个功能强大的Android调音器应用\n" +
                        "支持钢琴88键全音域\n\n" +
                        "作者: NeoTuner Team\n\n" +
                        "© 2025 All Rights Reserved",
                versionName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("关于");
        builder.setMessage(aboutMessage);
        builder.setPositiveButton("确定", null);
        builder.show();
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
                        frequencyValueTextView.setText(String.format(Locale.getDefault(), "%.2f", frequency));
                        targetFrequencyTextView.setText(String.format(Locale.getDefault(), "目标: %.2f Hz", note.frequency));

                        String centsText = String.format(Locale.getDefault(), "%+.1f", cents);
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
                    frequencyValueTextView.setText("--");
                    targetFrequencyTextView.setText("目标: -- Hz");
                    centsTextView.setText("0.0");
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
