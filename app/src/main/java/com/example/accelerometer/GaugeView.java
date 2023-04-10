package com.example.accelerometer;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.graphics.ColorUtils;

import java.util.Locale;

public class GaugeView extends View {

    private float accelerationValue = 0f;
    private Paint gaugePaint;
    private Paint needlePaint;
    private Paint backgroundPaint;
    private float centerX;
    private float centerY;
    private float radius;
    private float maxValue = 10f;
    private float minValue = 0f;
    private float startAngle = -90f;
    private float endAngle = 90f;
    private float numDivisions = 10;
    private float accelerationValueTextPadding = 10f;

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize the paints
        gaugePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gaugePaint.setStyle(Paint.Style.STROKE);
        gaugePaint.setStrokeWidth(40f);
        //gaugePaint.setColor(Color.RED);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStyle(Paint.Style.FILL);
        //needlePaint.setStrokeWidth(2.0f);


        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(0f);
        backgroundPaint.setColor(Color.GRAY);
    }

    //Smoothens out the needle animation
    public void setAccelerationValue(float acceleration) {
        ValueAnimator animator = ValueAnimator.ofFloat(accelerationValue, acceleration);
        animator.setDuration(500);
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Calculate the dimensions of the gauge
        centerX = getWidth() / 3f;
        centerY = getHeight() / 2f;
        radius = Math.min(centerX, centerY) - 10f;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Changes the position of the gauge
        float centerX = canvas.getWidth() / 2f;
        float centerY = canvas.getHeight() / 4f;

        // Draw the scale
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(30f);
        textPaint.setColor(Color.BLACK);
        float textHeight = textPaint.descent() - textPaint.ascent();
        float scaleStep = (maxValue - minValue) / numDivisions;
        float scaleValue = minValue;
        float scaleAngle = (endAngle - startAngle) / numDivisions;
        for (int i = 0; i <= numDivisions; i++) {
            // Calculate the angle for this label
            float labelAngle = (i / numDivisions) * (endAngle - startAngle) + startAngle;

            // Convert the angle to radians
            float labelAngleRad = (float) Math.toRadians(labelAngle);

            // Calculate the position of the label
            // CHanging + and - decides whether the numbers will be inside or outside of the gauge
            float x = centerX + (radius + 60f) * (float) Math.sin(labelAngleRad);
            float y = centerY - (radius + 60f) * (float) Math.cos(labelAngleRad);

            // Calculate the width of the text
            String label = String.format(Locale.getDefault(), "%.1f", scaleValue);
            float labelWidth = textPaint.measureText(label);

            // Draw the label from left to right
            canvas.drawText(label, x - labelWidth / 2f, y + textHeight / 2f - textPaint.descent(), textPaint);

            scaleValue += scaleStep;
        }

        // Calculate the angle of the needle based on the acceleration value
        float angle = Math.max(startAngle, Math.min(endAngle, (endAngle - startAngle) * (accelerationValue - minValue) / (maxValue - minValue) + startAngle));

        /*
        Limit the angle of the needle to be within the gauge arc
        float gaugeArc = endAngle - startAngle;
        float halfGaugeArc = gaugeArc / 2f;
        */

        float lowerLimit = startAngle;
        float upperLimit = endAngle;
        angle = Math.max(lowerLimit, Math.min(upperLimit, angle));

        // Draw the gauge needle
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

        // Update the gauge color based on the acceleration value
        int gaugeColor, needleColor;
        if (accelerationValue < 2.0) {
            needleColor = ColorUtils.blendARGB(Color.GREEN, Color.BLACK, 0.1f); // Set needle color to a darker shade of green for acceleration < 2.0
            gaugeColor = ColorUtils.blendARGB(Color.GREEN, Color.BLACK, 0.1f); // Set gauge color to a darker shade of green for acceleration < 2.0
        } else if (accelerationValue < 6.0) {
            needleColor = ColorUtils.blendARGB(Color.YELLOW, Color.BLACK, 0.1f); // Set needle color to a darker shade of yellow for acceleration between 2.0 and 6.0
            gaugeColor = Color.YELLOW; // Set gauge color to yellow for acceleration between 2.0 and 6.0
        } else {
            needleColor = Color.RED; // Set needle color to red for acceleration >= 6.0
            gaugeColor = Color.RED; // Set gauge color to red for acceleration >= 6.0
        }
        gaugePaint.setColor(gaugeColor);
        needlePaint.setColor(needleColor);

        // Draw the gauge outline
        canvas.drawArc(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 180f, 180f, false, gaugePaint);

        // Draw the acceleration value text
        String accelerationValueText = "Acceleration: " + String.format("%.2f", accelerationValue) + " m/s^2";
        Paint accelerationValueTextPaint = new Paint();
        accelerationValueTextPaint.setColor(Color.BLACK);
        accelerationValueTextPaint.setTextSize(50f);

        accelerationValueTextPaint.setTextAlign(Paint.Align.CENTER);
        accelerationValueTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        float accelerationValueTextWidth = accelerationValueTextPaint.measureText(accelerationValueText);
        float accelerationValueTextX = centerX;
        float accelerationValueTextY = centerY + radius + accelerationValueTextPadding + textHeight;
        canvas.drawText(accelerationValueText, accelerationValueTextX, accelerationValueTextY, accelerationValueTextPaint);

    }
}