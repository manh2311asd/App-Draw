package com.example.appdraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorWheelView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int selectedColor = Color.BLACK;
    private PointF selectedPoint = null;
    private OnColorSelectedListener listener;

    private static final int NUM_RINGS = 8;
    private static final int NUM_SEGMENTS = 24;

    private final RectF rectF = new RectF();
    private final float[] hsv = new float[3];

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(3f);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public void setSelectedColor(int color) {
        this.selectedColor = color;
        Color.colorToHSV(color, hsv);
        updatePointFromColor();
        invalidate();
    }

    private void updatePointFromColor() {
        // Reverse calculation: HSV to XY
        post(() -> {
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            float maxRadius = Math.min(centerX, centerY) * 0.9f;
            float innerRadius = maxRadius * 0.2f;
            float ringWidth = (maxRadius - innerRadius) / NUM_RINGS;

            float angle = hsv[0];
            float saturation = hsv[1];
            
            // ringIndex calculation: saturation = 1.0 - ringIndex/NUM_RINGS
            // ringIndex = (1.0 - saturation) * NUM_RINGS
            float ringIndex = (1.0f - saturation) * NUM_RINGS;
            float dist = maxRadius - (ringIndex * ringWidth) - (ringWidth / 2f);

            double rad = Math.toRadians(angle);
            selectedPoint = new PointF(
                (float) (centerX + dist * Math.cos(rad)),
                (float) (centerY + dist * Math.sin(rad))
            );
            invalidate();
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float maxRadius = Math.min(centerX, centerY) * 0.9f;
        float innerRadius = maxRadius * 0.2f;
        float ringWidth = (maxRadius - innerRadius) / NUM_RINGS;

        for (int ring = 0; ring < NUM_RINGS; ring++) {
            float rOuter = maxRadius - (ring * ringWidth);
            float rInner = rOuter - ringWidth;
            float saturation = 1.0f - ((float) ring / NUM_RINGS);
            
            for (int seg = 0; seg < NUM_SEGMENTS; seg++) {
                float startAngle = seg * (360f / NUM_SEGMENTS);
                float sweepAngle = 360f / NUM_SEGMENTS;
                
                int color = Color.HSVToColor(new float[]{startAngle, saturation, 1.0f});
                paint.setColor(color);
                paint.setStyle(Paint.Style.FILL);

                Path path = new Path();
                rectF.set(centerX - rOuter, centerY - rOuter, centerX + rOuter, centerY + rOuter);
                path.arcTo(rectF, startAngle, sweepAngle);
                
                rectF.set(centerX - rInner, centerY - rInner, centerX + rInner, centerY + rInner);
                path.arcTo(rectF, startAngle + sweepAngle, -sweepAngle);
                path.close();
                
                canvas.drawPath(path, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.WHITE);
                paint.setStrokeWidth(1.5f);
                canvas.drawPath(path, paint);
            }
        }

        // Draw the indicator (Eyedropper/Pen icon)
        if (selectedPoint != null) {
            drawIndicator(canvas, selectedPoint.x, selectedPoint.y);
        }
    }

    private void drawIndicator(Canvas canvas, float x, float y) {
        // Draw a simple "Eyedropper" or "Pen" shape
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(45); // Tilt it like in the image

        // Outer white glow/border
        indicatorPaint.setColor(Color.WHITE);
        indicatorPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, 18, indicatorPaint);
        
        // Inner black border
        indicatorPaint.setColor(Color.BLACK);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        indicatorPaint.setStrokeWidth(2f);
        canvas.drawCircle(0, 0, 18, indicatorPaint);

        // Selected color in the center
        indicatorPaint.setColor(selectedColor);
        indicatorPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0, 0, 12, indicatorPaint);
        
        // Draw a small "tip" to make it look more like a pen/eyedropper
        Path tip = new Path();
        tip.moveTo(18, 0);
        tip.lineTo(28, 0);
        tip.lineTo(20, 5);
        tip.close();
        indicatorPaint.setColor(Color.BLACK);
        canvas.drawPath(tip, indicatorPaint);

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();
            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;
            
            double dx = x - centerX;
            double dy = y - centerY;
            double dist = Math.sqrt(dx * dx + dy * dy);
            
            float maxRadius = Math.min(centerX, centerY) * 0.9f;
            float innerRadius = maxRadius * 0.2f;

            if (dist >= innerRadius && dist <= maxRadius) {
                double angle = Math.toDegrees(Math.atan2(dy, dx));
                if (angle < 0) angle += 360;
                
                float ringWidth = (maxRadius - innerRadius) / NUM_RINGS;
                int ringIndex = (int) ((maxRadius - dist) / ringWidth);
                if (ringIndex >= NUM_RINGS) ringIndex = NUM_RINGS - 1;
                
                float saturation = 1.0f - ((float) ringIndex / NUM_RINGS);
                int color = Color.HSVToColor(new float[]{(float) angle, saturation, 1.0f});
                
                selectedColor = color;
                selectedPoint = new PointF(x, y);
                invalidate();
                
                if (listener != null) {
                    listener.onColorSelected(color);
                }
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}