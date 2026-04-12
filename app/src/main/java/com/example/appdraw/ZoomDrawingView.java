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

    public enum BrushType { PENCIL, WATERCOLOR, CRAYON, INK, MARKER, AIRBRUSH, CALLIGRAPHY, OIL }
    public enum Hardness { SOFT, MEDIUM, HARD }

    public static class Stroke {
        public final Path path;
        public final Paint paint;
        public Bitmap fillBitmap;
        public boolean isClearMarker = false;

        public Stroke(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
        
        public Stroke(Bitmap fillBitmap) {
            this.path = null;
            this.paint = null;
            this.fillBitmap = fillBitmap;
        }

        public Stroke(boolean isClearMarker) {
            this.path = null;
            this.paint = null;
            this.isClearMarker = isClearMarker;
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
    private float brushSizePx = 10f;
    private int brushAlpha = 255; 
    private BrushType brushType = BrushType.INK;
    private Hardness hardness = Hardness.MEDIUM;
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
    
    private float mScaleFactor = 1.0f;
    private final float MIN_SCALE = 1.0f; // Thu nhỏ tối đa vừa khít khung (100%)
    private final float MAX_SCALE = 20.0f; // Cho phép phóng to tối đa 20 lần

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
                        float prevScale = mScaleFactor;
                        mScaleFactor *= factor;
                        mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));
                        float effectiveFactor = mScaleFactor / prevScale;
                        
                        viewMatrix.postScale(effectiveFactor, effectiveFactor, detector.getFocusX(), detector.getFocusY());
                        clampMatrix();
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
                    if (s.isClearMarker) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    } else if (s.fillBitmap != null) {
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
            if (currentPath != null) {
                currentPath = null;
                invalidate();
            }
            handleTwoFingerPan(event);
            return true;
        }
        
        if (isScaling) {
            if (currentPath != null) {
                currentPath = null;
                invalidate();
            }
            return true;
        }

        Layer activeLayer = getActiveLayer();
        if (activeLayer == null || activeLayer.isLocked || !activeLayer.isVisible) return true;

        float[] mapped = mapToContent(event.getX(), event.getY());
        float x = mapped[0];
        float y = mapped[1];
        
        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
            return true;
        }

        if (fillMode && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
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
                if (currentPath != null) {
                    float dx = Math.abs(x - lastX);
                    float dy = Math.abs(y - lastY);
                    if (dx >= touchTolerance || dy >= touchTolerance) {
                        currentPath.quadTo(lastX, lastY, (x + lastX) / 2f, (y + lastY) / 2f);
                        lastX = x;
                        lastY = y;
                    }
                    invalidate();
                }
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
                if (lastMidX == 0f && lastMidY == 0f) {
                    lastMidX = midX;
                    lastMidY = midY;
                }
                float dx = midX - lastMidX;
                float dy = midY - lastMidY;
                viewMatrix.postTranslate(dx, dy);
                clampMatrix();
                lastMidX = midX;
                lastMidY = midY;
                updateInverse();
                invalidate();
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                lastMidX = 0f;
                lastMidY = 0f;
                break;
        }
    }

    private void handleFloodFill(int x, int y) {
        if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return;
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Layer layer = getActiveLayer();
        for (Stroke s : layer.strokes) {
            if (s.isClearMarker) canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            else if (s.fillBitmap != null) canvas.drawBitmap(s.fillBitmap, 0, 0, bitmapPaint);
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
        currentPaint.setPathEffect(null);
        currentPaint.setMaskFilter(null);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setXfermode(null);

        if (eraserMode) {
            currentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            currentPaint.setAlpha(255);
            currentPaint.setStrokeWidth(brushSizePx);
            return;
        }

        currentPaint.setColor(brushColor);
        int finalAlpha = brushAlpha;

        switch (brushType) {
            case WATERCOLOR:
                finalAlpha = (int)(brushAlpha * 0.4f);
                break;
            case CRAYON:
                currentPaint.setPathEffect(new android.graphics.DiscretePathEffect(Math.max(brushSizePx * 0.2f, 2f), Math.max(brushSizePx * 0.5f, 2f)));
                break;
            case MARKER:
                currentPaint.setStrokeCap(Paint.Cap.SQUARE);
                finalAlpha = (int)(brushAlpha * 0.6f);
                break;
            case AIRBRUSH:
                currentPaint.setMaskFilter(new android.graphics.BlurMaskFilter(Math.max(brushSizePx * 1.5f, 2f), android.graphics.BlurMaskFilter.Blur.NORMAL));
                finalAlpha = (int)(brushAlpha * 0.5f);
                break;
            case CALLIGRAPHY:
                Path dashPath = new Path();
                dashPath.addOval(new android.graphics.RectF(0, 0, brushSizePx, Math.max(brushSizePx * 0.2f, 2f)), Path.Direction.CCW);
                Matrix m = new Matrix();
                m.postRotate(45, brushSizePx / 2f, brushSizePx * 0.1f);
                dashPath.transform(m);
                currentPaint.setPathEffect(new android.graphics.PathDashPathEffect(dashPath, brushSizePx * 0.15f, 0, android.graphics.PathDashPathEffect.Style.MORPH));
                break;
            case OIL:
                currentPaint.setPathEffect(new android.graphics.DiscretePathEffect(brushSizePx * 0.1f, brushSizePx * 0.2f));
                break;
            default:
                break;
        }

        if (brushType != BrushType.AIRBRUSH && brushType != BrushType.CALLIGRAPHY) {
            if (hardness == Hardness.SOFT) {
                currentPaint.setMaskFilter(new android.graphics.BlurMaskFilter(Math.max(brushSizePx * 0.5f, 1f), android.graphics.BlurMaskFilter.Blur.NORMAL));
            } else if (hardness == Hardness.MEDIUM) {
                currentPaint.setMaskFilter(new android.graphics.BlurMaskFilter(Math.max(brushSizePx * 0.15f, 1f), android.graphics.BlurMaskFilter.Blur.NORMAL));
            }
        }

        currentPaint.setAlpha(finalAlpha);
        currentPaint.setStrokeWidth(brushSizePx);
    }

    public void setBrushType(BrushType type) {
        this.brushType = type;
        setEraser(false);
        setFillMode(false);
        updatePaint();
        invalidate();
    }
    public BrushType getBrushType() { return brushType; }

    public void setHardness(Hardness h) {
        this.hardness = h;
        updatePaint();
        invalidate();
    }
    public Hardness getHardness() { return hardness; }

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
        // KHÔNG tự động tắt eraserMode ở đây để tránh bị chuyển về bút khi không muốn
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
    public int getBrushOpacityPercent() {
        return (int) ((brushAlpha / 255f) * 100);
    }
    public void setEraser(boolean enabled) {
        this.eraserMode = enabled;
        if (enabled) this.fillMode = false;
        updatePaint();
        invalidate();
    }
    public void setFillMode(boolean enabled) {
        this.fillMode = enabled;
        if (enabled) this.eraserMode = false;
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
    public void clearCanvasUndoable() {
        Layer activeLayer = getActiveLayer();
        if (activeLayer != null) {
            activeLayer.undone.clear();
            activeLayer.strokes.add(new Stroke(true));
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

    private void clampMatrix() {
        if (getWidth() == 0 || getHeight() == 0) return;
        float[] values = new float[9];
        viewMatrix.getValues(values);
        float scale = values[Matrix.MSCALE_X];
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];

        float winWidth = getWidth();
        float winHeight = getHeight();
        float scaledWidth = winWidth * scale;
        float scaledHeight = winHeight * scale;

        if (scaledWidth >= winWidth) {
            transX = Math.max(winWidth - scaledWidth, Math.min(transX, 0));
        } else {
            transX = (winWidth - scaledWidth) / 2f;
        }

        if (scaledHeight >= winHeight) {
            transY = Math.max(winHeight - scaledHeight, Math.min(transY, 0));
        } else {
            transY = (winHeight - scaledHeight) / 2f;
        }

        values[Matrix.MTRANS_X] = transX;
        values[Matrix.MTRANS_Y] = transY;
        viewMatrix.setValues(values);
    }
}
