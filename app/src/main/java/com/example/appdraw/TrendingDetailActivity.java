package com.example.appdraw;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TrendingDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Toolbar toolbar = findViewById(R.id.toolbar_post_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String title = getIntent().getStringExtra("TITLE");
        TextView tvToolbarTitle = findViewById(R.id.tv_toolbar_title);
        if (tvToolbarTitle != null && title != null) {
            tvToolbarTitle.setText(title);
        }

        // Cập nhật dữ liệu bài viết dựa trên tiêu đề nhận được
        updatePostContent(title);
    }

    private void updatePostContent(String title) {
        if (title == null) return;

        TextView tvUserName = findViewById(R.id.tv_user_name);
        TextView tvPostContent = findViewById(R.id.tv_post_content);
        ImageView ivPostImage = findViewById(R.id.iv_post_image);
        TextView tvRelatedLesson = findViewById(R.id.tv_related_lesson);

        if ("Hoàng hôn trên biển".equals(title)) {
            if (tvUserName != null) tvUserName.setText("Hải Nam");
            if (tvPostContent != null) tvPostContent.setText("Một buổi chiều bình yên trên bãi biển #watercolor #sunset #ocean");
            if (ivPostImage != null) ivPostImage.setImageResource(R.drawable.tp_trending_1);
            if (tvRelatedLesson != null) tvRelatedLesson.setText("Xem bài học liên quan đến Vẽ phong cảnh biển →");
        } else if ("Mèo con say ngủ".equals(title)) {
            if (tvUserName != null) tvUserName.setText("Thu Thủy");
            if (tvPostContent != null) tvPostContent.setText("Chú mèo con lười biếng #cat #sketch #cute");
            if (ivPostImage != null) ivPostImage.setImageResource(R.drawable.tp_trending_2);
            if (tvRelatedLesson != null) tvRelatedLesson.setText("Xem bài học liên quan đến Vẽ động vật cơ bản →");
        }
    }
}
