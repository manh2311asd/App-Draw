package com.example.appdraw.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.Map;

import com.example.appdraw.NotificationsActivity;
import com.example.appdraw.ProfileActivity;
import com.example.appdraw.R;
import com.example.appdraw.challenge.ChallengeActivity;
import com.example.appdraw.challenge.ChallengeDetailActivity;
import com.example.appdraw.community.EventScheduleActivity;
import com.example.appdraw.drawing.DrawingActivity;
import com.example.appdraw.explore.LessonDetailActivity;
import com.example.appdraw.explore.LessonListActivity;
import com.example.appdraw.explore.SearchActivity;
import com.example.appdraw.model.Event;
import com.example.appdraw.model.EventTicket;
import com.google.android.material.button.MaterialButton;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class HomeFragment extends Fragment {

    private ListenerRegistration challengeListenerReg;
    private boolean reseedAttempted = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Fetch User Role and Profile from Firestore ---
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        View layoutBadgeMentor = view.findViewById(R.id.layout_badge_mentor);
        ImageView ivAvatarHome = view.findViewById(R.id.iv_avatar_home);

        ImageView btnAddChallenge = view.findViewById(R.id.btn_add_challenge);
        if (btnAddChallenge != null) {
            btnAddChallenge.setVisibility(View.GONE); // Hide completely
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance().collection("Users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> profile = (Map<String, Object>) documentSnapshot.get("profile");
                            if (profile != null) {
                                if (profile.containsKey("fullName")) {
                                    String name = (String) profile.get("fullName");
                                    String shortName = name;
                                    if (name.contains(" ")) {
                                        shortName = name.substring(name.lastIndexOf(" ") + 1);
                                    }
                                    if (tvGreeting != null)
                                        tvGreeting.setText("Chào " + shortName + "!");
                                }
                                if (profile.containsKey("avatarUrl")) {
                                    String avatarUrl = (String) profile.get("avatarUrl");
                                    if (ivAvatarHome != null && getContext() != null) {
                                        ivAvatarHome.setPadding(0, 0, 0, 0);
                                        if (avatarUrl != null && !avatarUrl.isEmpty()
                                                && avatarUrl.startsWith("data:image")) {
                                            byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1],
                                                    android.util.Base64.DEFAULT);
                                            Glide.with(getContext()).load(b).circleCrop().into(ivAvatarHome);
                                        } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                            Glide.with(getContext()).load(avatarUrl).circleCrop().into(ivAvatarHome);
                                        } else {
                                            Glide.with(getContext()).load(R.drawable.ic_default_user).circleCrop()
                                                    .into(ivAvatarHome);
                                        }
                                    }
                                } else if (ivAvatarHome != null && getContext() != null) {
                                    ivAvatarHome.setPadding(0, 0, 0, 0);
                                    Glide.with(getContext()).load(R.drawable.ic_default_user).circleCrop()
                                            .into(ivAvatarHome);
                                }
                            }

                            String role = documentSnapshot.getString("role");
                            if ("mentor".equals(role)) {
                                if (layoutBadgeMentor != null)
                                    layoutBadgeMentor.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // User deleted from DB -> logout
                            Toast.makeText(getContext(), "Tài khoản của bạn không tồn tại hoặc đã bị xóa.",
                                    Toast.LENGTH_LONG).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getActivity(),
                                    com.example.appdraw.auth.LoginOptionsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            if (getActivity() != null)
                                getActivity().finish();
                        }
                    });
        }

        // Nút thông báo + badge chấm đỏ
        View btnNotifications = view.findViewById(R.id.btn_notifications);
        View notificationBadge = view.findViewById(R.id.notification_badge);

        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> {
                // Ẩn badge khi mở trang thông báo
                if (notificationBadge != null)
                    notificationBadge.setVisibility(View.GONE);
                Intent intent = new Intent(getActivity(), NotificationsActivity.class);
                startActivity(intent);
            });
        }

        // Lắng nghe real-time thông báo chưa đọc để hiện chấm đỏ
        if (user != null && notificationBadge != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Notifications")
                    .whereEqualTo("userId", user.getUid())
                    .addSnapshotListener((snapshots, err) -> {
                        if (err != null || snapshots == null)
                            return;
                        if (getView() == null)
                            return;
                        // Lọc client-side để tránh cần composite index
                        boolean hasUnread = false;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : snapshots) {
                            Boolean isRead = doc.getBoolean("isRead");
                            if (isRead == null || !isRead) {
                                hasUnread = true;
                                break;
                            }
                        }
                        View badge = getView().findViewById(R.id.notification_badge);
                        if (badge != null) {
                            badge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                        }
                    });
        }

        // Nút Livestream
        View btnLivestream = view.findViewById(R.id.btn_livestream);
        if (btnLivestream != null) {
            btnLivestream.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), com.example.appdraw.live.LiveListActivity.class);
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
                Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
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

        // --- Thử Thách ---
        setupChallenges(view);

        // --- Bài Học Yêu Thích ---
        setupSavedLessons(view);

        // --- Sự kiện sắp tới ---
        setupHomeEvents(view);

        // Xem tất cả sự kiện
        View tvViewAllEvents = view.findViewById(R.id.tv_view_all_events);
        if (tvViewAllEvents != null) {
            tvViewAllEvents.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Chỉ reload sự kiện (dùng get() một lần), không gọi lại setupChallenges
        // vì nó dùng addSnapshotListener và sẽ tích lũy listener nếu gọi nhiều lần
        if (getView() != null) {
            setupHomeEvents(getView());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Hủy listener khi fragment bị destroy để tránh memory leak
        if (challengeListenerReg != null) {
            challengeListenerReg.remove();
            challengeListenerReg = null;
        }
    }

    private void setupSuggestedLessons(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_suggested_lessons_container);
        if (container == null)
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (getContext() == null)
            return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        container.removeAllViews();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        java.util.List<String> suggestedTitles = java.util.Arrays.asList(
                "Làm quen với Brush", "Đêm trăng sáng trên đồi", "Palette pha màu cơ bản",
                "Phác thảo khuôn mặt Chibi", "Core tỷ lệ khuôn mặt");
        db.collection("Lessons").whereIn("title", suggestedTitles).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (String targetTitle : suggestedTitles) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            if (targetTitle.equals(doc.getString("title"))) {
                                String title = doc.getString("title");
                                String author = doc.getString("author");
                                if (author == null)
                                    author = doc.getString("authorName");
                                String imageResStr = doc.getString("imageRes");
                                String imageUrl = doc.getString("imageUrl");
                                String category = doc.getString("category");

                                if ("Đêm trăng sáng trên đồi".equals(title)) {
                                    imageResStr = "dem_trang_sang_tren_doi";
                                    author = "Thu Thủy";
                                } else if ("Phác thảo khuôn mặt Chibi".equals(title)) {
                                    imageResStr = "phac_thao_khuon_mat_chibi";
                                    author = "Minh Khang";
                                } else if ("Core tỷ lệ khuôn mặt".equals(title)) {
                                    imageResStr = "core_ty_le_khuon_mat";
                                    author = "Hương Lan";
                                } else if ("Palette pha màu cơ bản".equals(title)) {
                                    imageResStr = "palette_pha_mau_co_ban";
                                    author = "Tuấn Vũ";
                                } else if ("Làm quen với Brush".equals(title)) {
                                    imageResStr = "lam_quen_voi_brush";
                                    author = "Phong Artist";
                                }

                                View lessonView = inflater.inflate(R.layout.item_lesson_preview, container, false);

                                TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
                                TextView tvAuthor = lessonView.findViewById(R.id.tv_lesson_author);
                                ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
                                TextView tvStatus = lessonView.findViewById(R.id.tv_lesson_status);
                                TextView tvDuration = lessonView.findViewById(R.id.tv_duration);

                                if (tvTitle != null)
                                    tvTitle.setText(title);
                                if (tvAuthor != null) {
                                    if (author != null && !author.toLowerCase().startsWith("bởi")) {
                                        tvAuthor.setText("Bởi " + author);
                                    } else {
                                        tvAuthor.setText(author);
                                    }
                                }

                                if (ivThumb != null) {
                                    if (imageResStr != null && !imageResStr.isEmpty()
                                            && !imageResStr.matches("-?\\d+")) {
                                        try {
                                            int resId = getResources().getIdentifier(imageResStr, "drawable",
                                                    getContext().getPackageName());
                                            if (resId != 0)
                                                ivThumb.setImageResource(resId);
                                        } catch (Exception e) {
                                        }
                                    } else if (imageUrl != null && !imageUrl.isEmpty()) {
                                        Glide.with(this).load(imageUrl).centerCrop().into(ivThumb);
                                    }
                                }

                                tvStatus.setText("Chưa học");
                                tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                                tvStatus.setTextColor(Color.parseColor("#808080"));

                                android.widget.RatingBar rb = lessonView.findViewById(R.id.rating_bar);
                                if (rb != null) {
                                    rb.setRating(4.5f);
                                }

                                String inferredCategory = category;
                                if (inferredCategory == null) {
                                    if ("Làm quen với Brush".equals(title)) {
                                        inferredCategory = "mới bắt đầu";
                                    } else if ("Đêm trăng sáng trên đồi".equals(title)
                                            || "Palette pha màu cơ bản".equals(title)) {
                                        inferredCategory = "thiên nhiên";
                                    } else {
                                        inferredCategory = "manga";
                                    }
                                }

                                if (tvDuration != null) {
                                    String catCheck = inferredCategory.toLowerCase();
                                    if (catCheck.contains("mới bắt đầu") || catCheck.contains("beginner")) {
                                        tvDuration.setText("20 min");
                                    } else if (catCheck.contains("thiên nhiên") || catCheck.contains("màu nước")) {
                                        tvDuration.setText("45 min");
                                    } else {
                                        tvDuration.setText("60 min");
                                    }
                                }

                                if (uid != null && title != null) {
                                    db.collection("Users").document(uid).collection("lessonProgress").document(title)
                                            .get().addOnSuccessListener(progDoc -> {
                                                if (progDoc.exists()) {
                                                    String status = progDoc.getString("status");
                                                    if ("COMPLETED".equals(status)) {
                                                        tvStatus.setText("Hoàn thành");
                                                        tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                                                        tvStatus.setTextColor(Color.WHITE);
                                                    } else if ("IN_PROGRESS".equals(status)
                                                            || "WAITING_FOR_HOMEWORK".equals(status)) {
                                                        tvStatus.setText("Đang học");
                                                        tvStatus.setBackgroundResource(R.drawable.bg_badge_in_progress);
                                                        tvStatus.setTextColor(Color.WHITE);
                                                    }
                                                }
                                            });
                                }

                                final String finalAuthor = author;
                                final String finalImageResStr = imageResStr;
                                lessonView.setOnClickListener(v -> {
                                    if ("Hoàn thành".equals(tvStatus.getText().toString())) {
                                        Intent intent = new Intent(getActivity(),
                                                com.example.appdraw.explore.MySubmissionActivity.class);
                                        intent.putExtra("LESSON_TITLE", title);
                                        startActivity(intent);
                                    } else {
                                        Intent intent = new Intent(getActivity(), LessonDetailActivity.class);
                                        intent.putExtra("LESSON_TITLE", title);
                                        intent.putExtra("CATEGORY", category);
                                        intent.putExtra("IMAGE_RES", finalImageResStr);
                                        intent.putExtra("AUTHOR", finalAuthor);
                                        intent.putExtra("LESSON_ID", doc.getId());
                                        startActivity(intent);
                                    }
                                });

                                container.addView(lessonView);
                                break;
                            }
                        }
                    }
                });
    }

    private void setupSavedLessons(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_saved_lessons_container);
        android.widget.LinearLayout header = view.findViewById(R.id.ll_saved_lessons_header);
        android.widget.HorizontalScrollView hsv = view.findViewById(R.id.hsv_saved_lessons);

        if (container == null || header == null || hsv == null)
            return;

        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
            return;
        String uid = auth.getCurrentUser().getUid();

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Users").document(uid).collection("savedLessons")
                .orderBy("savedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null || queryDocumentSnapshots == null)
                        return;

                    if (queryDocumentSnapshots.isEmpty()) {
                        header.setVisibility(View.GONE);
                        hsv.setVisibility(View.GONE);
                        return;
                    }

                    header.setVisibility(View.VISIBLE);
                    hsv.setVisibility(View.VISIBLE);

                    if (getContext() == null)
                        return;
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    container.removeAllViews();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String imageResStr = doc.getString("imageRes");
                        String author = doc.getString("author");

                        if (author == null || author.isEmpty()) {
                            author = "Phong Artist"; // default
                        }

                        View lessonView = inflater.inflate(R.layout.item_lesson_preview, container, false);
                        TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
                        TextView tvAuthor = lessonView.findViewById(R.id.tv_lesson_author);
                        ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
                        TextView tvStatus = lessonView.findViewById(R.id.tv_lesson_status);
                        TextView tvDuration = lessonView.findViewById(R.id.tv_duration);
                        android.widget.RatingBar rb = lessonView.findViewById(R.id.rating_bar);

                        if (tvTitle != null && title != null)
                            tvTitle.setText(title);
                        if (tvAuthor != null)
                            tvAuthor.setText(author);
                        if (rb != null)
                            rb.setRating(5.0f);
                        if (tvStatus != null) {
                            tvStatus.setText("Đã lưu");
                            tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                            tvStatus.setTextColor(android.graphics.Color.parseColor("#808080"));
                        }

                        if (tvDuration != null) {
                            tvDuration.setText("♥");
                        }

                        if (ivThumb != null && imageResStr != null && !imageResStr.isEmpty()) {
                            try {
                                int resId = getResources().getIdentifier(imageResStr, "drawable",
                                        getContext().getPackageName());
                                if (resId != 0)
                                    ivThumb.setImageResource(resId);
                            } catch (Exception ex) {
                            }
                        }

                        lessonView.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), LessonDetailActivity.class);
                            intent.putExtra("LESSON_TITLE", title);
                            intent.putExtra("CATEGORY", category);
                            intent.putExtra("IMAGE_RES", imageResStr);
                            intent.putExtra("LESSON_ID", doc.getId());
                            startActivity(intent);
                        });

                        container.addView(lessonView);
                    }
                });
    }

    private void setupHomeEvents(View view) {
        RecyclerView rvMySchedule = view.findViewById(R.id.rv_home_my_schedule);
        RecyclerView rvExploreEvents = view.findViewById(R.id.rv_home_explore_events);
        TextView tvEmptySchedule = view.findViewById(R.id.tv_empty_schedule);

        if (rvMySchedule == null || rvExploreEvents == null)
            return;

        rvMySchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        rvExploreEvents.setVisibility(View.GONE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user == null)
            return;

        rvMySchedule.setVisibility(View.VISIBLE);
        db.collection("EventRegistrations")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(ticketDocs -> {
                    List<EventTicket> myTickets = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : ticketDocs) {
                        EventTicket t = doc.toObject(EventTicket.class);
                        if (t != null)
                            myTickets.add(t);
                    }

                    db.collection("Events").get().addOnSuccessListener(eventDocs -> {
                        List<Event> upcomingEvents = new ArrayList<>();
                        long now = System.currentTimeMillis() - 24 * 60 * 60 * 1000L;
                        for (com.google.firebase.firestore.DocumentSnapshot doc : eventDocs) {
                            Event e = doc.toObject(Event.class);
                            if (e != null && e.getDateMillis() >= now) {
                                boolean hasTicket = false;
                                for (EventTicket t : myTickets) {
                                    if (t.getEventId().equals(e.getId())) {
                                        hasTicket = true;
                                        break;
                                    }
                                }
                                boolean isAuthor = e.getAuthorId() != null && e.getAuthorId().equals(user.getUid());
                                if (hasTicket || isAuthor || "Live".equals(e.getEventType())) {
                                    upcomingEvents.add(e);
                                }
                            }
                        }

                        java.util.Collections.sort(upcomingEvents,
                                (e1, e2) -> Long.compare(e1.getDateMillis(), e2.getDateMillis()));

                        if (upcomingEvents.isEmpty()) {
                            tvEmptySchedule.setText("Bạn chưa có lịch học nào. Nhấn Xem lịch để khám phá!");
                            tvEmptySchedule.setVisibility(View.VISIBLE);
                            rvMySchedule.setVisibility(View.GONE);
                        } else {
                            tvEmptySchedule.setVisibility(View.GONE);
                            rvMySchedule.setVisibility(View.VISIBLE);
                            int limit = Math.min(3, upcomingEvents.size());
                            List<Event> displayEvents = upcomingEvents.subList(0, limit);
                            rvMySchedule.setAdapter(new HomeScheduleAdapter(displayEvents, myTickets));
                        }
                    });
                });
    }

    private class HomeScheduleAdapter extends RecyclerView.Adapter<HomeScheduleAdapter.VH> {
        List<Event> list;
        List<EventTicket> tickets;

        HomeScheduleAdapter(List<Event> l, List<EventTicket> myT) {
            list = l;
            tickets = myT;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_schedule, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Event e = list.get(position);
            holder.tvTitle.setText(e.getTitle());

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(e.getDateMillis());
            holder.tvTime.setText(e.getStartTime() + " - " + cal.get(java.util.Calendar.DAY_OF_MONTH) + "/"
                    + (cal.get(java.util.Calendar.MONTH) + 1));

            FirebaseFirestore.getInstance().collection("Users").document(e.getAuthorId())
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists() && holder.tvSubtitle != null) {
                            java.util.Map<String, Object> profile = (java.util.Map<String, Object>) doc.get("profile");
                            String fullName = "Người ẩn danh";
                            if (profile != null && profile.containsKey("fullName")) {
                                fullName = (String) profile.get("fullName");
                            }
                            holder.tvSubtitle.setText(fullName + " - " + (e.isOnline() ? "Online" : "Offline"));
                        }
                    });

            if ("Live".equals(e.getEventType())) {
                holder.tvBadge.setText("Live");
                holder.tvBadge
                        .setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
                holder.btnAction.setText("Tham gia");
                holder.btnAction.setOnClickListener(
                        v -> Toast.makeText(getContext(), "Đang vào phòng Live...", Toast.LENGTH_SHORT).show());
            } else {
                holder.tvBadge.setText("Workshop");
                holder.tvBadge
                        .setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F57C00")));

                String uid = FirebaseAuth.getInstance().getUid();
                boolean isAuthor = uid != null && uid.equals(e.getAuthorId());

                if (isAuthor) {
                    holder.btnAction.setText("Xem");
                    holder.btnAction.setOnClickListener(
                            v -> Toast.makeText(getContext(), "Bạn là nhà tổ chức", Toast.LENGTH_SHORT).show());
                } else {
                    holder.btnAction.setText("Xem vé");
                    holder.btnAction.setOnClickListener(v -> {
                        String myTicketId = null;
                        for (EventTicket t : tickets) {
                            if (t.getEventId().equals(e.getId()))
                                myTicketId = t.getId();
                        }
                        Intent intent = new Intent(getActivity(),
                                com.example.appdraw.community.EventTicketActivity.class);
                        intent.putExtra("EVENT_ID", e.getId());
                        if (myTicketId != null)
                            intent.putExtra("TICKET_ID", myTicketId);
                        startActivity(intent);
                    });
                }
            }

            if (e.getCoverImageBase64() != null && e.getCoverImageBase64().startsWith("data:image")) {
                byte[] b = android.util.Base64.decode(e.getCoverImageBase64().split(",")[1],
                        android.util.Base64.DEFAULT);
                Glide.with(HomeFragment.this).load(b).centerCrop().into(holder.ivCover);
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle, tvTime, tvBadge, btnAction;
            com.google.android.material.imageview.ShapeableImageView ivCover;

            VH(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_event_title);
                tvSubtitle = itemView.findViewById(R.id.tv_event_subtitle);
                tvTime = itemView.findViewById(R.id.tv_event_time);
                tvBadge = itemView.findViewById(R.id.tv_event_badge);
                btnAction = itemView.findViewById(R.id.btn_event_action);
                ivCover = itemView.findViewById(R.id.iv_event_cover);
            }
        }
    }

    private void setupChallenges(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_challenges_container);
        if (container == null)
            return;

        // Hủy listener cũ nếu có, tránh tích lũy
        if (challengeListenerReg != null) {
            challengeListenerReg.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        challengeListenerReg = db.collection("Challenges").addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null || queryDocumentSnapshots == null)
                return;

            if (queryDocumentSnapshots.isEmpty()) {
                if (getContext() != null) {
                    container.removeAllViews();
                    TextView tvEmpty = new TextView(getContext());
                    tvEmpty.setText("Chưa có thử thách nào diễn ra.");
                    tvEmpty.setPadding(32, 32, 32, 32);
                    container.addView(tvEmpty);
                }
                return;
            } else {
                // Tự động quét và xoá 3 bài rác cũ khỏi Server
                for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                    String t = doc.getString("title");
                    if ("Vẽ cây ngày trái đất".equals(t) || "14 ngày ký họa phong cảnh".equals(t)
                            || "Thử thách Anime 30 ngày".equals(t)) {
                        doc.getReference().delete();
                    }
                }
            }

            if (getContext() == null)
                return;
            LayoutInflater inflater = LayoutInflater.from(getContext());
            container.removeAllViews();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

            int count = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                if (count >= 1)
                    break; // Only display 1 challenge dynamically

                String title = doc.getString("title");
                String author = doc.getString("author");
                String dateStr = doc.getString("dateStr");
                String participantsCount = doc.getString("participantsCount");
                String imageResStr = doc.getString("imageRes");
                String imageUrl = doc.getString("imageUrl");
                String rulesStr = doc.getString("rules");
                String rewardsStr = doc.getString("rewards");

                View cardView = inflater.inflate(R.layout.item_challenge_card, container, false);

                TextView tvTitle = cardView.findViewById(R.id.tv_challenge_title);
                TextView tvAuthor = cardView.findViewById(R.id.tv_challenge_author);
                TextView tvDate = cardView.findViewById(R.id.tv_challenge_date);
                TextView tvParticipants = cardView.findViewById(R.id.tv_participants_count);
                ImageView ivImage = cardView.findViewById(R.id.iv_challenge_image);
                MaterialButton btnJoin = cardView.findViewById(R.id.btnJoinChallenge);

                if (tvTitle != null)
                    tvTitle.setText("Thử thách: " + title);
                if (tvAuthor != null)
                    tvAuthor.setText(author);
                if (tvDate != null)
                    tvDate.setText(dateStr);
                if (tvParticipants != null)
                    tvParticipants.setText(participantsCount);

                if (ivImage != null) {
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        if (imageUrl.startsWith("data:image")) {
                            try {
                                String base64Data = imageUrl.substring(imageUrl.indexOf(",") + 1);
                                byte[] b = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT);
                                Glide.with(this).load(b).centerCrop().into(ivImage);
                            } catch (Exception e) {
                            }
                        } else {
                            Glide.with(this).load(imageUrl).centerCrop().into(ivImage);
                        }
                    } else if (imageResStr != null && !imageResStr.isEmpty()) {
                        try {
                            int resId = Integer.parseInt(imageResStr);
                            ivImage.setImageResource(resId);
                        } catch (Exception e) {
                            // imageResStr là tên drawable string
                            try {
                                int resId = getResources().getIdentifier(imageResStr, "drawable",
                                        requireContext().getPackageName());
                                if (resId != 0)
                                    ivImage.setImageResource(resId);
                            } catch (Exception ex) {
                            }
                        }
                    }
                }

                String authorId = doc.getString("authorId");

                // Check role and status locally
                if (uid != null) {
                    db.collection("Users").document(uid).get().addOnSuccessListener(userDoc -> {
                        String role = userDoc.getString("role");

                        String mentorName = "Mentor";
                        if (userDoc.exists()) {
                            java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc
                                    .get("profile");
                            if (profile != null && profile.containsKey("fullName")) {
                                mentorName = "Mentor: " + profile.get("fullName");
                            }
                        }

                        if ("mentor".equals(role)) {
                            if (btnJoin != null) {
                                boolean isAuthor = false;
                                if (uid.equals(authorId))
                                    isAuthor = true;
                                else if (authorId == null && author != null && author.equals(mentorName))
                                    isAuthor = true; // Fallback cho bài cũ

                                if (isAuthor) {
                                    btnJoin.setText("Quản lý");
                                    btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2D5A9E")));
                                } else {
                                    btnJoin.setText("Chấm điểm bài");
                                    btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                                }
                            }
                        } else {
                            // Check if joined or submitted
                            db.collection("Users").document(uid).collection("joinedChallenges").document(title)
                                    .get().addOnSuccessListener(chalDoc -> {
                                        if (chalDoc.exists() && btnJoin != null) {
                                            String status = chalDoc.getString("status");
                                            if ("SUBMITTED".equals(status) || "GRADED".equals(status)) {
                                                btnJoin.setText("Đã nộp");
                                                btnJoin.setBackgroundTintList(
                                                        ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
                                            } else {
                                                btnJoin.setText("Tiếp tục");
                                                btnJoin.setBackgroundTintList(
                                                        ColorStateList.valueOf(Color.parseColor("#E67E22"))); // Orange
                                            }
                                        }
                                    });
                        }
                    });
                }

                if (btnJoin != null) {
                    btnJoin.setOnClickListener(v -> {
                        String currentText = btnJoin.getText().toString();
                        if ("Tham gia".equals(currentText) && uid != null) {
                            btnJoin.setEnabled(false); // Ngăn double-click

                            // Check if previously joined to prevent multiple increments
                            db.collection("Users").document(uid).collection("joinedChallenges").document(title)
                                    .get().addOnSuccessListener(chalDoc -> {
                                        if (!chalDoc.exists()) {
                                            java.util.Map<String, Object> joinData = new java.util.HashMap<>();
                                            joinData.put("status", "JOINED");
                                            db.collection("Users").document(uid).collection("joinedChallenges")
                                                    .document(title).set(joinData);

                                            // Increment global counter
                                            try {
                                                String countStr = participantsCount;
                                                if (countStr != null)
                                                    countStr = countStr.replaceAll("[^0-9]", "");
                                                int currentCount = (countStr == null || countStr.isEmpty()) ? 0
                                                        : Integer.parseInt(countStr);
                                                currentCount++;
                                                String newCountStr = currentCount + " đã tham gia";
                                                db.collection("Challenges").document(doc.getId())
                                                        .update("participantsCount", newCountStr);
                                                if (tvParticipants != null)
                                                    tvParticipants.setText(newCountStr);
                                            } catch (Exception e) {
                                            }

                                            Toast.makeText(getContext(), "Đã tham gia thử thách!", Toast.LENGTH_SHORT)
                                                    .show();
                                        }

                                        btnJoin.setEnabled(true);
                                        btnJoin.setText("Tiếp tục");
                                        btnJoin.setBackgroundTintList(
                                                ColorStateList.valueOf(Color.parseColor("#E67E22")));
                                    })
                                    .addOnFailureListener(e -> btnJoin.setEnabled(true));
                        } else {
                            Intent intent = new Intent(getActivity(), ChallengeDetailActivity.class);
                            intent.putExtra("CHALLENGE_TITLE", title);
                            intent.putExtra("CHALLENGE_IMAGE_URL", imageUrl);
                            intent.putExtra("CHALLENGE_RULES", rulesStr);
                            intent.putExtra("CHALLENGE_REWARDS", rewardsStr);
                            intent.putExtra("CHALLENGE_DEADLINE", dateStr);
                            startActivity(intent);
                        }
                    });
                }

                // Also make the whole card clickable
                cardView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ChallengeDetailActivity.class);
                    intent.putExtra("CHALLENGE_TITLE", title);
                    intent.putExtra("CHALLENGE_IMAGE_URL", imageUrl);
                    intent.putExtra("CHALLENGE_RULES", rulesStr);
                    intent.putExtra("CHALLENGE_REWARDS", rewardsStr);
                    intent.putExtra("CHALLENGE_DEADLINE", dateStr);
                    startActivity(intent);
                });

                container.addView(cardView);
                count++;
            }
        });
    }
}
