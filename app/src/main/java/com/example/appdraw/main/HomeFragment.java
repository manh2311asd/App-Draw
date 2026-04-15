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
    private com.google.firebase.firestore.ListenerRegistration savedLessonsListenerReg;
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
            btnAddChallenge.setVisibility(View.GONE);
            btnAddChallenge.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), com.example.appdraw.challenge.CreateChallengeActivity.class);
                startActivity(intent);
            });
        }

        ImageView btnAddEvent = view.findViewById(R.id.btn_add_event);
        if (btnAddEvent != null) {
            btnAddEvent.setVisibility(View.GONE);
            btnAddEvent.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), com.example.appdraw.community.CreateEventActivity.class);
                startActivity(intent);
            });
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
        // View btnSearch = view.findViewById(R.id.btn_search);
        // if (btnSearch != null) {
        //     btnSearch.setOnClickListener(v -> {
        //         Intent intent = new Intent(getActivity(), SearchActivity.class);
        //         startActivity(intent);
        //     });
        // }

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

        // Xem tất cả sự kiện (Sự kiện sắp tới)
        View tvViewAllEvents = view.findViewById(R.id.tv_view_all_events);
        if (tvViewAllEvents != null) {
            tvViewAllEvents.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
                intent.putExtra("OPEN_EXPLORE", true);
                startActivity(intent);
            });
        }

        // Xem lịch (Lịch của bạn)
        View tvViewSchedule = view.findViewById(R.id.tv_view_calendar);
        if (tvViewSchedule != null) {
            tvViewSchedule.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), EventScheduleActivity.class);
                intent.putExtra("OPEN_EXPLORE", false);
                startActivity(intent);
            });
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Chỉ reload các phần dùng get() một lần để tránh stale UI, 
        // riêng challenge thì dùng listener rồi nên không cần nạp lại.
        if (getView() != null) {
            setupHomeEvents(getView());
            setupSuggestedLessons(getView());
            setupSavedLessons(getView());
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
        if (savedLessonsListenerReg != null) {
            savedLessonsListenerReg.remove();
            savedLessonsListenerReg = null;
        }
    }

    private void setupSuggestedLessons(View view) {
        android.widget.LinearLayout container = view.findViewById(R.id.ll_suggested_lessons_container);
        if (container == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (getContext() == null) return;

        LayoutInflater inflater = LayoutInflater.from(getContext());
        container.removeAllViews();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        if (uid == null) return;

        // B1: Lấy danh sách bài đã học xong
        db.collection("Users").document(uid).collection("lessonProgress").get().addOnSuccessListener(progSnap -> {
            java.util.Set<String> completedTitles = new java.util.HashSet<>();
            for (com.google.firebase.firestore.DocumentSnapshot d : progSnap) {
                if ("COMPLETED".equals(d.getString("status"))) {
                    completedTitles.add(d.getId());
                }
            }

            // B2: Lấy toàn bộ bài học và lọc theo chủ đề
            db.collection("Lessons").get().addOnSuccessListener(lessonSnap -> {
                // Sắp xếp các bài học theo thứ tự tăng dần (dựa vào Order hoặc ID)
                java.util.List<com.google.firebase.firestore.DocumentSnapshot> allDocs = new java.util.ArrayList<>();
                for (com.google.firebase.firestore.DocumentSnapshot d : lessonSnap) allDocs.add(d);
                allDocs.sort((d1, d2) -> {
                    Long c1 = d1.getLong("createdAt");
                    Long c2 = d2.getLong("createdAt");
                    if (c1 != null && c2 != null) return Long.compare(c1, c2);

                    String id1 = d1.getId();
                    String id2 = d2.getId();
                    try {
                        int index1 = Integer.parseInt(id1.substring(id1.lastIndexOf("_") + 1));
                        int index2 = Integer.parseInt(id2.substring(id2.lastIndexOf("_") + 1));
                        return Integer.compare(index1, index2);
                    } catch (Exception e) {
                        return id1.compareTo(id2);
                    }
                });

                java.util.Map<String, java.util.List<com.google.firebase.firestore.DocumentSnapshot>> lessonsByCategory = new java.util.HashMap<>();
                for (com.google.firebase.firestore.DocumentSnapshot doc : allDocs) {
                    String cat = doc.getString("category");
                    if (cat == null) cat = "Khác";
                    if (!lessonsByCategory.containsKey(cat)) lessonsByCategory.put(cat, new java.util.ArrayList<>());
                    lessonsByCategory.get(cat).add(doc);
                }

                // Nhóm 5 danh mục chính
                String[] coreCategories = {
                    "Dành cho người mới bắt đầu", 
                    "Vẽ thiên nhiên", 
                    "Khám phá màu nước", 
                    "Nghệ thuật vẽ Chibi", 
                    "Chân dung Manga"
                };

                java.util.List<com.google.firebase.firestore.DocumentSnapshot> finalSuggestions = new java.util.ArrayList<>();
                
                // Lấy bài đầu tiên CHƯA hoàn thành của từng danh mục
                for (String cat : coreCategories) {
                    java.util.List<com.google.firebase.firestore.DocumentSnapshot> catLessons = lessonsByCategory.get(cat);
                    if (catLessons != null) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : catLessons) {
                            String title = doc.getString("title");
                            if (title != null && !completedTitles.contains(title)) {
                                finalSuggestions.add(doc);
                                break;
                            }
                        }
                    }
                }

                // Render UI
                for (com.google.firebase.firestore.DocumentSnapshot doc : finalSuggestions) {
                    String title = doc.getString("title");
                    String author = doc.getString("author");
                    if (author == null) author = doc.getString("authorName");
                    String imageResStr = doc.getString("imageRes");
                    String imageUrl = doc.getString("thumbnailUrl");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = doc.getString("imageUrl");
                    }
                    String category = doc.getString("category");

                    // Override ảnh theo title 
                    if ("Đêm trăng sáng trên đồi".equals(title)) {
                        imageResStr = "dem_trang_sang_tren_doi";
                    } else if ("Khu vườn nhiệt đới".equals(title)) {
                        imageResStr = "khu_vuon_nhiet_doi";
                    } else if ("Thung lũng sương mù".equals(title)) {
                        imageResStr = "thung_lung_suong_mu";
                    } else if ("Vẽ rừng cây mùa thu".equals(title)) {
                        imageResStr = "ve_rung_cay_mua_thu";
                    } else if ("Tổng hợp phong cảnh".equals(title)) {
                        imageResStr = "tong_hop_phong_canh";
                    } else if ("Bãi biển lúc hoàng hôn".equals(title)) {
                        imageResStr = "bai_bien_luc_hoang_hon";
                    } else if ("Núi non trùng điệp".equals(title)) {
                        imageResStr = "nui_non_trung_diep";
                    } else if ("Dòng suối nhỏ trong vắt".equals(title)) {
                        imageResStr = "dong_suoi_nho_trong_vat";
                    } else if ("Thảo nguyên xanh mướt".equals(title)) {
                        imageResStr = "thao_nguyen_xanh_muot";
                    } else if ("Vẽ thác nước hùng vĩ".equals(title)) {
                        imageResStr = "ve_thac_nuoc_hung_vi";
                    } else if ("Làm quen với Brush".equals(title)) {
                        imageResStr = "lam_quen_voi_brush";
                    } else if ("Khái niệm hình học".equals(title)) {
                        imageResStr = "khai_niem_hinh_hoc";
                    } else if ("Đánh bóng và chiếu sáng".equals(title)) {
                        imageResStr = "danh_bong_va_chieu_sang";
                    } else if ("Kỹ thuật đan nét cọ".equals(title)) {
                        imageResStr = "ki_thuat_dan_net_co";
                    } else if ("Vẽ tĩnh vật quả táo".equals(title)) {
                        imageResStr = "ve_tinh_vat_qua_tao";
                    } else if ("Xây dựng khối 3D".equals(title)) {
                        imageResStr = "xay_dung_khoi_3d";
                    } else if ("Luyện tập tổng hợp".equals(title)) {
                        imageResStr = "luyen_tap_tong_hop";
                    } else if ("Palette pha màu cơ bản".equals(title)) {
                        imageResStr = "palette_pha_mau_co_ban";
                    } else if ("Kỹ thuật loang màu ẩm".equals(title)) {
                        imageResStr = "ki_thuat_loang_mau_am";
                    } else if ("Vẽ bầu trời gợn mây".equals(title)) {
                        imageResStr = "ve_bau_troi_gon_may";
                    } else if ("Tĩnh vật cốc cà phê".equals(title)) {
                        imageResStr = "tinh_vat_coc_ca_phe";
                    } else if ("Bông cẩm tú cầu".equals(title)) {
                        imageResStr = "bong_cam_tu_cau";
                    } else if ("Sơn thủy hữu tình".equals(title)) {
                        imageResStr = "son_thuy_huu_tinh";
                    } else if ("Ánh tà dương hoàng hôn".equals(title)) {
                        imageResStr = "anh_ta_duong_hoang_hon";
                    } else if ("Phác thảo khuôn mặt Chibi".equals(title)) {
                        imageResStr = "phac_thao_khuon_mat_chibi";
                    } else if ("Tỷ lệ cơ thể đầu to".equals(title)) {
                        imageResStr = "ty_le_co_the_dau_to";
                    } else if ("Vẽ mắt to tròn đáng yêu".equals(title)) {
                        imageResStr = "ve_mat_to_tron_dang_yeu";
                    } else if ("Biểu cảm khuôn mặt dễ thương".equals(title)) {
                        imageResStr = "bieu_cam_khuon_mat_de_thuong";
                    } else if ("Vẽ tóc bồng bềnh".equals(title)) {
                        imageResStr = "ve_toc_bong_benh";
                    } else if ("Phối đồ phong cách basic".equals(title)) {
                        imageResStr = "phoi_do_phong_cach_basic";
                    } else if ("Lên màu pastel cơ bản".equals(title)) {
                        imageResStr = "len_mau_pastel_co_ban";
                    } else if ("Hoàn thiện nhân vật".equals(title)) {
                        imageResStr = "hoan_thien_nhan_vat";
                    } else if ("Core tỷ lệ khuôn mặt".equals(title)) {
                        imageResStr = "core_ty_le_khuon_mat";
                    } else if ("Vẽ mắt Manga mượt mà".equals(title)) {
                        imageResStr = "ve_mat_manga_muot_ma";
                    } else if ("Kiểu tóc nam và nữ cơ bản".equals(title)) {
                        imageResStr = "kieu_toc_nam_va_nu_co_ban";
                    } else if ("Mảng biểu cảm vui buồn".equals(title)) {
                        imageResStr = "mang_bieu_cam_vui_buon";
                    } else if ("Góc nghiêng thần thánh".equals(title)) {
                        imageResStr = "goc_nghieng_than_thanh";
                    } else if ("Phác họa nhân vật nữ".equals(title)) {
                        imageResStr = "phac_hoa_nhan_vat_nu";
                    } else if ("Phác họa nhân vật nam".equals(title)) {
                        imageResStr = "phac_hoa_nhan_vat_nam";
                    }

                    View lessonView = inflater.inflate(R.layout.item_lesson_preview, container, false);

                    TextView tvTitle = lessonView.findViewById(R.id.tv_lesson_title);
                    TextView tvAuthor = lessonView.findViewById(R.id.tv_lesson_author);
                    ImageView ivThumb = lessonView.findViewById(R.id.iv_lesson_thumb);
                    TextView tvStatus = lessonView.findViewById(R.id.tv_lesson_status);
                    TextView tvDuration = lessonView.findViewById(R.id.tv_duration);

                    if (tvTitle != null) tvTitle.setText(title);
                    if (tvAuthor != null) {
                        if (author != null && !author.toLowerCase().startsWith("bởi")) {
                            tvAuthor.setText("Bởi " + author);
                        } else {
                            tvAuthor.setText(author);
                        }
                    }

                    if (ivThumb != null) {
                        if (imageResStr != null && !imageResStr.isEmpty() && !imageResStr.matches("-?\\d+")) {
                            try {
                                int resId = getResources().getIdentifier(imageResStr, "drawable", getContext().getPackageName());
                                if (resId != 0) ivThumb.setImageResource(resId);
                            } catch (Exception e) {}
                        } else if (imageUrl != null && !imageUrl.isEmpty()) {
                            if (imageUrl.startsWith("data:image")) {
                                try {
                                    byte[] imageByteArray = android.util.Base64.decode(imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                                    com.bumptech.glide.Glide.with(this).load(imageByteArray).centerCrop().into(ivThumb);
                                } catch (Exception e) {
                                    ivThumb.setImageResource(R.drawable.ve_hoa_mau_nuoc);
                                }
                            } else {
                                com.bumptech.glide.Glide.with(this).load(imageUrl).centerCrop().into(ivThumb);
                            }
                        } else {
                            ivThumb.setImageResource(R.drawable.ve_hoa_mau_nuoc);
                        }
                    }

                    // Mặc định cho bài gợi ý là "Đang học" hoặc "Chưa học"
                    tvStatus.setText("Gợi ý");
                    tvStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                    tvStatus.setTextColor(Color.parseColor("#808080"));

                    android.widget.RatingBar rb = lessonView.findViewById(R.id.rating_bar);
                    if (rb != null) rb.setRating(4.5f);

                    if (tvDuration != null) {
                        Long actualDuration = doc.getLong("durationMin");
                        if (actualDuration != null && actualDuration > 0) {
                            tvDuration.setText(actualDuration + " min");
                        } else {
                            if (category != null && (category.toLowerCase().contains("mới bắt đầu"))) tvDuration.setText("20 min");
                            else if (category != null && category.toLowerCase().contains("thiên nhiên")) tvDuration.setText("45 min");
                            else tvDuration.setText("60 min");
                        }
                    }

                    // Check progress để biết Đang học
                    db.collection("Users").document(uid).collection("lessonProgress").document(title)
                        .get().addOnSuccessListener(progDoc -> {
                            if (progDoc.exists()) {
                                String status = progDoc.getString("status");
                                if ("IN_PROGRESS".equals(status) || "WAITING_FOR_HOMEWORK".equals(status)) {
                                    tvStatus.setText("Đang học");
                                    tvStatus.setBackgroundResource(R.drawable.bg_badge_in_progress);
                                    tvStatus.setTextColor(Color.WHITE);
                                }
                            }
                        });

                    final String finalAuthor = author;
                    final String finalImageResStr = imageResStr;
                    lessonView.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), LessonDetailActivity.class);
                        intent.putExtra("LESSON_TITLE", title);
                        intent.putExtra("CATEGORY", category);
                        intent.putExtra("IMAGE_RES", finalImageResStr);
                        intent.putExtra("AUTHOR", finalAuthor);
                        intent.putExtra("LESSON_ID", doc.getId());
                        startActivity(intent);
                    });

                    container.addView(lessonView);
                }
            });
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

        if (savedLessonsListenerReg != null) {
            savedLessonsListenerReg.remove();
        }

        savedLessonsListenerReg = com.google.firebase.firestore.FirebaseFirestore.getInstance()
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

                            if (uid != null && title != null) {
                                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("Users").document(uid).collection("lessonProgress").document(title)
                                        .get().addOnSuccessListener(progDoc -> {
                                            if (progDoc.exists()) {
                                                String status = progDoc.getString("status");
                                                if ("COMPLETED".equals(status)) {
                                                    tvStatus.setText("Hoàn thành");
                                                    tvStatus.setBackgroundResource(R.drawable.bg_badge_completed);
                                                    tvStatus.setTextColor(android.graphics.Color.WHITE);
                                                } else if ("IN_PROGRESS".equals(status)
                                                        || "WAITING_FOR_HOMEWORK".equals(status)) {
                                                    tvStatus.setText("Đang học");
                                                    tvStatus.setBackgroundResource(R.drawable.bg_badge_in_progress);
                                                    tvStatus.setTextColor(android.graphics.Color.WHITE);
                                                }
                                            }
                                        });
                            }
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

                        final String finalAuthor = author;
                        final String finalImageResStr = imageResStr;
                        lessonView.setOnClickListener(v -> {
                            if (tvStatus != null && "Hoàn thành".equals(tvStatus.getText().toString())) {
                                Intent intent = new Intent(getActivity(), com.example.appdraw.explore.MySubmissionActivity.class);
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
                        List<Event> exploreEvents = new ArrayList<>();
                        long now = System.currentTimeMillis();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : eventDocs) {
                            Event e = doc.toObject(Event.class);
                            if (e != null) {
                                boolean isExpired = false;
                                try {
                                    if (e.getEndTime() != null && e.getEndTime().contains(":")) {
                                        String[] parts = e.getEndTime().split(":");
                                        int hour = Integer.parseInt(parts[0].trim());
                                        int min = Integer.parseInt(parts[1].trim());
                                        java.util.Calendar cal = java.util.Calendar.getInstance();
                                        cal.setTimeInMillis(e.getDateMillis());
                                        cal.set(java.util.Calendar.HOUR_OF_DAY, hour);
                                        cal.set(java.util.Calendar.MINUTE, min);
                                        if (cal.getTimeInMillis() < now) isExpired = true;
                                    } else if (e.getDateMillis() + 24 * 60 * 60 * 1000L < now) {
                                        isExpired = true;
                                    }
                                } catch (Exception ex) {
                                    if (e.getDateMillis() + 24 * 60 * 60 * 1000L < now) isExpired = true;
                                }

                                if (!isExpired) {
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
                                    } else {
                                        exploreEvents.add(e);
                                    }
                                }
                            }
                        }

                        java.util.Collections.sort(upcomingEvents,
                                (e1, e2) -> Long.compare(e1.getDateMillis(), e2.getDateMillis()));
                        java.util.Collections.sort(exploreEvents,
                                (e1, e2) -> Long.compare(e1.getDateMillis(), e2.getDateMillis()));

                        if (upcomingEvents.isEmpty()) {
                            tvEmptySchedule.setText("Bạn chưa có lịch học nào. Nhấn Xem lịch để khám phá!");
                            tvEmptySchedule.setVisibility(View.VISIBLE);
                            rvMySchedule.setVisibility(View.GONE);
                        } else {
                            tvEmptySchedule.setVisibility(View.GONE);
                            rvMySchedule.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
                            rvMySchedule.setVisibility(View.VISIBLE);
                            int limit = Math.min(10, upcomingEvents.size());
                            List<Event> displayEvents = upcomingEvents.subList(0, limit);
                            rvMySchedule.setAdapter(new HomeExploreEventAdapter(displayEvents, myTickets));
                        }

                        if (exploreEvents.isEmpty()) {
                            rvExploreEvents.setVisibility(View.GONE);
                        } else {
                            rvExploreEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                            rvExploreEvents.setVisibility(View.VISIBLE);
                            int limit = Math.min(5, exploreEvents.size());
                            rvExploreEvents.setAdapter(new HomeExploreEventAdapter(exploreEvents.subList(0, limit), myTickets));
                        }
                    });
                });
    }

    private class HomeExploreEventAdapter extends HomeScheduleAdapter {
        HomeExploreEventAdapter(List<Event> l, List<EventTicket> myT) {
            super(l, myT);
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            VH holder = super.onCreateViewHolder(parent, viewType);
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null) {
                lp.width = (int) (300 * parent.getContext().getResources().getDisplayMetrics().density);
                if (lp instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) lp).rightMargin = (int) (12 * parent.getContext().getResources().getDisplayMetrics().density);
                    ((ViewGroup.MarginLayoutParams) lp).leftMargin = (int) (4 * parent.getContext().getResources().getDisplayMetrics().density);
                }
                holder.itemView.setLayoutParams(lp);
            }
            return holder;
        }
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
            String endTimeStr = e.getEndTime();
            if (endTimeStr != null && !endTimeStr.isEmpty()) {
                holder.tvTime.setText(e.getStartTime() + " - " + endTimeStr + " • " + cal.get(java.util.Calendar.DAY_OF_MONTH) + "/"
                        + (cal.get(java.util.Calendar.MONTH) + 1));
            } else {
                holder.tvTime.setText(e.getStartTime() + " - " + cal.get(java.util.Calendar.DAY_OF_MONTH) + "/"
                        + (cal.get(java.util.Calendar.MONTH) + 1));
            }

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
                    String myTicketId = null;
                    for (EventTicket t : tickets) {
                        if (t.getEventId().equals(e.getId())) {
                            myTicketId = t.getId();
                        }
                    }
                    if (myTicketId != null) {
                        holder.btnAction.setText("Xem vé");
                        String finalMyTicketId = myTicketId;
                        holder.btnAction.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(),
                                    com.example.appdraw.community.EventTicketActivity.class);
                            intent.putExtra("EVENT_ID", e.getId());
                            intent.putExtra("TICKET_ID", finalMyTicketId);
                            startActivity(intent);
                        });
                    } else {
                        holder.btnAction.setText("Đăng ký");
                        holder.btnAction.setOnClickListener(v -> registerHomeEvent(e));
                    }
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

    private void registerHomeEvent(Event event) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        
        String ticketId = FirebaseFirestore.getInstance().collection("EventRegistrations").document().getId();
        String ticketCode = "TKT" + String.format("%04d", (int)(Math.random() * 10000));
        EventTicket ticket = new EventTicket(ticketId, event.getId(), uid, ticketCode, System.currentTimeMillis());
        
        FirebaseFirestore.getInstance().collection("EventRegistrations").document(ticketId)
                .set(ticket)
                .addOnSuccessListener(aVoid -> {
                    if (getView() != null) setupHomeEvents(getView());
                    showSuccessDialog(event, ticket);
                    
                    if (event.getAuthorId() != null && !event.getAuthorId().equals(uid)) {
                        com.example.appdraw.utils.NotificationHelper.sendNotification(event.getAuthorId(), "EVENT", "Một người dùng vừa đăng ký sự kiện: " + event.getTitle(), event.getId());
                    }
                    com.example.appdraw.utils.NotificationHelper.sendNotification(uid, "EVENT", "Bạn đã đăng ký thành công sự kiện: " + event.getTitle(), event.getId());
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi đăng ký", Toast.LENGTH_SHORT).show());
    }

    private void showSuccessDialog(Event event, EventTicket ticket) {
        if (getContext() == null) return;
        android.app.Dialog dialog = new android.app.Dialog(getContext());
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_event_registered);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        
        TextView tvTitle = dialog.findViewById(R.id.tv_dialog_event_title);
        TextView tvTime = dialog.findViewById(R.id.tv_dialog_event_time);
        TextView tvLocation = dialog.findViewById(R.id.tv_dialog_event_location);
        TextView tvFormat = dialog.findViewById(R.id.tv_dialog_event_format);
        TextView tvPrice = dialog.findViewById(R.id.tv_dialog_event_price);
        View btnViewTicket = dialog.findViewById(R.id.btn_dialog_view_ticket);
        View btnBackSchedule = dialog.findViewById(R.id.btn_dialog_back_schedule);

        tvTitle.setText(event.getTitle());
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(event.getDateMillis());
        tvTime.setText(event.getStartTime() + " - " + event.getEndTime() + " - " + cal.get(java.util.Calendar.DAY_OF_MONTH) + "/" + (cal.get(java.util.Calendar.MONTH)+1));
        
        tvLocation.setText(event.getLocation());
        tvFormat.setText(event.isOnline() ? "Online" : "Offline");
        tvPrice.setText(event.getPrice());

        btnViewTicket.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(getActivity(), com.example.appdraw.community.EventTicketActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            intent.putExtra("TICKET_ID", ticket.getId());
            startActivity(intent);
        });

        btnBackSchedule.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
