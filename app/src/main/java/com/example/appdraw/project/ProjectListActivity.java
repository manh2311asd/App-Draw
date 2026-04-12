package com.example.appdraw.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.appdraw.CreateProjectActivity;
import com.example.appdraw.R;
import com.example.appdraw.model.Project;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProjectListActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private List<Project> projectList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar_projects);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        FloatingActionButton fab = findViewById(R.id.fab_add_project);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                startActivity(new Intent(this, CreateProjectActivity.class));
            });
        }

        rvProjects = findViewById(R.id.rv_projects);
        if (rvProjects != null) {
            rvProjects.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
            adapter = new ProjectAdapter();
            rvProjects.setAdapter(adapter);
            loadProjects();
        }
    }

    private void loadProjects() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        db.collection("Projects").whereEqualTo("uid", uid)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Lỗi tải dự án: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                projectList.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot doc : value) {
                        try {
                            Project p = doc.toObject(Project.class);
                            projectList.add(p);
                        } catch (Exception ignored) {}
                    }
                    
                    // Sắp xếp local để tránh lỗi thiếu Index trên Firestore
                    java.util.Collections.sort(projectList, (p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                }
                adapter.notifyDataSetChanged();
            });
    }

    private class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Project project = projectList.get(position);
            holder.tvName.setText(project.getName());
            
            if (project.getDescription() != null && !project.getDescription().isEmpty()) {
                holder.tvDescription.setText(project.getDescription());
                holder.tvDescription.setVisibility(View.VISIBLE);
            } else {
                holder.tvDescription.setVisibility(View.GONE);
            }

            holder.tvArtworkCount.setText(project.getArtworkCount() + " tác phẩm");

            if (project.getCoverImageUrl() != null && !project.getCoverImageUrl().isEmpty()) {
                if (project.getCoverImageUrl().startsWith("data:image")) {
                    byte[] b = android.util.Base64.decode(project.getCoverImageUrl().split(",")[1], android.util.Base64.DEFAULT);
                    Glide.with(ProjectListActivity.this).load(b).centerCrop().into(holder.ivThumb);
                } else {
                    Glide.with(ProjectListActivity.this).load(project.getCoverImageUrl()).centerCrop().into(holder.ivThumb);
                }
            } else {
                // Background màu trơn tao nhã dựa trên tên dự án
                Glide.with(ProjectListActivity.this).clear(holder.ivThumb);
                String[] colorHexes = {"#4272D0", "#E75A7C", "#F0B259", "#70C1B3", "#2C363F", "#8E44AD", "#34495E", "#D35400"};
                int colorIndex = Math.abs((project.getName() != null ? project.getName() : "").hashCode()) % colorHexes.length;
                holder.ivThumb.setImageDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.parseColor(colorHexes[colorIndex])));
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ProjectListActivity.this, ProjectDetailActivity.class);
                intent.putExtra("PROJECT_ID", project.getId());
                intent.putExtra("PROJECT_NAME", project.getName());
                intent.putExtra("PROJECT_DESC", project.getDescription());
                intent.putExtra("PROJECT_COVER", project.getCoverImageUrl());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return projectList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivThumb;
            TextView tvName, tvDescription, tvArtworkCount;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivThumb = itemView.findViewById(R.id.iv_project_thumb);
                tvName = itemView.findViewById(R.id.tv_project_name);
                tvDescription = itemView.findViewById(R.id.tv_project_description);
                tvArtworkCount = itemView.findViewById(R.id.tv_artwork_count);
            }
        }
    }
}
