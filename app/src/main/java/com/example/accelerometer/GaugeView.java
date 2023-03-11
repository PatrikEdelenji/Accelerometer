package com.example.accelerometer;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import java.util.Locale;

public class GaugeView extends View {

    private float accelerationValue = 0f;
    private Paint gaugePaint;
    private Paint backgroundPaint;
    private float centerX;
    private float centerY;
    private float radius;
    private float maxValue = 10f;
    private float minValue = -10f;
    private float startAngle = 90f;
    private float endAngle = 270f;
    private float numDivisions = 10;

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize the paints
        gaugePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gaugePaint.setStyle(Paint.Style.STROKE);
        gaugePaint.setStrokeWidth(40f);
        gaugePaint.setColor(Color.RED);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(40f);
        backgroundPaint.setColor(Color.GRAY);
    }

    public void setAccelerationValue(float acceleration) {
        accelerationValue = acceleration;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calculate the dimensions of the gauge
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;
        radius = Math.min(centerX, centerY) - 40f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Draw the scale
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(30f);
        textPaint.setColor(Color.BLACK);
        float textHeight = textPaint.descent() - textPaint.ascent();
        float scaleStep = (maxValue - minValue) / numDivisions;
        float scaleValue = minValue;
        float scaleAngle = (endAngle - startAngle) / numDivisions;
        for (int i = 0; i <= numDivisions; i++) {
            float scaleAngleRad = (float) Math.toRadians(startAngle + i * scaleAngle);
            float x = centerX + (radius - 60f) * (float) Math.cos(scaleAngleRad);
            float y = centerY + (radius - 60f) * (float) Math.sin(scaleAngleRad);
            canvas.drawText(String.format(Locale.getDefault(), "%.1f", scaleValue), x, y + textHeight / 2f, textPaint);
            scaleValue += scaleStep;
        }

        // Calculate the angle of the needle based on the acceleration value
        float angle = Math.max(startAngle, Math.min(endAngle, (endAngle - startAngle) * (accelerationValue - minValue) / (maxValue - minValue) + startAngle));

        // Draw the gauge needle
        Paint needlePaint = new Paint();
        needlePaint.setColor(Color.RED);
        needlePaint.setStyle(Paint.Style.FILL);
        Path needlePath = new Path();
        needlePath.moveTo(centerX, centerY - radius);
        needlePath.lineTo(centerX - 20, centerY);
        needlePath.lineTo(centerX, centerY + radius / 2);
        needlePath.lineTo(centerX + 20, centerY);
        needlePath.close();
        canvas.save();
        canvas.rotate(angle, centerX, centerY);
        canvas.drawPath(needlePath, needlePaint);
        canvas.restore();

        // Draw the gauge outline
        canvas.drawCircle(centerX, centerY, radius, gaugePaint);
    }
}