package com.example.appdraw.community;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.appdraw.NotificationsActivity;
import com.example.appdraw.R;

public class CommunityFragment extends Fragment {

    private boolean isLiked = false;
    private int likeCount = 1200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        // --- Notifications ---
        View btnNotifications = view.findViewById(R.id.btn_notifications_community);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // --- Tabs ---
        setupTabs(view);

        // --- Category Chips ---
        setupCategoryChips(view);

        // --- Filter and Sort ---
        View tvFilterStatus = view.findViewById(R.id.tv_comm_filter_status);
        if (tvFilterStatus != null) {
            tvFilterStatus.setOnClickListener(v -> Toast.makeText(getContext(), "Xóa bộ lọc", Toast.LENGTH_SHORT).show());
        }
        View tvSort = view.findViewById(R.id.tv_comm_sort);
        if (tvSort != null) {
            tvSort.setOnClickListener(v -> Toast.makeText(getContext(), "Sắp xếp: Mới nhất", Toast.LENGTH_SHORT).show());
        }

        // --- Post Feed ---
        LinearLayout postContainer = view.findViewById(R.id.ll_post_container);
        if (postContainer != null) {
            View postView = inflater.inflate(R.layout.item_post, postContainer, false);
            
            // Xử lý nút Tim (Like)
            View llLike = postView.findViewById(R.id.ll_like);
            ImageView ivLike = postView.findViewById(R.id.iv_like);
            TextView tvLikeCount = postView.findViewById(R.id.tv_like_count);

            if (llLike != null && ivLike != null && tvLikeCount != null) {
                llLike.setOnClickListener(v -> {
                    if (!isLiked) {
                        isLiked = true;
                        likeCount++;
                        ivLike.setImageResource(R.drawable.ic_heart);
                        ivLike.setColorFilter(Color.parseColor("#E91E63")); 
                        tvLikeCount.setText(String.valueOf(likeCount));
                    } else {
                        isLiked = false;
                        likeCount--;
                        ivLike.setColorFilter(Color.parseColor("#888888"));
                        tvLikeCount.setText(String.valueOf(likeCount));
                    }
                });
            }

            // Click to see detail
            postView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                startActivity(intent);
            });

            // Click avatar/name to see profile
            View userHeader = postView.findViewById(R.id.ll_user_header);
            if (userHeader != null) {
                userHeader.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), OtherUserProfileActivity.class);
                    intent.putExtra("USER_NAME", "Linh Trần");
                    startActivity(intent);
                });
            }

            postContainer.addView(postView);
        }

        return view;
    }

    private void setupTabs(View view) {
        TextView tvExplore = view.findViewById(R.id.tv_comm_explore);
        TextView tvWorks = view.findViewById(R.id.tv_comm_works);
        TextView tvProgress = view.findViewById(R.id.tv_comm_progress);
        TextView tvTips = view.findViewById(R.id.tv_comm_tips);

        View.OnClickListener tabListener = v -> {
            String text = ((TextView) v).getText().toString();
            Toast.makeText(getContext(), "Chọn tab: " + text, Toast.LENGTH_SHORT).show();
            updateTabSelection((TextView) v, tvExplore, tvWorks, tvProgress, tvTips);
        };

        if (tvExplore != null) tvExplore.setOnClickListener(tabListener);
        if (tvWorks != null) tvWorks.setOnClickListener(tabListener);
        if (tvProgress != null) tvProgress.setOnClickListener(tabListener);
        if (tvTips != null) tvTips.setOnClickListener(tabListener);
    }

    private void updateTabSelection(TextView selected, TextView... others) {
        int blueColor = ContextCompat.getColor(requireContext(), R.color.primary_blue);
        int grayColor = ContextCompat.getColor(requireContext(), R.color.text_gray);

        for (TextView tab : others) {
            if (tab == null) continue;
            if (tab == selected) {
                tab.setTextColor(blueColor);
                tab.setTypeface(null, android.graphics.Typeface.BOLD);
                tab.setBackgroundResource(R.drawable.selected_tool_bg);
            } else {
                tab.setTextColor(grayColor);
                tab.setTypeface(null, android.graphics.Typeface.NORMAL);
                tab.setBackground(null);
            }
        }
    }

    private void setupCategoryChips(View view) {
        View chipTopic = view.findViewById(R.id.chip_comm_topic);
        View chipWatercolor = view.findViewById(R.id.chip_comm_watercolor);
        View chipSketch = view.findViewById(R.id.chip_comm_sketch);

        if (chipTopic != null) chipTopic.setOnClickListener(v -> Toast.makeText(getContext(), "Lọc Chủ đề", Toast.LENGTH_SHORT).show());
        if (chipWatercolor != null) chipWatercolor.setOnClickListener(v -> Toast.makeText(getContext(), "Lọc Màu nước", Toast.LENGTH_SHORT).show());
        if (chipSketch != null) chipSketch.setOnClickListener(v -> Toast.makeText(getContext(), "Lọc Phác thảo", Toast.LENGTH_SHORT).show());
    }
}
