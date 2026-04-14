package com.example.appdraw.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {
    private Paint arcPaint;
    private Paint gridPaint;
    private int[] colors;
    private OnColorSelectedListener listener;
    private float cx, cy, radius, innerRadius, ringThickness;
    private int currentColor = Color.RED;
    private float selX, selY;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorPickerView(Context context) {
        super(context);
        init();
    }

    public ColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.WHITE);
        gridPaint.setStrokeWidth(3f);
        gridPaint.setStyle(Paint.Style.STROKE);
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int color) {
        this.currentColor = color;
        if (innerRadius > 0 && ringThickness > 0) {
            updateSelectorFromColor();
        }
        invalidate();
    }

    private void updateSelectorFromColor() {
        float[] hsv = new float[3];
        Color.colorToHSV(currentColor, hsv);
        float hue = hsv[0];
        float sat = hsv[1];

        float shiftedAngle = hue + 15f;
        if (shiftedAngle >= 360) shiftedAngle -= 360;
        int sliceIndex = (int) (shiftedAngle / 30f);

        int ringIndex = Math.round(sat * 5f) - 1;
        if (ringIndex < 0) ringIndex = 0;
        if (ringIndex > 4) ringIndex = 4;

        float snapAngleRad = (float) Math.toRadians(sliceIndex * 30f);
        float snapDist = innerRadius + ringThickness * (ringIndex + 0.5f);
        selX = cx + (float) Math.cos(snapAngleRad) * snapDist;
        selY = cy + (float) Math.sin(snapAngleRad) * snapDist;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cx = w / 2f;
        cy = h / 2f;
        radius = Math.min(cx, cy) - 20;
        innerRadius = radius * 0.35f;
        ringThickness = (radius - innerRadius) / 5f;

        updateSelectorFromColor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw 5 rings x 12 slices
        arcPaint.setStrokeWidth(ringThickness);
        for (int r = 0; r < 5; r++) {
            float rCenter = innerRadius + ringThickness * (r + 0.5f);
            android.graphics.RectF oval = new android.graphics.RectF(cx - rCenter, cy - rCenter, cx + rCenter, cy + rCenter);
            float sat = (r + 1) / 5f;

            for (int i = 0; i < 12; i++) {
                float hue = i * 30f;
                int color = Color.HSVToColor(new float[]{hue, sat, 1f});
                arcPaint.setColor(color);
                canvas.drawArc(oval, i * 30f - 15f, 30.5f, false, arcPaint);
            }
        }

        // Draw white grid lines (concentric)
        for (int r = 0; r <= 5; r++) {
            float rBoundary = innerRadius + ringThickness * r;
            canvas.drawCircle(cx, cy, rBoundary, gridPaint);
        }

        // Draw white grid lines (radial)
        for (int i = 0; i < 12; i++) {
            float angleRad = (float) Math.toRadians(i * 30f - 15f);
            float startX = cx + (float) Math.cos(angleRad) * innerRadius;
            float startY = cy + (float) Math.sin(angleRad) * innerRadius;
            float endX = cx + (float) Math.cos(angleRad) * radius;
            float endY = cy + (float) Math.sin(angleRad) * radius;
            canvas.drawLine(startX, startY, endX, endY, gridPaint);
        }

        // Draw pipette selector
        android.graphics.drawable.Drawable pipette = androidx.core.content.ContextCompat.getDrawable(getContext(), com.example.appdraw.R.drawable.ic_pipette);
        if (pipette != null) {
            int w = pipette.getIntrinsicWidth();
            int h = pipette.getIntrinsicHeight();
            int offset = 4;
            pipette.setBounds((int)selX - offset, (int)selY - h + offset, (int)selX + w - offset, (int)selY + offset);
            pipette.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float x = event.getX() - cx;
            float y = event.getY() - cy;
            float dist = (float) Math.sqrt(x * x + y * y);

            if (dist < innerRadius) dist = innerRadius + 0.1f;
            if (dist > radius - 0.1f) dist = radius - 0.1f;

            float angle = (float) Math.toDegrees(Math.atan2(y, x));
            if (angle < 0) angle += 360;

            float shiftedAngle = angle + 15f;
            if (shiftedAngle >= 360) shiftedAngle -= 360;
            int sliceIndex = (int) (shiftedAngle / 30f);
            float hue = sliceIndex * 30f;

            int ringIndex = (int) ((dist - innerRadius) / ringThickness);
            if (ringIndex < 0) ringIndex = 0;
            if (ringIndex > 4) ringIndex = 4;
            float sat = (ringIndex + 1) / 5f;

            currentColor = Color.HSVToColor(new float[]{hue, sat, 1f});

            float snapAngleRad = (float) Math.toRadians(sliceIndex * 30f);
            float snapDist = innerRadius + ringThickness * (ringIndex + 0.5f);
            selX = cx + (float) Math.cos(snapAngleRad) * snapDist;
            selY = cy + (float) Math.sin(snapAngleRad) * snapDist;

            if (listener != null) listener.onColorSelected(currentColor);
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }
}
