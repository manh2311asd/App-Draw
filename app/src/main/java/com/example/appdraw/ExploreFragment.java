package com.example.appdraw;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;

public class ExploreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // --- Navigation Tabs ---
        setupNavigationTabs(view);

        // --- Category Chips ---
        setupCategoryChips(view);

        // --- Filter and Sort ---
        View tvFilterStatus = view.findViewById(R.id.tv_filter_status);
        if (tvFilterStatus != null) {
            tvFilterStatus.setOnClickListener(v -> Toast.makeText(getContext(), "Xóa bộ lọc", Toast.LENGTH_SHORT).show());
        }
        View tvSortNewest = view.findViewById(R.id.tv_sort_newest);
        if (tvSortNewest != null) {
            tvSortNewest.setOnClickListener(v -> Toast.makeText(getContext(), "Sắp xếp: Mới nhất", Toast.LENGTH_SHORT).show());
        }

        // --- Other existing logic ---
        View cardSearch = view.findViewById(R.id.card_search);
        if (cardSearch != null) {
            cardSearch.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            });
        }

        ImageView ivFilter = view.findViewById(R.id.iv_filter_explore);
        if (ivFilter != null) {
            ivFilter.setOnClickListener(v -> showFilterDialog());
        }

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

    private void setupNavigationTabs(View view) {
        TextView tvExplore = view.findViewById(R.id.tv_nav_explore);
        TextView tvWorks = view.findViewById(R.id.tv_nav_works);
        TextView tvProgress = view.findViewById(R.id.tv_nav_progress);
        TextView tvTips = view.findViewById(R.id.tv_nav_tips);

        View.OnClickListener tabListener = v -> {
            String text = ((TextView) v).getText().toString();
            Toast.makeText(getContext(), "Chọn: " + text, Toast.LENGTH_SHORT).show();
            updateTabSelection((TextView) v, tvExplore, tvWorks, tvProgress, tvTips);
        };

        if (tvExplore != null) {
            tvExplore.setClickable(true);
            tvExplore.setFocusable(true);
            tvExplore.setOnClickListener(tabListener);
        }
        if (tvWorks != null) {
            tvWorks.setClickable(true);
            tvWorks.setFocusable(true);
            tvWorks.setOnClickListener(tabListener);
        }
        if (tvProgress != null) {
            tvProgress.setClickable(true);
            tvProgress.setFocusable(true);
            tvProgress.setOnClickListener(tabListener);
        }
        if (tvTips != null) {
            tvTips.setClickable(true);
            tvTips.setFocusable(true);
            tvTips.setOnClickListener(tabListener);
        }
    }

    private void updateTabSelection(TextView selected, TextView... others) {
        int blueColor = ContextCompat.getColor(requireContext(), R.color.primary_blue);
        int grayColor = ContextCompat.getColor(requireContext(), R.color.text_gray);

        for (TextView tab : others) {
            if (tab == null) continue;
            if (tab == selected) {
                tab.setTextColor(blueColor);
                tab.setTypeface(null, android.graphics.Typeface.BOLD);
                tab.setBackgroundResource(R.drawable.bg_chip_selected);
            } else {
                tab.setTextColor(grayColor);
                tab.setTypeface(null, android.graphics.Typeface.NORMAL);
                tab.setBackground(null);
            }
        }
    }

    private void setupCategoryChips(View view) {
        View chipTopic = view.findViewById(R.id.chip_topic);
        View chipWatercolor = view.findViewById(R.id.chip_watercolor);
        View chipSketch = view.findViewById(R.id.chip_sketch);

        if (chipTopic != null) chipTopic.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Chủ đề", Toast.LENGTH_SHORT).show());
        if (chipWatercolor != null) chipWatercolor.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Màu nước", Toast.LENGTH_SHORT).show());
        if (chipSketch != null) chipSketch.setOnClickListener(v -> Toast.makeText(getContext(), "Chọn Phác thảo", Toast.LENGTH_SHORT).show());
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
            "Nghệ sĩ Hoàng Lam là một trong những họa sĩ màu nước hàng đầu với hơn 10 năm kinh nghiệm. Anh nổi tiếng with phong cách vẽ thiên nhiên đầy sống động và cảm xúc.");
        
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

    private void showFilterDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }
}
