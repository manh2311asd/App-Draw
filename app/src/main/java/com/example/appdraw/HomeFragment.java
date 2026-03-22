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

public class HomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        View btnStart = view.findViewById(R.id.btnStartDrawingFragment);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DrawingActivity.class);
                startActivity(intent);
            });
        }

        TextView tvViewAllLessons = view.findViewById(R.id.tv_view_all_lessons);
        if (tvViewAllLessons != null) {
            tvViewAllLessons.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                intent.putExtra("TITLE", "Tất cả bài học gợi ý");
                startActivity(intent);
            });
        }

        TextView tvViewAllChallenges = view.findViewById(R.id.tv_view_all_challenges);
        if (tvViewAllChallenges != null) {
            tvViewAllChallenges.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChallengeActivity.class);
                startActivity(intent);
            });
        }

        TextView tvViewAllEvents = view.findViewById(R.id.tv_view_all_events);
        if (tvViewAllEvents != null) {
            tvViewAllEvents.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EventListActivity.class);
                startActivity(intent);
            });
        }

        View btnNotifications = view.findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), NotificationsActivity.class);
                startActivity(intent);
            });
        }

        View btnSearch = view.findViewById(R.id.btn_search);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            });
        }

        View btnViewCalendar = view.findViewById(R.id.tv_view_calendar);
        if (btnViewCalendar != null) {
            btnViewCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CalendarActivity.class);
                startActivity(intent);
            });
        }

        // --- Cấu hình Bài học gợi ý có các bước học ---
        setupSuggestedLesson(view.findViewById(R.id.suggested_lesson_1), "Vẽ mèo dễ thương", R.drawable.tp_trending_2);
        setupSuggestedLesson(view.findViewById(R.id.suggested_lesson_2), "Phác thảo cơ bản", R.drawable.ve_hoa_mau_nuoc);
        setupSuggestedLesson(view.findViewById(R.id.suggested_lesson_3), "Tô màu phong cảnh", R.drawable.ve_thien_nhien);

        // 1. Thử thách chưa tham gia
        View cardChallengeNew = view.findViewById(R.id.card_challenge_new);
        if (cardChallengeNew != null) {
            cardChallengeNew.setOnClickListener(v -> openChallengeDetail("NEW"));
        }
        
        View btnJoinChallenge = view.findViewById(R.id.btnJoinChallenge);
        if (btnJoinChallenge != null) {
            btnJoinChallenge.setOnClickListener(v -> openChallengeDetail("NEW"));
        }

        return view;
    }

    private void setupSuggestedLesson(View lessonView, String title, int imageRes) {
        if (lessonView == null) return;
        
        TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
        ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
        
        if (tvTitle != null) tvTitle.setText(title);
        if (ivThumb != null) {
            ivThumb.setImageResource(imageRes);
            ivThumb.setAlpha(1.0f); // Bỏ màu xám mờ nếu có
            ivThumb.setColorFilter(null);
        }

        lessonView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LessonDetailActivity.class);
            intent.putExtra("LESSON_STATUS", "NOT_STARTED"); // Luôn có các bước học
            intent.putExtra("LESSON_TITLE", title);
            startActivity(intent);
        });
    }
    
    private void openChallengeDetail(String status) {
        Intent intent = new Intent(getActivity(), ChallengeDetailActivity.class);
        intent.putExtra("CHALLENGE_STATUS", status);
        startActivity(intent);
    }
}
