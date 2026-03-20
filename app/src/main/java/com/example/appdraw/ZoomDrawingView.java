package com.example.appdraw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ZoomDrawingView extends View {

    public static class Stroke {
        public final Path path;
        public final Paint paint;
        public Bitmap fillBitmap;

        public Stroke(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
        
        public Stroke(Bitmap fillBitmap) {
            this.path = null;
            this.paint = null;
            this.fillBitmap = fillBitmap;
        }
    }

    public static class Layer {
        public String name;
        public final List<Stroke> strokes = new ArrayList<>();
        public final Deque<Stroke> undone = new ArrayDeque<>();
        public boolean isVisible = true;
        public boolean isLocked = false;

        public Layer(String name) {
            this.name = name;
        }
    }

    private final List<Layer> layers = new ArrayList<>();
    private int activeLayerIndex = 0;

    private int brushColor = Color.BLACK;
    private float brushSizePx = 24f;
    private int brushAlpha = 255; 
    private boolean eraserMode = false;
    private boolean fillMode = false;
    private int canvasBgColor = Color.WHITE;

    private Path currentPath = null;
    private Paint currentPaint;
    private final Paint bitmapPaint; // Paint tối ưu cho hiển thị ảnh

    private float lastX, lastY;
    private final float touchTolerance = 4f;

    private final Matrix viewMatrix = new Matrix();
    private final Matrix inverseMatrix = new Matrix();

    private boolean isScaling = false;
    private float lastMidX = 0f, lastMidY = 0f;

    private final ScaleGestureDetector scaleDetector;

    public ZoomDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        layers.add(new Layer("Nền"));
        layers.add(new Layer("Lớp 1"));
        activeLayerIndex = 1;

        currentPaint = makeBasePaint();
        
        // Cấu hình bitmapPaint để khử nhiễu tối đa
        bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        
        updatePaint();
        updateInverse();

        scaleDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        isScaling = true;
                        return true;
                    }

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float factor = detector.getScaleFactor();
                        viewMatrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
                        updateInverse();
                        invalidate();
                        return true;
                    }

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                        isScaling = false;
                    }
                });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(canvasBgColor);

        canvas.save();
        canvas.concat(viewMatrix);

        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            if (layer.isVisible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    canvas.saveLayer(null, null);
                } else {
                    canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
                }

                for (Stroke s : layer.strokes) {
                    if (s.fillBitmap != null) {
                        // Luôn dùng bitmapPaint để ảnh không bị vỡ khi zoom
                        canvas.drawBitmap(s.fillBitmap, 0, 0, bitmapPaint);
                    } else if (s.path != null) {
                        canvas.drawPath(s.path, s.paint);
                    }
                }
                
                if (i == activeLayerIndex && currentPath != null) {
                    canvas.drawPath(currentPath, currentPaint);
                }

                canvas.restore();
            }
        }

        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        if (event.getPointerCount() >= 2) {
            handleTwoFingerPan(event);
            return true;
        }
        if (isScaling) return true;

        Layer activeLayer = getActiveLayer();
        if (activeLayer == null || activeLayer.isLocked || !activeLayer.isVisible) return true;

        float[] mapped = mapToContent(event.getX(), event.getY());
        float x = mapped[0];
        float y = mapped[1];

        if (fillMode && event.getAction() == MotionEvent.ACTION_DOWN) {
            handleFloodFill((int) x, (int) y);
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activeLayer.undone.clear();
                currentPath = new Path();
                currentPath.moveTo(x, y);
                lastX = x;
                lastY = y;
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - lastX);
                float dy = Math.abs(y - lastY);
                if (dx >= touchTolerance || dy >= touchTolerance) {
                    currentPath.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f);
                    lastX = x;
                    lastY = y;
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (currentPath != null) {
                    currentPath.lineTo(x, y);
                    Paint p = new Paint(currentPaint);
                    activeLayer.strokes.add(new Stroke(currentPath, p));
                }
                currentPath = null;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handleTwoFingerPan(MotionEvent event) {
        float midX = (event.getX(0) + event.getX(1)) / 2f;
        float midY = (event.getY(0) + event.getY(1)) / 2f;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                lastMidX = midX;
                lastMidY = midY;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = midX - lastMidX;
                float dy = midY - lastMidY;
                viewMatrix.postTranslate(dx, dy);
                lastMidX = midX;
                lastMidY = midY;
                updateInverse();
                invalidate();
                break;
        }
    }

    private void handleFloodFill(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Layer layer = getActiveLayer();
        for (Stroke s : layer.strokes) {
            if (s.fillBitmap != null) canvas.drawBitmap(s.fillBitmap, 0, 0, bitmapPaint);
            else canvas.drawPath(s.path, s.paint);
        }
        int targetColor = bitmap.getPixel(x, y);
        int replacementColor = brushColor;
        if (targetColor == replacementColor) return;

        Bitmap fillResult = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));
        int[] pixels = new int[getWidth() * getHeight()];
        bitmap.getPixels(pixels, 0, getWidth(), 0, 0, getWidth(), getHeight());
        int[] resultPixels = new int[getWidth() * getHeight()];

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (pixels[p.y * getWidth() + p.x] == targetColor) {
                pixels[p.y * getWidth() + p.x] = replacementColor;
                resultPixels[p.y * getWidth() + p.x] = replacementColor;
                if (p.x > 0) queue.add(new Point(p.x - 1, p.y));
                if (p.x < getWidth() - 1) queue.add(new Point(p.x + 1, p.y));
                if (p.y > 0) queue.add(new Point(p.x, p.y - 1));
                if (p.y < getHeight() - 1) queue.add(new Point(p.x, p.y + 1));
            }
        }
        fillResult.setPixels(resultPixels, 0, getWidth(), 0, 0, getWidth(), getHeight());
        layer.strokes.add(new Stroke(fillResult));
        invalidate();
    }

    private float[] mapToContent(float vx, float vy) {
        float[] pts = new float[]{vx, vy};
        inverseMatrix.mapPoints(pts);
        return pts;
    }

    private void updateInverse() {
        viewMatrix.invert(inverseMatrix);
    }

    private Paint makeBasePaint() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setFilterBitmap(true); // Quan trọng để không bị nhiễu khi zoom
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        return p;
    }

    private void updatePaint() {
        if (eraserMode) {
            currentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            currentPaint.setAlpha(0);
        } else {
            currentPaint.setXfermode(null);
            currentPaint.setColor(brushColor);
            currentPaint.setAlpha(brushAlpha);
        }
        currentPaint.setStrokeWidth(brushSizePx);
    }

    public List<Layer> getLayers() { return layers; }
    public int getActiveLayerIndex() { return activeLayerIndex; }
    public void setActiveLayerIndex(int index) {
        if (index >= 0 && index < layers.size()) {
            activeLayerIndex = index;
            invalidate();
        }
    }
    public Layer getActiveLayer() {
        if (activeLayerIndex >= 0 && activeLayerIndex < layers.size()) return layers.get(activeLayerIndex);
        return null;
    }
    public void addLayer() {
        layers.add(new Layer("Lớp " + layers.size()));
        activeLayerIndex = layers.size() - 1;
        invalidate();
    }
    public void removeLayer(int index) {
        if (layers.size() > 1 && index >= 0 && index < layers.size()) {
            layers.remove(index);
            activeLayerIndex = Math.min(activeLayerIndex, layers.size() - 1);
            invalidate();
        }
    }
    public void setBrushColor(int color) {
        this.brushColor = color;
        updatePaint();
        invalidate();
    }
    public void setBrushSizePx(float px) {
        this.brushSizePx = Math.max(2f, px);
        updatePaint();
        invalidate();
    }
    public void setBrushOpacityPercent(int percent) {
        int p = Math.max(0, Math.min(100, percent));
        this.brushAlpha = (int) (255f * (p / 100f));
        updatePaint();
        invalidate();
    }
    public void setEraser(boolean enabled) {
        this.eraserMode = enabled;
        this.fillMode = false;
        updatePaint();
        invalidate();
    }
    public void setFillMode(boolean enabled) {
        this.fillMode = enabled;
        this.eraserMode = false;
        invalidate();
    }
    public boolean isEraser() { return eraserMode; }
    public boolean isFillMode() { return fillMode; }
    public void undo() {
        Layer activeLayer = getActiveLayer();
        if (activeLayer != null && !activeLayer.strokes.isEmpty()) {
            Stroke last = activeLayer.strokes.remove(activeLayer.strokes.size() - 1);
            activeLayer.undone.addLast(last);
            invalidate();
        }
    }
    public void redo() {
        Layer activeLayer = getActiveLayer();
        if (activeLayer != null && !activeLayer.undone.isEmpty()) {
            activeLayer.strokes.add(activeLayer.undone.removeLast());
            invalidate();
        }
    }
    public void clearAll() {
        Layer activeLayer = getActiveLayer();
        if (activeLayer != null) {
            activeLayer.strokes.clear();
            activeLayer.undone.clear();
            currentPath = null;
            invalidate();
        }
    }
    public Bitmap exportBitmap() {
        if (getWidth() <= 0 || getHeight() <= 0) return null;
        Bitmap out = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        draw(c);
        return out;
    }
}
