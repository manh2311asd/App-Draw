package com.example.appdraw;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CommunityFragment extends Fragment {

    private boolean isLiked = false;
    private int likeCount = 1200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        LinearLayout postContainer = view.findViewById(R.id.ll_post_container);
        if (postContainer != null) {
            // Add a sample post
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

            // Click avatar/name to see profile -> Mở OtherUserProfileActivity (Ảnh 2)
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
}
