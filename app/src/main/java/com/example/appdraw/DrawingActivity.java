package com.example.appdraw;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class DrawingActivity extends AppCompatActivity {

    private ZoomDrawingView drawingView;
    private TextView txtSizeVal, txtOpacityVal, txtProjectName;
    private View toolPen, toolEraser, toolFill;
    private int selectedColor = Color.BLACK;
    private int selectedOpacity = 100;
    private String selectedBrushType = "Bút chì";
    
    private int penClickCount = 0;
    private Handler penClickHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        drawingView = findViewById(R.id.drawingView);
        txtSizeVal = findViewById(R.id.txtSizeVal);
        txtOpacityVal = findViewById(R.id.txtOpacityVal);
        txtProjectName = findViewById(R.id.txtProjectName);

        // Tools
        toolPen = findViewById(R.id.toolPen);
        toolEraser = findViewById(R.id.toolEraser);
        toolFill = findViewById(R.id.toolFill);
        View toolClear = findViewById(R.id.toolClear);

        // Action Buttons
        ImageView btnUndo = findViewById(R.id.btnUndo);
        ImageView btnRedo = findViewById(R.id.btnRedo);
        ImageView btnOpenColorPicker = findViewById(R.id.btnOpenColorPicker);
        ImageView btnLayers = findViewById(R.id.btnLayers);

        // Top Bar Buttons
        View btnSaveProject = findViewById(R.id.btnSaveProject);
        View btnExportImg = findViewById(R.id.btnExportImg);
        View btnBack = findViewById(R.id.btnBack);

        // SeekBars
        SeekBar seekSize = findViewById(R.id.seekSize);
        SeekBar seekOpacity = findViewById(R.id.seekOpacity);

        // Tool Listeners - Double click for Brush Settings
        toolPen.setOnClickListener(v -> {
            // Select tool immediately for better feedback
            if (drawingView.isEraser() || drawingView.isFillMode()) {
                selectTool("pen");
            }

            penClickCount++;
            if (penClickCount == 1) {
                penClickHandler.postDelayed(() -> {
                    penClickCount = 0;
                }, 300); // 300ms window for double click
            } else if (penClickCount == 2) {
                // Double Click logic
                showBrushSettingsDialog();
                penClickCount = 0;
            }
        });
        
        toolPen.setOnLongClickListener(v -> {
            showBrushSettingsDialog();
            return true;
        });

        toolEraser.setOnClickListener(v -> selectTool("eraser"));
        toolFill.setOnClickListener(v -> selectTool("fill"));
        
        // Thùng rác: Xóa thành trang trắng nhưng cho phép Undo (Quay lại)
        if (toolClear != null) {
            toolClear.setOnClickListener(v -> {
                drawingView.clearCanvasUndoable();
                Toast.makeText(this, "Đã xóa bảng vẽ (Có thể hoàn tác)", Toast.LENGTH_SHORT).show();
            });
        }

        btnUndo.setOnClickListener(v -> drawingView.undo());
        btnRedo.setOnClickListener(v -> drawingView.redo());

        btnOpenColorPicker.setOnClickListener(v -> showColorPickerDialog(null));
        btnLayers.setOnClickListener(v -> showLayersDialog());

        seekSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawingView.setBrushSizePx(progress);
                if (txtSizeVal != null) txtSizeVal.setText(progress + "px");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedOpacity = progress;
                drawingView.setBrushOpacityPercent(progress);
                if (txtOpacityVal != null) txtOpacityVal.setText(progress + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSaveProject.setOnClickListener(v -> showSaveProjectDialog());
        btnExportImg.setOnClickListener(v -> showExportDialog());
        
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        
        selectTool("pen");
    }

    private void selectTool(String tool) {
        drawingView.setEraser(tool.equals("eraser"));
        drawingView.setFillMode(tool.equals("fill"));

        toolPen.setBackgroundResource(tool.equals("pen") ? R.drawable.selected_tool_bg : 0);
        toolEraser.setBackgroundResource(tool.equals("eraser") ? R.drawable.selected_tool_bg : 0);
        toolFill.setBackgroundResource(tool.equals("fill") ? R.drawable.selected_tool_bg : 0);
        
        updateToolUI(toolPen, tool.equals("pen"), "#1A73E8", "#5F6368");
        updateToolUI(toolEraser, tool.equals("eraser"), "#1A73E8", "#5F6368");
        updateToolUI(toolFill, tool.equals("fill"), "#1A73E8", "#5F6368");
    }

    private void updateToolUI(View view, boolean isSelected, String activeHex, String inactiveHex) {
        if (!(view instanceof ViewGroup)) return;
        ViewGroup vg = (ViewGroup) view;
        int color = Color.parseColor(isSelected ? activeHex : inactiveHex);
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(color);
            } else if (child instanceof TextView) {
                ((TextView) child).setTextColor(color);
            }
        }
    }

    private void setupWideDialog(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
    }

    private void showConfirmClearDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_save_project);
        setupWideDialog(dialog);
        
        TextView title = dialog.findViewById(R.id.txtTitleSave);
        if (title != null) title.setText("Xóa tất cả?");
        
        EditText input = dialog.findViewById(R.id.etProjectName);
        if (input != null) input.setVisibility(View.GONE);
        
        View labelDesc = dialog.findViewById(R.id.etProjectDesc);
        if (labelDesc != null) labelDesc.setVisibility(View.GONE);
        
        View progressArea = dialog.findViewById(R.id.pb_save_progress);
        if (progressArea != null && progressArea.getParent() instanceof View) {
             ((View)progressArea.getParent()).setVisibility(View.GONE);
        }
        dialog.findViewById(R.id.pb_save_progress).setVisibility(View.GONE);
        dialog.findViewById(R.id.cb_save_step1).setVisibility(View.GONE);
        dialog.findViewById(R.id.cb_save_step2).setVisibility(View.GONE);
        dialog.findViewById(R.id.cb_save_step3).setVisibility(View.GONE);
        dialog.findViewById(R.id.cb_save_step4).setVisibility(View.GONE);
        dialog.findViewById(R.id.cb_save_step5).setVisibility(View.GONE);

        Button btnYes = dialog.findViewById(R.id.btnFinalSave);
        if (btnYes != null) {
            btnYes.setText("Xóa");
            btnYes.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.RED));
            btnYes.setOnClickListener(v -> {
                drawingView.clearAll();
                dialog.dismiss();
            });
        }
        dialog.show();
    }

    private interface OnColorPickedListener {
        void onPicked(int color);
    }

    private void showColorPickerDialog(OnColorPickedListener callback) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_color_picker);
        setupWideDialog(dialog);

        ColorWheelView colorWheel = dialog.findViewById(R.id.colorWheel);
        ViewGroup gridRecentColors = dialog.findViewById(R.id.recentColorsGrid);
        ViewGroup gridBaseColors = dialog.findViewById(R.id.baseColorsGrid);
        SeekBar dialogOpacity = dialog.findViewById(R.id.dialogOpacity);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        final int[] tempColor = {selectedColor};
        dialogOpacity.setProgress(selectedOpacity);
        colorWheel.setSelectedColor(selectedColor);

        colorWheel.setOnColorSelectedListener(color -> tempColor[0] = color);

        if (gridRecentColors != null) {
            for (int i = 0; i < gridRecentColors.getChildCount(); i++) {
                View cv = gridRecentColors.getChildAt(i);
                cv.setOnClickListener(v -> {
                    if (v.getBackground() instanceof ColorDrawable) {
                        tempColor[0] = ((ColorDrawable) v.getBackground()).getColor();
                        colorWheel.setSelectedColor(tempColor[0]);
                    }
                });
            }
        }

        if (gridBaseColors != null) {
            for (int i = 0; i < gridBaseColors.getChildCount(); i++) {
                View cv = gridBaseColors.getChildAt(i);
                cv.setOnClickListener(v -> {
                    if (v.getBackgroundTintList() != null) {
                        tempColor[0] = v.getBackgroundTintList().getDefaultColor();
                        colorWheel.setSelectedColor(tempColor[0]);
                    }
                });
            }
        }

        btnSave.setOnClickListener(v -> {
            selectedColor = tempColor[0];
            drawingView.setBrushColor(selectedColor);
            selectedOpacity = dialogOpacity.getProgress();
            drawingView.setBrushOpacityPercent(selectedOpacity);
            
            SeekBar mainOpacity = findViewById(R.id.seekOpacity);
            if (mainOpacity != null) mainOpacity.setProgress(selectedOpacity);
            
            if (callback != null) callback.onPicked(selectedColor);
            
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showLayersDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_layers);
        setupWideDialog(dialog);

        RecyclerView rvLayers = dialog.findViewById(R.id.rvLayers);
        rvLayers.setLayoutManager(new LinearLayoutManager(this));
        
        LayersAdapter adapter = new LayersAdapter(drawingView.getLayers(), drawingView.getActiveLayerIndex(), new LayersAdapter.OnLayerActionListener() {
            @Override public void onSelected(int index) { drawingView.setActiveLayerIndex(index); }
            @Override public void onToggleVisibility(int index) {
                drawingView.getLayers().get(index).isVisible = !drawingView.getLayers().get(index).isVisible;
                drawingView.invalidate();
            }
            @Override public void onDelete(int index) {
                if (drawingView.getLayers().size() > 1) drawingView.removeLayer(index);
                else Toast.makeText(DrawingActivity.this, "Cần ít nhất 1 Layer", Toast.LENGTH_SHORT).show();
            }
        });
        rvLayers.setAdapter(adapter);

        dialog.findViewById(R.id.btnAddLayer).setOnClickListener(v -> {
            drawingView.addLayer();
            adapter.setActiveIndex(drawingView.getActiveLayerIndex());
            adapter.notifyDataSetChanged();
            rvLayers.smoothScrollToPosition(drawingView.getLayers().size() - 1);
        });

        dialog.findViewById(R.id.btnDoneLayers).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnCancelLayers).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showBrushSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_brush_settings);
        setupWideDialog(dialog);

        // --- Brush Selection Logic ---
        ViewGroup[] brushes = new ViewGroup[]{
                dialog.findViewById(R.id.brush1), dialog.findViewById(R.id.brush2),
                dialog.findViewById(R.id.brush3), dialog.findViewById(R.id.brush4),
                dialog.findViewById(R.id.brush5), dialog.findViewById(R.id.brush6),
                dialog.findViewById(R.id.brush7), dialog.findViewById(R.id.brush8)
        };

        for (ViewGroup brush : brushes) {
            if (brush == null) continue;
            
            // Set initial selection
            TextView tv = (TextView) brush.getChildAt(1);
            if (tv != null && tv.getText().toString().equals(selectedBrushType)) {
                brush.setBackgroundResource(R.drawable.selected_tool_bg);
            }

            brush.setOnClickListener(v -> {
                for (ViewGroup b : brushes) if (b != null) b.setBackgroundResource(0);
                v.setBackgroundResource(R.drawable.selected_tool_bg);
                selectedBrushType = ((TextView) ((ViewGroup) v).getChildAt(1)).getText().toString();
                Toast.makeText(this, "Đã chọn: " + selectedBrushType, Toast.LENGTH_SHORT).show();
                
                // Khi người dùng chủ động chọn loại bút trong dialog, chuyển sang chế độ bút
                selectTool("pen");
            });
        }

        View viewCurrentColor = dialog.findViewById(R.id.viewCurrentColor);
        TextView txtColorHex = dialog.findViewById(R.id.txtColorHex);
        SeekBar seekBrushOpacity = dialog.findViewById(R.id.seekBrushOpacity);

        if (viewCurrentColor != null) viewCurrentColor.setBackgroundColor(selectedColor);
        if (txtColorHex != null) txtColorHex.setText(String.format("#%06X", (0xFFFFFF & selectedColor)));
        if (seekBrushOpacity != null) seekBrushOpacity.setProgress(selectedOpacity);

        View btnDialogColorPicker = dialog.findViewById(R.id.btnDialogColorPicker);
        if (btnDialogColorPicker != null) {
            btnDialogColorPicker.setOnClickListener(v -> showColorPickerDialog(color -> {
                if (viewCurrentColor != null) viewCurrentColor.setBackgroundColor(color);
                if (txtColorHex != null) txtColorHex.setText(String.format("#%06X", (0xFFFFFF & color)));
                
                // Khi đổi màu, chuyển sang chế độ bút
                selectTool("pen");
            }));
        }

        dialog.findViewById(R.id.btnSaveBrush).setOnClickListener(v -> {
            if (seekBrushOpacity != null) {
                selectedOpacity = seekBrushOpacity.getProgress();
                drawingView.setBrushOpacityPercent(selectedOpacity);
                ((SeekBar)findViewById(R.id.seekOpacity)).setProgress(selectedOpacity);
            }
            // Không tự động gọi selectTool("pen") khi lưu cài đặt chung (như độ mờ)
            // để giữ nguyên trạng thái nếu người dùng đang dùng tẩy.
            dialog.dismiss();
        });

        dialog.findViewById(R.id.btnCancelBrush).setOnClickListener(v -> dialog.dismiss());
        if (dialog.findViewById(R.id.btnBackBrush) != null) {
            dialog.findViewById(R.id.btnBackBrush).setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showExportDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_export);
        setupWideDialog(dialog);

        dialog.findViewById(R.id.btnExportToDevice).setOnClickListener(v -> {
            exportImage(false);
            dialog.dismiss();
        });

        View btnShare = dialog.findViewById(R.id.btnShareImg);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> {
                exportImage(true);
                dialog.dismiss();
            });
        }
        
        View btnBack = dialog.findViewById(R.id.btnBackExport);
        if (btnBack != null) btnBack.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showSaveProjectDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_save_project);
        setupWideDialog(dialog);

        EditText et = dialog.findViewById(R.id.etProjectName);
        LinearProgressIndicator pbSave = dialog.findViewById(R.id.pb_save_progress);
        TextView tvPercent = dialog.findViewById(R.id.tv_save_progress_percent);
        
        CheckBox[] checkBoxes = new CheckBox[]{
                dialog.findViewById(R.id.cb_save_step1),
                dialog.findViewById(R.id.cb_save_step2),
                dialog.findViewById(R.id.cb_save_step3),
                dialog.findViewById(R.id.cb_save_step4),
                dialog.findViewById(R.id.cb_save_step5)
        };

        for (CheckBox cb : checkBoxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int checkedCount = 0;
                for (CheckBox c : checkBoxes) if (c.isChecked()) checkedCount++;
                int percent = (checkedCount * 100) / checkBoxes.length;
                if (pbSave != null) pbSave.setProgress(percent);
                if (tvPercent != null) tvPercent.setText(percent + "%");
            });
        }

        dialog.findViewById(R.id.btnFinalSave).setOnClickListener(v -> {
            String name = et.getText().toString();
            if (name.isEmpty()) name = "Bản vẽ mới";
            txtProjectName.setText(name);
            Toast.makeText(this, "Đã lưu dự án: " + name, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void exportImage(boolean share) {
        Bitmap bitmap = drawingView.exportBitmap();
        if (bitmap == null) return;
        String filename = "AppDraw_" + System.currentTimeMillis() + ".png";
        
        try {
            if (share) {
                File cachePath = new File(getExternalCacheDir(), "images");
                cachePath.mkdirs();
                File file = new File(cachePath, filename);
                FileOutputStream stream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();

                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Chia sẻ tác phẩm"));
            } else {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                cv.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cv.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/AppDraw");
                }
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
                OutputStream os = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.close();
                Toast.makeText(this, "Đã lưu vào bộ sưu tập!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static class LayersAdapter extends RecyclerView.Adapter<LayersAdapter.ViewHolder> {
        private final List<ZoomDrawingView.Layer> layers;
        private int activeIndex;
        private final OnLayerActionListener listener;
        public interface OnLayerActionListener { void onSelected(int index); void onToggleVisibility(int index); void onDelete(int index); }
        public LayersAdapter(List<ZoomDrawingView.Layer> layers, int activeIndex, OnLayerActionListener listener) {
            this.layers = layers;
            this.activeIndex = activeIndex;
            this.listener = listener;
        }
        public void setActiveIndex(int index) { this.activeIndex = index; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ZoomDrawingView.Layer layer = layers.get(position);
            holder.text1.setText(layer.name + (position == activeIndex ? " (Đang chọn)" : ""));
            holder.text2.setText(layer.isVisible ? "👁️ Đang hiện" : "🚫 Đang ẩn");
            holder.itemView.setBackgroundColor(position == activeIndex ? Color.parseColor("#E3F2FD") : Color.TRANSPARENT);
            holder.itemView.setOnClickListener(v -> { activeIndex = position; listener.onSelected(position); notifyDataSetChanged(); });
            holder.text2.setOnClickListener(v -> { listener.onToggleVisibility(position); notifyItemChanged(position); });
            holder.itemView.setOnLongClickListener(v -> { listener.onDelete(position); notifyDataSetChanged(); return true; });
        }
        @Override public int getItemCount() { return layers.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            ViewHolder(View v) { super(v); text1 = v.findViewById(android.R.id.text1); text2 = v.findViewById(android.R.id.text2); }
        }
    }
}
