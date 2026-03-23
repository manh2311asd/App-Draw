package com.example.appdraw;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProjectListActivity extends AppCompatActivity {

    private TextView tabDoing, tabCompleted;
    private boolean isShowingDoing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        Toolbar toolbar = findViewById(R.id.toolbar_projects);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tabDoing = findViewById(R.id.tab_doing);
        tabCompleted = findViewById(R.id.tab_completed);

        tabDoing.setOnClickListener(v -> showDoingTab());
        tabCompleted.setOnClickListener(v -> showCompletedTab());

        // Mặc định hiển thị tab Đang làm
        showDoingTab();

        // Xử lý sự kiện nhấn vào nút FAB (+) để tạo dự án mới
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_project);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(this, CreateProjectActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showDoingTab() {
        isShowingDoing = true;
        tabDoing.setBackgroundResource(R.drawable.bg_chip_selected);
        tabDoing.setBackgroundTintList(ColorStateList.valueOf(0xFFF0B259));
        tabDoing.setTextColor(Color.WHITE);

        tabCompleted.setBackgroundResource(0);
        tabCompleted.setTextColor(Color.parseColor("#666666"));

        setupDoingProjects();
    }

    private void showCompletedTab() {
        isShowingDoing = false;
        tabCompleted.setBackgroundResource(R.drawable.bg_chip_selected);
        tabCompleted.setBackgroundTintList(ColorStateList.valueOf(0xFFF0B259));
        tabCompleted.setTextColor(Color.WHITE);

        tabDoing.setBackgroundResource(0);
        tabDoing.setTextColor(Color.parseColor("#666666"));

        setupCompletedProjects();
    }

    private void setupDoingProjects() {
        setupProjectItem(findViewById(R.id.proj_1), "Tranh núi sơn dầu", R.drawable.tp_trending_1, 40, false, null);
        setupProjectItem(findViewById(R.id.proj_2), "Tranh cây cảnh", R.drawable.ve_thien_nhien, 70, false, null);
        setupProjectItem(findViewById(R.id.proj_3), "Cảnh biển", R.drawable.tp_trending_3, 30, false, null);
        setupProjectItem(findViewById(R.id.proj_4), "Cảnh hoàng hôn", R.drawable.ve_hoa_mau_nuoc, 85, false, null);
        setupProjectItem(findViewById(R.id.proj_5), "Tranh núi sơn dầu 2", R.drawable.tp_trending_1, 50, false, null);
        setupProjectItem(findViewById(R.id.proj_6), "Mầm cây", R.drawable.img_challenge_tree, 20, false, null);
        
        // Ẩn các ô thừa nếu có
        if (findViewById(R.id.proj_7) != null) findViewById(R.id.proj_7).setVisibility(View.GONE);
        if (findViewById(R.id.proj_8) != null) findViewById(R.id.proj_8).setVisibility(View.GONE);
    }

    private void setupCompletedProjects() {
        setupProjectItem(findViewById(R.id.proj_1), "Tranh sơn dầu", R.drawable.tp_trending_1, 100, true, "Hoàn thành 20/12/2023");
        setupProjectItem(findViewById(R.id.proj_2), "Tranh tĩnh vật", R.drawable.tp_trending_2, 100, true, "Hoàn thành 15/06/2023");
        setupProjectItem(findViewById(R.id.proj_3), "Tranh màu nước", R.drawable.ve_hoa_mau_nuoc, 100, true, "Hoàn thành 10/12/2024");
        setupProjectItem(findViewById(R.id.proj_4), "Tranh núi sơn dầu", R.drawable.ve_thien_nhien, 100, true, "Hoàn thành 10/10/2024");
        setupProjectItem(findViewById(R.id.proj_5), "Tranh đồ dùng", R.drawable.tp_trending_1, 100, true, "Hoàn thành 11/11/2023");
        setupProjectItem(findViewById(R.id.proj_6), "Tranh cái cốc", R.drawable.coc_nuoc, 100, true, "Hoàn thành 20/10/2022");
        
        // Hiện lại các ô nếu cần
        if (findViewById(R.id.proj_7) != null) {
            findViewById(R.id.proj_7).setVisibility(View.VISIBLE);
            setupProjectItem(findViewById(R.id.proj_7), "Phong cảnh 1", R.drawable.tp_trending_1, 100, true, "Hoàn thành 01/01/2022");
        }
        if (findViewById(R.id.proj_8) != null) {
            findViewById(R.id.proj_8).setVisibility(View.VISIBLE);
            setupProjectItem(findViewById(R.id.proj_8), "Phong cảnh 2", R.drawable.tp_trending_1, 100, true, "Hoàn thành 02/02/2022");
        }
    }

    private void setupProjectItem(View view, String name, int imageRes, int progress, boolean isDone, String doneDate) {
        if (view == null) return;
        view.setVisibility(View.VISIBLE);

        ImageView ivThumb = view.findViewById(R.id.iv_project_thumb);
        TextView tvName = view.findViewById(R.id.tv_project_name);
        View llProgress = view.findViewById(R.id.ll_progress_info);
        LinearProgressIndicator pbProgress = view.findViewById(R.id.pb_project_progress);
        TextView tvDoneDate = view.findViewById(R.id.tv_completed_date);
        ImageView ivBadge = view.findViewById(R.id.iv_done_badge);

        if (ivThumb != null) ivThumb.setImageResource(imageRes);
        if (tvName != null) tvName.setText(name);

        if (isDone) {
            if (llProgress != null) llProgress.setVisibility(View.GONE);
            if (tvDoneDate != null) {
                tvDoneDate.setVisibility(View.VISIBLE);
                tvDoneDate.setText(doneDate);
            }
            if (ivBadge != null) ivBadge.setVisibility(View.VISIBLE);
        } else {
            if (llProgress != null) llProgress.setVisibility(View.VISIBLE);
            if (tvDoneDate != null) tvDoneDate.setVisibility(View.GONE);
            if (ivBadge != null) ivBadge.setVisibility(View.GONE);
            if (pbProgress != null) pbProgress.setProgress(progress);
        }

        view.setOnClickListener(v -> {
            Intent intent;
            if (isDone) {
                // Nếu dự án đã hoàn thành -> Vào trang chi tiết tổng quan
                intent = new Intent(this, ProjectDetailActivity.class);
            } else {
                // Nếu dự án đang làm -> Vào trang checklist tiếp tục vẽ
                intent = new Intent(this, DoingProjectDetailActivity.class);
            }
            intent.putExtra("PROJECT_NAME", name);
            intent.putExtra("IS_DONE", isDone);
            startActivity(intent);
        });
    }
}
