package com.example.appdraw.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdraw.R;
import com.example.appdraw.drawing.DrawingActivity;
import com.example.appdraw.model.Artwork;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProjectDetailActivity extends AppCompatActivity {
    private String projectId;
    private String projectName;

    private RecyclerView rvArtworks;
    private ArtworkAdapter adapter;
    private List<Artwork> artworkList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        db = FirebaseFirestore.getInstance();
        projectId = getIntent().getStringExtra("PROJECT_ID");
        projectName = getIntent().getStringExtra("PROJECT_NAME");
        String projectDesc = getIntent().getStringExtra("PROJECT_DESC");
        String projectCover = getIntent().getStringExtra("PROJECT_COVER");

        Toolbar toolbar = findViewById(R.id.toolbar_project_detail);
        TextView tvTitleToolbar = findViewById(R.id.tv_project_title_toolbar);
        TextView tvTitleExpanded = findViewById(R.id.tv_project_title_expanded);
        TextView tvDescExpanded = findViewById(R.id.tv_project_desc_expanded);
        android.widget.ImageView ivCover = findViewById(R.id.iv_project_cover_detail);
        com.google.android.material.appbar.AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        
        if (projectName != null) {
            tvTitleToolbar.setText(projectName);
            if (tvTitleExpanded != null) tvTitleExpanded.setText(projectName);
        }

        if (projectDesc != null && !projectDesc.isEmpty() && tvDescExpanded != null) {
            tvDescExpanded.setText(projectDesc);
        } else if (tvDescExpanded != null) {
            tvDescExpanded.setVisibility(android.view.View.GONE);
        }

        if (projectCover != null && !projectCover.isEmpty() && ivCover != null) {
            if (projectCover.startsWith("data:image")) {
                byte[] b = android.util.Base64.decode(projectCover.split(",")[1], android.util.Base64.DEFAULT);
                com.bumptech.glide.Glide.with(this).load(b).centerCrop().into(ivCover);
            } else {
                com.bumptech.glide.Glide.with(this).load(projectCover).centerCrop().into(ivCover);
            }
        }

        // Fade in/out toolbar title based on scroll
        if (appBarLayout != null && tvTitleToolbar != null) {
            appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
                if (Math.abs(verticalOffset) >= appBarLayout1.getTotalScrollRange() - 50) {
                    tvTitleToolbar.setAlpha(1.0f);
                } else {
                    tvTitleToolbar.setAlpha(0.0f);
                }
            });
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvArtworks = findViewById(R.id.rv_artworks);
        rvArtworks.setLayoutManager(new androidx.recyclerview.widget.StaggeredGridLayoutManager(2, androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL));
        adapter = new ArtworkAdapter(artworkList, artwork -> {
            // Mở DrawingActivity
            Intent intent = new Intent(this, DrawingActivity.class);
            intent.putExtra("ARTWORK_ID", artwork.getId());
            intent.putExtra("PROJECT_ID", projectId);
            intent.putExtra("STATUS", artwork.getStatus());
            startActivity(intent);
        });
        rvArtworks.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_artwork);
        fab.setOnClickListener(v -> createNewArtwork());

        loadArtworks();
    }

    private void loadArtworks() {
        if (projectId == null) return;
        db.collection("Artworks")
                .whereEqualTo("projectId", projectId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải ảnh: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    artworkList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Artwork a = doc.toObject(Artwork.class);
                            artworkList.add(a);
                        }
                        
                        // Sắp xếp local
                        java.util.Collections.sort(artworkList, (a1, a2) -> Long.compare(a2.getCreatedAt(), a1.getCreatedAt()));

                        // Tự động đồng bộ số lượng tác phẩm thật ngoài bìa dự án (Self-healing count)
                        db.collection("Projects").document(projectId).update("artworkCount", artworkList.size());
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void createNewArtwork() {
        if (projectId == null) return;
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        String docId = db.collection("Artworks").document().getId();
        Artwork newArtwork = new Artwork(docId, uid, projectId, "Trang trắng", "", Artwork.STATUS_DRAFT, System.currentTimeMillis());

        db.collection("Artworks").document(docId).set(newArtwork)
                .addOnSuccessListener(aVoid -> {
                    db.collection("Projects").document(projectId).update("artworkCount", com.google.firebase.firestore.FieldValue.increment(1));
                    // Mở luôn trang vẽ
                    Intent intent = new Intent(this, DrawingActivity.class);
                    intent.putExtra("ARTWORK_ID", docId);
                    intent.putExtra("PROJECT_ID", projectId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tạo bản vẽ", Toast.LENGTH_SHORT).show();
                });
    }
}
