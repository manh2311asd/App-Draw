package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CommunityFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        LinearLayout postContainer = view.findViewById(R.id.ll_post_container);
        if (postContainer != null) {
            // Add a sample post
            View postView = inflater.inflate(R.layout.item_post, postContainer, false);
            
            // Click to see detail
            postView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                startActivity(intent);
            });

            // Click avatar/name to see profile
            View userHeader = postView.findViewById(R.id.ll_user_header);
            if (userHeader != null) {
                userHeader.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    // Truyền thêm flag để ProfileActivity biết đây là xem hồ sơ người khác
                    intent.putExtra("IS_OTHER_USER", true);
                    intent.putExtra("USER_NAME", "Linh Trần");
                    startActivity(intent);
                });
            }

            postContainer.addView(postView);
        }

        return view;
    }
}
