package com.neotuner.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class OscilloscopeView extends View {
    private Paint waveformPaint;
    private Paint centerLinePaint;
    private Paint gridPaint;
    private Paint textPaint;
    private short[] audioBuffer;
    private double targetFrequency;
    private static final int MAX_SAMPLES = 1024;

    public OscilloscopeView(Context context) {
        super(context);
        init();
    }

    public OscilloscopeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OscilloscopeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        waveformPaint = new Paint();
        waveformPaint.setColor(Color.parseColor("#00FF88"));
        waveformPaint.setStyle(Paint.Style.STROKE);
        waveformPaint.setStrokeWidth(3);
        waveformPaint.setAntiAlias(true);

        centerLinePaint = new Paint();
        centerLinePaint.setColor(Color.parseColor("#FF6600"));
        centerLinePaint.setStyle(Paint.Style.STROKE);
        centerLinePaint.setStrokeWidth(2);

        gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#333333"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);
    }

    public void setAudioBuffer(short[] buffer) {
        if (buffer != null) {
            int copyLength = Math.min(buffer.length, MAX_SAMPLES);
            this.audioBuffer = new short[copyLength];
            System.arraycopy(buffer, 0, this.audioBuffer, 0, copyLength);
            postInvalidate();
        }
    }

    public void setTargetFrequency(double frequency) {
        this.targetFrequency = frequency;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;

        drawGrid(canvas, width, height, centerY);
        drawCenterLine(canvas, width, centerY);
        drawWaveform(canvas, width, height, centerY);
    }

    private void drawGrid(Canvas canvas, int width, int height, int centerY) {
        for (int i = 0; i <= 4; i++) {
            float y = centerY + (i - 2) * (height / 8f);
            canvas.drawLine(0, y, width, y, gridPaint);
        }

        for (int i = 0; i <= 8; i++) {
            float x = i * (width / 8f);
            canvas.drawLine(x, 0, x, height, gridPaint);
        }
    }

    private void drawCenterLine(Canvas canvas, int width, int centerY) {
        canvas.drawLine(0, centerY, width, centerY, centerLinePaint);
    }

    private void drawWaveform(Canvas canvas, int width, int height, int centerY) {
        if (audioBuffer == null || audioBuffer.length == 0) {
            return;
        }

        float xStep = (float) width / audioBuffer.length;
        float scale = height / 4f / 32768f;

        for (int i = 0; i < audioBuffer.length - 1; i++) {
            float x1 = i * xStep;
            float y1 = centerY - audioBuffer[i] * scale;
            float x2 = (i + 1) * xStep;
            float y2 = centerY - audioBuffer[i + 1] * scale;
            canvas.drawLine(x1, y1, x2, y2, waveformPaint);
        }
    }
}
