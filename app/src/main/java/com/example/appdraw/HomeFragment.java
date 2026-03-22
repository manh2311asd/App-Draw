package com.example.appdraw;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                // Điều hướng đến CalendarActivity làm nơi xem danh sách sự kiện
                Intent intent = new Intent(getActivity(), CalendarActivity.class);
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
        
        return view;
    }
}
