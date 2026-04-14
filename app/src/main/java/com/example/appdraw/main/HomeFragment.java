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
import java.util.Map;

import com.example.appdraw.LiveStreamActivity;
import com.example.appdraw.NotificationsActivity;
import com.example.appdraw.ProfileActivity;
import com.example.appdraw.R;
import com.example.appdraw.challenge.ChallengeActivity;
import com.example.appdraw.challenge.ChallengeDetailActivity;
import com.example.appdraw.drawing.DrawingActivity;
import com.example.appdraw.event.CalendarActivity;
import com.example.appdraw.event.EventListActivity;
import com.example.appdraw.explore.LessonDetailActivity;
import com.example.appdraw.explore.LessonListActivity;
import com.example.appdraw.explore.SearchActivity;
import com.google.android.material.button.MaterialButton;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                                if (tvGreeting != null) tvGreeting.setText("Chào " + shortName + "!");
                            }
                            if (profile.containsKey("avatarUrl")) {
                                String avatarUrl = (String) profile.get("avatarUrl");
                                if (ivAvatarHome != null && getContext() != null) {
                                    ivAvatarHome.setPadding(0, 0, 0, 0);
                                    if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("data:image")) {
                                        byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                                        Glide.with(getContext()).load(b).circleCrop().into(ivAvatarHome);
                                    } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                        Glide.with(getContext()).load(avatarUrl).circleCrop().into(ivAvatarHome);
                                    } else {
                                        Glide.with(getContext()).load(R.drawable.ic_default_user).circleCrop().into(ivAvatarHome);
                                    }
                                }
                            } else if (ivAvatarHome != null && getContext() != null) {
                                ivAvatarHome.setPadding(0, 0, 0, 0);
                                Glide.with(getContext()).load(R.drawable.ic_default_user).circleCrop().into(ivAvatarHome);
                            }
                        }

                        String role = documentSnapshot.getString("role");
                        if ("mentor".equals(role)) {
                            if (layoutBadgeMentor != null) layoutBadgeMentor.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // User deleted from DB -> logout
                        Toast.makeText(getContext(), "Tài khoản của bạn không tồn tại hoặc đã bị xóa.", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getActivity(), com.example.appdraw.auth.LoginOptionsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) getActivity().finish();
                    }
                });
        }

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

        // --- Thử Thách ---
        setupChallenges(view);

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

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            setupChallenges(getView());
        }
    }

    private void setupSuggestedLessons(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_suggested_lessons_container);
        if (container == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("SuggestedLessons").get().addOnSuccessListener(queryDocumentSnapshots -> {
            boolean isCorrupted = !queryDocumentSnapshots.isEmpty() 
                && queryDocumentSnapshots.getDocuments().get(0).getString("imageRes") != null 
                && queryDocumentSnapshots.getDocuments().get(0).getString("imageRes").matches("-?\\d+");

            if (queryDocumentSnapshots.isEmpty() || isCorrupted) {
                if (isCorrupted) {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                }

                // Auto seed 5 default realistic lessons
                String[] titles = {"Phác thảo Manga", "Vẽ hoa màu nước", "Gấp hạc giấy cơ bản", "Phong cảnh đồi núi", "Rừng thông sương mù"};
                String[] authors = {"Bởi Linh Trần", "Bởi Hoàng Lam", "Bởi Donal", "Bởi Thùy Chi", "Bởi Tuấn Vũ"};
                String[] durations = {"25 min", "45 min", "60 min", "30 min", "50 min"};
                String[] images = {"tp_trending_1", "ve_hoa_mau_nuoc", "ve_thien_nhien", "banner_watercolor", "tp_trending_2"};
                
                for (int i = 0; i < titles.length; i++) {
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("title", titles[i]);
                    data.put("author", authors[i]);
                    data.put("duration", durations[i]);
                    data.put("imageRes", images[i]);
                    db.collection("SuggestedLessons").add(data);
                }
                container.postDelayed(() -> setupSuggestedLessons(view), 2500);
                return;
            }

            if (getContext() == null) return;
            LayoutInflater inflater = LayoutInflater.from(getContext());
            container.removeAllViews();
            
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                String title = doc.getString("title");
                String author = doc.getString("author");
                String duration = doc.getString("duration");
                String imageResStr = doc.getString("imageRes");
                String imageUrl = doc.getString("imageUrl");

                View lessonView = inflater.inflate(R.layout.item_lesson_preview, container, false);
                
                TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
                TextView tvAuthor = lessonView.findViewById(R.id.tv_lesson_author);
                ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
                TextView tvStatus = lessonView.findViewById(R.id.tv_lesson_status);
                // Try to map duration if we had a dedicated ID, but item_lesson_preview has 25 min static text,
                // let's just find the textview dynamically without ID or skip it. (Oh wait, no ID for duration in item_lesson_preview, so we leave it).
                
                if (tvTitle != null) tvTitle.setText(title);
                if (tvAuthor != null) tvAuthor.setText(author);
                
                if (ivThumb != null) {
                    if (imageResStr != null && !imageResStr.isEmpty() && !imageResStr.matches("-?\\d+")) {
                        try { 
                            int resId = getResources().getIdentifier(imageResStr, "drawable", getContext().getPackageName());
                            if (resId != 0) ivThumb.setImageResource(resId);
                        } catch (Exception e){}
                    } else if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).centerCrop().into(ivThumb);
                    }
                }

                // Default status
                tvStatus.setText("Chưa học");
                tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                tvStatus.setTextColor(Color.parseColor("#808080"));
                
                android.widget.RatingBar rb = lessonView.findViewById(R.id.rating_bar);
                if (rb != null) {
                    float randomRating = 3.5f + (float)(Math.random() * 1.5f);
                    rb.setRating(randomRating);
                }

                // Check sync progress
                if (uid != null && title != null) {
                    db.collection("Users").document(uid).collection("lessonProgress").document(title)
                        .get().addOnSuccessListener(progDoc -> {
                            if (progDoc.exists()) {
                                String status = progDoc.getString("status");
                                if ("COMPLETED".equals(status)) {
                                    tvStatus.setText("Hoàn thành");
                                    tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                                    tvStatus.setTextColor(Color.WHITE);
                                } else if ("IN_PROGRESS".equals(status)) {
                                    tvStatus.setText("Đang học");
                                    tvStatus.setBackgroundResource(R.drawable.rounded_bg_gray);
                                    tvStatus.setTextColor(Color.parseColor("#666666"));
                                }
                            }
                        });
                }

                lessonView.setOnClickListener(v -> {
                    String currentStatus = tvStatus.getText().toString();
                    if ("Hoàn thành".equals(currentStatus)) {
                        Toast.makeText(getContext(), "Bạn đã hoàn thành bài học: " + title, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(getActivity(), LessonDetailActivity.class);
                        intent.putExtra("LESSON_TITLE", title);
                        startActivity(intent);
                    }
                });

                container.addView(lessonView);
            }
        });
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

    private void setupChallenges(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_challenges_container);
        if (container == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Challenges").addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null || queryDocumentSnapshots == null) return;

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
                    if ("Vẽ cây ngày trái đất".equals(t) || "14 ngày ký họa phong cảnh".equals(t) || "Thử thách Anime 30 ngày".equals(t)) {
                        doc.getReference().delete();
                    }
                }
            }

            if (getContext() == null) return;
            LayoutInflater inflater = LayoutInflater.from(getContext());
            container.removeAllViews();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

            int count = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                if (count >= 1) break; // Only display 1 challenge dynamically
                
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

                if (tvTitle != null) tvTitle.setText("Thử thách: " + title);
                if (tvAuthor != null) tvAuthor.setText(author);
                if (tvDate != null) tvDate.setText(dateStr);
                if (tvParticipants != null) tvParticipants.setText(participantsCount);

                if (ivImage != null) {
                    if (imageResStr != null && !imageResStr.isEmpty()) {
                        try { ivImage.setImageResource(Integer.parseInt(imageResStr)); } catch (Exception e){}
                    } else if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this).load(imageUrl).centerCrop().into(ivImage);
                    }
                }

                String authorId = doc.getString("authorId");
                
                // Check role and status locally
                if (uid != null) {
                    db.collection("Users").document(uid).get().addOnSuccessListener(userDoc -> {
                        String role = userDoc.getString("role");
                        
                        String mentorName = "Mentor";
                        if (userDoc.exists()) {
                            java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc.get("profile");
                            if (profile != null && profile.containsKey("fullName")) {
                                mentorName = "Mentor: " + profile.get("fullName");
                            }
                        }

                        if ("mentor".equals(role)) {
                            if (btnJoin != null) {
                                boolean isAuthor = false;
                                if (uid.equals(authorId)) isAuthor = true;
                                else if (authorId == null && author != null && author.equals(mentorName)) isAuthor = true; // Fallback cho bài cũ

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
                                            btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
                                        } else {
                                            btnJoin.setText("Tiếp tục");
                                            btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E67E22"))); // Orange
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
                                        db.collection("Users").document(uid).collection("joinedChallenges").document(title).set(joinData);
                                        
                                        // Increment global counter
                                        try {
                                            String countStr = participantsCount;
                                            if (countStr != null) countStr = countStr.replaceAll("[^0-9]", "");
                                            int currentCount = (countStr == null || countStr.isEmpty()) ? 0 : Integer.parseInt(countStr);
                                            currentCount++;
                                            String newCountStr = currentCount + " đã tham gia";
                                            db.collection("Challenges").document(doc.getId()).update("participantsCount", newCountStr);
                                            if (tvParticipants != null) tvParticipants.setText(newCountStr);
                                        } catch (Exception e) {}
                                        
                                        Toast.makeText(getContext(), "Đã tham gia thử thách!", Toast.LENGTH_SHORT).show();
                                    }
                                    
                                    btnJoin.setEnabled(true);
                                    btnJoin.setText("Tiếp tục");
                                    btnJoin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E67E22")));
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
