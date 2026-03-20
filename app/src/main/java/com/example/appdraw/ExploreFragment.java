package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class ExploreFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

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

        setupDummyData(view);

        return view;
    }

    private void setupDummyData(View view) {
        // Topic 1
        View topic1 = view.findViewById(R.id.topic_1);
        if (topic1 != null) {
            ((ImageView) topic1.findViewById(R.id.iv_category)).setImageResource(R.drawable.ve_hoa_mau_nuoc);
            ((TextView) topic1.findViewById(R.id.tv_category_name)).setText("Dành cho người mới bắt đầu");
            ((TextView) topic1.findViewById(R.id.tv_category_count)).setText("7 bài học");
            
            topic1.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                startActivity(intent);
            });
        }

        // Topic 2
        View topic2 = view.findViewById(R.id.topic_2);
        if (topic2 != null) {
            ((ImageView) topic2.findViewById(R.id.iv_category)).setImageResource(R.drawable.img_origami_art);
            ((TextView) topic2.findViewById(R.id.tv_category_name)).setText("Nghệ thuật gấp giấy Origami");
            ((TextView) topic2.findViewById(R.id.tv_category_count)).setText("8 bài học");
        }

        // Topic 3
        View topic3 = view.findViewById(R.id.topic_3);
        if (topic3 != null) {
            ((ImageView) topic3.findViewById(R.id.iv_category)).setImageResource(R.drawable.ve_thien_nhien);
            ((TextView) topic3.findViewById(R.id.tv_category_name)).setText("Vẽ thiên nhiên");
            ((TextView) topic3.findViewById(R.id.tv_category_count)).setText("15 bài học");
        }

        // Artist 1
        View artist1 = view.findViewById(R.id.artist_1);
        if (artist1 != null) {
            ((ImageView) artist1.findViewById(R.id.iv_artist)).setImageResource(R.drawable.nghe_si_hoang_lam);
            ((TextView) artist1.findViewById(R.id.tv_artist_name)).setText("Hoàng Lam");
        }

        // Artist 2
        View artist2 = view.findViewById(R.id.artist_2);
        if (artist2 != null) {
            ((ImageView) artist2.findViewById(R.id.iv_artist)).setImageResource(R.drawable.nghe_si_lien_ninh);
            ((TextView) artist2.findViewById(R.id.tv_artist_name)).setText("Liên Ninh");
        }

        // Artist 3
        View artist3 = view.findViewById(R.id.artist_3);
        if (artist3 != null) {
            ((ImageView) artist3.findViewById(R.id.iv_artist)).setImageResource(R.drawable.nghe_si_donal);
            ((TextView) artist3.findViewById(R.id.tv_artist_name)).setText("Donal");
        }

        // Trending 1
        View trending1 = view.findViewById(R.id.trending_1);
        if (trending1 != null) {
            ((ImageView) trending1.findViewById(R.id.iv_trending)).setImageResource(R.drawable.tp_trending_1);
            ((TextView) trending1.findViewById(R.id.tv_trending_title)).setText("Hoàng hôn trên biển");
            ((TextView) trending1.findViewById(R.id.tv_trending_author)).setText("Bởi Hải Nam");
        }

        // Trending 2
        View trending2 = view.findViewById(R.id.trending_2);
        if (trending2 != null) {
            ((ImageView) trending2.findViewById(R.id.iv_trending)).setImageResource(R.drawable.tp_trending_2);
            ((TextView) trending2.findViewById(R.id.tv_trending_title)).setText("Mèo con say ngủ");
            ((TextView) trending2.findViewById(R.id.tv_trending_author)).setText("Bởi Thu Thủy");
        }
    }

    private void showFilterDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter, null);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }
}
