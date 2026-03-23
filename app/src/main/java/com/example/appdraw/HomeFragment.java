package com.example.appdraw;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Nút thông báo
        View btnNotifications = view.findViewById(R.id.btn_notifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // Nút tìm kiếm
        View btnSearch = view.findViewById(R.id.btn_search);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            });
        }

        // Bắt đầu vẽ ngay
        View btnStartDrawing = view.findViewById(R.id.btnStartDrawingFragment);
        if (btnStartDrawing != null) {
            btnStartDrawing.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DrawingActivity.class);
                startActivity(intent);
            });
        }

        // Xem lịch
        View tvViewCalendar = view.findViewById(R.id.tv_view_calendar);
        if (tvViewCalendar != null) {
            tvViewCalendar.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CalendarActivity.class);
                startActivity(intent);
            });
        }

        // Tham gia Live
        View btnJoinLive = view.findViewById(R.id.btn_join_live);
        if (btnJoinLive != null) {
            btnJoinLive.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LiveStreamActivity.class);
                startActivity(intent);
            });
        }

        // Xem tất cả bài học
        View tvViewAllLessons = view.findViewById(R.id.tv_view_all_lessons);
        if (tvViewAllLessons != null) {
            tvViewAllLessons.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), LessonListActivity.class);
                startActivity(intent);
            });
        }

        // --- Bài học gợi ý ---
        setupSuggestedLessons(view);

        // Xem tất cả thử thách
        View tvViewAllChallenges = view.findViewById(R.id.tv_view_all_challenges);
        if (tvViewAllChallenges != null) {
            tvViewAllChallenges.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChallengeActivity.class);
                startActivity(intent);
            });
        }

        // Tham gia thử thách cụ thể
        View cardChallenge = view.findViewById(R.id.card_challenge_new);
        if (cardChallenge != null) {
            cardChallenge.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChallengeDetailActivity.class);
                intent.putExtra("CHALLENGE_TITLE", "Vẽ cây ngày trái đất");
                startActivity(intent);
            });
        }

        // --- Sự kiện sắp tới ---
        setupUpcomingEvents(view);

        // Xem tất cả sự kiện
        View tvViewAllEvents = view.findViewById(R.id.tv_view_all_events);
        if (tvViewAllEvents != null) {
            tvViewAllEvents.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EventListActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    private void setupSuggestedLessons(View view) {
        // Lesson 1
        View lesson1 = view.findViewById(R.id.suggested_lesson_1);
        if (lesson1 != null) {
            ((TextView) lesson1.findViewById(R.id.tv_lesson_title)).setText("Vẽ mèo dễ thương");
            ((TextView) lesson1.findViewById(R.id.tv_lesson_author)).setText("Bởi Linh Trần");
            TextView status1 = lesson1.findViewById(R.id.tv_lesson_status);
            status1.setText("Chưa học");
            status1.setBackgroundTintList(null); // Reset to default
            lesson1.setOnClickListener(v -> openLessonDetail("Vẽ mèo dễ thương", "Chưa học"));
        }

        // Lesson 2
        View lesson2 = view.findViewById(R.id.suggested_lesson_2);
        if (lesson2 != null) {
            ((TextView) lesson2.findViewById(R.id.tv_lesson_title)).setText("Phối màu nước");
            ((TextView) lesson2.findViewById(R.id.tv_lesson_author)).setText("Bởi Hoàng Lam");
            TextView status2 = lesson2.findViewById(R.id.tv_lesson_status);
            status2.setText("Đang học");
            status2.setBackgroundResource(R.drawable.rounded_bg_gray); // Giả sử dùng màu xám cho đang học
            status2.setTextColor(Color.parseColor("#666666"));
            lesson2.setOnClickListener(v -> openLessonDetail("Phối màu nước", "Đang học"));
        }

        // Lesson 3
        View lesson3 = view.findViewById(R.id.suggested_lesson_3);
        if (lesson3 != null) {
            ((TextView) lesson3.findViewById(R.id.tv_lesson_title)).setText("Kỹ thuật vẽ chì");
            ((TextView) lesson3.findViewById(R.id.tv_lesson_author)).setText("Bởi Donal");
            TextView status3 = lesson3.findViewById(R.id.tv_lesson_status);
            status3.setText("Hoàn thành");
            status3.setBackgroundResource(R.drawable.bg_badge_completed);
            status3.setTextColor(Color.WHITE);
            lesson3.setOnClickListener(v -> openLessonDetail("Kỹ thuật vẽ chì", "Hoàn thành"));
        }
    }

    private void openLessonDetail(String title, String status) {
        if ("Hoàn thành".equals(status)) {
            Toast.makeText(getContext(), "Bạn đã hoàn thành bài học: " + title, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getActivity(), LessonDetailActivity.class);
            intent.putExtra("LESSON_TITLE", title);
            startActivity(intent);
        }
    }

    private void setupUpcomingEvents(View view) {
        // Event 1
        View event1 = view.findViewById(R.id.layout_event_1);
        if (event1 != null) {
            MaterialButton btn1 = event1.findViewById(R.id.btn_register);
            btn1.setOnClickListener(v -> handleRegistration(btn1, "Vẽ mầm cây"));
        }

        // Event 2
        View event2 = view.findViewById(R.id.layout_event_2);
        if (event2 != null) {
            ((TextView) event2.findViewById(R.id.tv_event_title)).setText("Vẽ tĩnh vật");
            MaterialButton btn2 = event2.findViewById(R.id.btn_register);
            btn2.setOnClickListener(v -> handleRegistration(btn2, "Vẽ tĩnh vật"));
        }
    }

    private void handleRegistration(MaterialButton button, String eventTitle) {
        if (button.getText().toString().equals("Đã đăng ký")) {
            button.setText("Đăng ký");
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_blue)));
            Toast.makeText(getContext(), "Đã hủy đăng ký: " + eventTitle, Toast.LENGTH_SHORT).show();
        } else {
            button.setText("Đã đăng ký");
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2ECC71"))); // Màu xanh lá sáng lên
            Toast.makeText(getContext(), "Đăng ký thành công: " + eventTitle, Toast.LENGTH_SHORT).show();
        }
    }
}
