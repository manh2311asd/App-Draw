package com.example.appdraw.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.appdraw.R;

public class ExploreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // --- Search Bar ---
        View cardSearch = view.findViewById(R.id.card_search);
        if (cardSearch != null) {
            cardSearch.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            });
        }

        // --- Category Chips ---
        setupCategoryChips(view);

        // --- Banner ---
        View btnExploreNow = view.findViewById(R.id.btn_explore_now);
        if (btnExploreNow != null) {
            btnExploreNow.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                intent.putExtra("TITLE", "Khám phá màu nước");
                startActivity(intent);
            });
        }

        setupDummyData(view);

        return view;
    }

    private void setupCategoryChips(View view) {
        View chipTopic = view.findViewById(R.id.chip_topic);
        View chipTechnique = view.findViewById(R.id.chip_technique);
        View chipMaterials = view.findViewById(R.id.chip_materials);
        View chipLevel = view.findViewById(R.id.chip_level);

        if (chipTopic != null) chipTopic.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Chủ đề", Toast.LENGTH_SHORT).show());
        if (chipTechnique != null) chipTechnique.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Kỹ thuật", Toast.LENGTH_SHORT).show());
        if (chipMaterials != null) chipMaterials.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Vật liệu", Toast.LENGTH_SHORT).show());
        if (chipLevel != null) chipLevel.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Level", Toast.LENGTH_SHORT).show());
    }

    private void setupDummyData(View view) {
        // Topic 1
        View topic1 = view.findViewById(R.id.topic_1);
        if (topic1 != null) {
            String title = "Dành cho người mới bắt đầu";
            ((ImageView) topic1.findViewById(R.id.iv_category)).setImageResource(R.drawable.ve_hoa_mau_nuoc);
            ((TextView) topic1.findViewById(R.id.tv_category_name)).setText(title);
            ((TextView) topic1.findViewById(R.id.tv_category_count)).setText("7 bài học");
            
            topic1.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                intent.putExtra("TITLE", title);
                startActivity(intent);
            });
        }

        // Topic 2
        View topic2 = view.findViewById(R.id.topic_2);
        if (topic2 != null) {
            String title = "Nghệ thuật gấp giấy Origami";
            ((ImageView) topic2.findViewById(R.id.iv_category)).setImageResource(R.drawable.img_origami_art);
            ((TextView) topic2.findViewById(R.id.tv_category_name)).setText(title);
            ((TextView) topic2.findViewById(R.id.tv_category_count)).setText("8 bài học");
            
            topic2.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                intent.putExtra("TITLE", title);
                startActivity(intent);
            });
        }

        // Topic 3
        View topic3 = view.findViewById(R.id.topic_3);
        if (topic3 != null) {
            String title = "Vẽ thiên nhiên";
            ((ImageView) topic3.findViewById(R.id.iv_category)).setImageResource(R.drawable.ve_thien_nhien);
            ((TextView) topic3.findViewById(R.id.tv_category_name)).setText(title);
            ((TextView) topic3.findViewById(R.id.tv_category_count)).setText("15 bài học");
            
            topic3.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                intent.putExtra("TITLE", title);
                startActivity(intent);
            });
        }

        // --- Cấu hình Nghệ sĩ nổi bật ---
        setupArtist(view.findViewById(R.id.artist_1), "Hoàng Lam", R.drawable.nghe_si_hoang_lam, 
            "Nghệ sĩ Hoàng Lam là một trong những họa sĩ màu nước hàng đầu với hơn 10 năm kinh nghiệm. Anh nổi tiếng với phong cách vẽ thiên nhiên đầy sống động và cảm xúc.");
        
        setupArtist(view.findViewById(R.id.artist_2), "Liên Ninh", R.drawable.nghe_si_lien_ninh, 
            "Nghệ sĩ Liên Ninh chuyên về phong cách vẽ minh họa hoa lá và chân dung. Cô có cách phối màu nhẹ nhàng, thanh lịch, mang lại cảm giác bình yên.");
        
        setupArtist(view.findViewById(R.id.artist_3), "Donal", R.drawable.nghe_si_donal, 
            "Donal là một nghệ sĩ trẻ tài năng với phong cách vẽ tranh kỹ thuật số độc đáo. Anh thường xuyên chia sẻ các dự án sáng tạo và kỹ thuật vẽ hiện đại.");

        // Trending 1
        View trending1 = view.findViewById(R.id.trending_1);
        if (trending1 != null) {
            ((ImageView) trending1.findViewById(R.id.iv_trending)).setImageResource(R.drawable.tp_trending_1);
            ((TextView) trending1.findViewById(R.id.tv_trending_title)).setText("Hoàng hôn trên biển");
            ((TextView) trending1.findViewById(R.id.tv_trending_author)).setText("Bởi Hải Nam");
            
            trending1.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), TrendingDetailActivity.class);
                intent.putExtra("TITLE", "Hoàng hôn trên biển");
                startActivity(intent);
            });
        }

        // Trending 2
        View trending2 = view.findViewById(R.id.trending_2);
        if (trending2 != null) {
            ((ImageView) trending2.findViewById(R.id.iv_trending)).setImageResource(R.drawable.tp_trending_2);
            ((TextView) trending2.findViewById(R.id.tv_trending_title)).setText("Mèo con say ngủ");
            ((TextView) trending2.findViewById(R.id.tv_trending_author)).setText("Bởi Thu Thủy");

            trending2.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), TrendingDetailActivity.class);
                intent.putExtra("TITLE", "Mèo con say ngủ");
                startActivity(intent);
            });
        }
    }

    private void setupArtist(View artistView, String name, int imageRes, String bio) {
        if (artistView == null) return;
        
        ImageView ivArtist = artistView.findViewById(R.id.iv_artist);
        TextView tvName = artistView.findViewById(R.id.tv_artist_name);
        
        if (ivArtist != null) ivArtist.setImageResource(imageRes);
        if (tvName != null) tvName.setText(name);
        
        artistView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ArtistDetailActivity.class);
            intent.putExtra("ARTIST_NAME", name);
            intent.putExtra("ARTIST_IMAGE", imageRes);
            intent.putExtra("ARTIST_BIO", bio);
            startActivity(intent);
        });
    }
}
