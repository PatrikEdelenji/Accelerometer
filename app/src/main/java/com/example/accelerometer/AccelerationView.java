package com.example.accelerometer;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import androidx.core.graphics.ColorUtils;

import java.util.Locale;

public class AccelerationView extends View {

    private float accelerationValue = 0f;
    private Paint gaugePaint;
    private Paint needlePaint;
    private Paint backgroundPaint;
    private float radius = 300;
    private float maxValue = 5f;
    private float minValue = -5f;
    private float startAngle = -90f;
    private float endAngle = 90f;
    private float numDivisions = 10;

    public AccelerationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {

        gaugePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gaugePaint.setStyle(Paint.Style.STROKE);
        gaugePaint.setStrokeWidth(40f);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(0f);
        backgroundPaint.setColor(Color.GRAY);
    }


    public void setAccelerationValue(float acceleration) {
        ValueAnimator animator = ValueAnimator.ofFloat(accelerationValue, acceleration);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                accelerationValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float centerX = canvas.getWidth() / 2f;
        float centerY = canvas.getHeight() / 4f;

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(30f);
        textPaint.setColor(Color.BLACK);
        float textHeight = textPaint.descent() - textPaint.ascent();
        float scaleStep = (maxValue - minValue) / numDivisions;
        float scaleValue = minValue;
        for (int i = 0; i <= numDivisions; i++) {
            float labelAngle = (i / numDivisions) * (endAngle - startAngle) + startAngle;
            float labelAngleRad = (float) Math.toRadians(labelAngle);
            float x = centerX + (radius + 60f) * (float) Math.sin(labelAngleRad);
            float y = centerY - (radius + 60f) * (float) Math.cos(labelAngleRad);

            String label = String.format(Locale.getDefault(), "%.1f", scaleValue);
            float labelWidth = textPaint.measureText(label);

            canvas.drawText(label, x - labelWidth / 2f, y + textHeight / 2f - textPaint.descent(), textPaint);
            scaleValue += scaleStep;
        }


        float angle = Math.max(startAngle, Math.min(endAngle, (endAngle - startAngle) * (accelerationValue - minValue) / (maxValue - minValue) + startAngle));
        float lowerLimit = startAngle;
        float upperLimit = endAngle;
        angle = Math.max(lowerLimit, Math.min(upperLimit, angle));

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


        int gaugeColor, needleColor;
        if (accelerationValue < 2.0 && accelerationValue > -2.0) {
            needleColor = ColorUtils.blendARGB(Color.GREEN, Color.BLACK, 0.1f);
            gaugeColor = ColorUtils.blendARGB(Color.GREEN, Color.BLACK, 0.1f);
        } else if (accelerationValue < 2.943 && accelerationValue > -2.943) {
            needleColor = ColorUtils.blendARGB(Color.YELLOW, Color.BLACK, 0.1f);
            gaugeColor = Color.YELLOW;
        } else {
            needleColor = Color.RED;
            gaugeColor = Color.RED;
        }
        gaugePaint.setColor(gaugeColor);
        needlePaint.setColor(needleColor);

        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 180f, 180f, false, gaugePaint);

    }
}
