package com.example.appdraw.explore;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdraw.R;
import com.example.appdraw.model.Lesson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;
import android.content.SharedPreferences;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private LinearLayout llSuggestions;
    private LinearLayout llEmpty;
    private RecyclerView rvResults;
    private UnifiedSearchAdapter adapter;
    private List<Lesson> allLessons = new ArrayList<>();
    private List<SearchResultItem> unifiedResults = new ArrayList<>();
    private android.os.Handler searchHandler = new android.os.Handler(android.os.Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ImageView ivBack = findViewById(R.id.iv_back_search);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        etSearch = findViewById(R.id.et_search_input);
        llSuggestions = findViewById(R.id.ll_search_suggestions);
        llEmpty = findViewById(R.id.ll_empty_search);
        rvResults = findViewById(R.id.rv_search_results);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UnifiedSearchAdapter();
        rvResults.setAdapter(adapter);

        loadStaticLessons();
        setupSearchControls();
        setupClickableSuggestions();
        loadSearchHistory();

        // Auto-focus keyboard
        etSearch.requestFocus();
        getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        etSearch.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                    android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }, 200);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                String q = s.toString().trim();

                ImageView ivClear = findViewById(R.id.iv_clear_text);
                if (ivClear != null) {
                    ivClear.setVisibility(q.length() > 0 ? View.VISIBLE : View.GONE);
                }

                if (q.isEmpty()) {
                    performSearch(q); // immediately clear
                } else {
                    searchHandler.postDelayed(() -> performSearch(q), 500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupSearchControls() {
        ImageView ivClear = findViewById(R.id.iv_clear_text);
        if (ivClear != null) {
            ivClear.setOnClickListener(v -> etSearch.setText(""));
        }

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    saveSearchHistory(query);
                }
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                        android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        TextView tvClearHistory = findViewById(R.id.tv_clear_history);
        if (tvClearHistory != null) {
            tvClearHistory.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("SearchAppData", MODE_PRIVATE);
                prefs.edit().remove("search_history").apply();
                loadSearchHistory();
                Toast.makeText(this, "Đã xóa lịch sử", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupClickableSuggestions() {
        View.OnClickListener suggestionListener = v -> {
            String q = "";
            if (v instanceof Chip) {
                q = ((Chip) v).getText().toString();
            } else if (v.getId() == R.id.card_suggestion_1) {
                q = "Vẽ hoàng hôn";
            } else if (v.getId() == R.id.card_suggestion_2) {
                q = "Phác thảo cơ bản";
            }

            if (!q.isEmpty()) {
                etSearch.setText(q);
                etSearch.setSelection(q.length());
                saveSearchHistory(q);
            }
        };

        ChipGroup cgCategory = findViewById(R.id.cg_categories);
        if (cgCategory != null) {
            // Need single selection enabled in xml, but let's toggle manually or just
            // trigger search
            for (int i = 0; i < cgCategory.getChildCount(); i++) {
                View child = cgCategory.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setCheckable(true);
                    ((Chip) child).setOnCheckedChangeListener((buttonView, isChecked) -> {
                        performSearch(etSearch.getText().toString().trim());
                    });
                }
            }
        }

        ChipGroup cgTrending = findViewById(R.id.cg_trending);
        if (cgTrending != null) {
            for (int i = 0; i < cgTrending.getChildCount(); i++) {
                cgTrending.getChildAt(i).setOnClickListener(suggestionListener);
            }
        }

        View card1 = findViewById(R.id.card_suggestion_1);
        View card2 = findViewById(R.id.card_suggestion_2);
        if (card1 != null)
            card1.setOnClickListener(suggestionListener);
        if (card2 != null)
            card2.setOnClickListener(suggestionListener);
    }

    private void loadSearchHistory() {
        ChipGroup cgHistory = findViewById(R.id.cg_history);
        LinearLayout llHistory = findViewById(R.id.ll_history_header);
        if (cgHistory == null || llHistory == null)
            return;

        cgHistory.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("SearchAppData", MODE_PRIVATE);
        String histStr = prefs.getString("search_history", "");

        if (histStr.isEmpty()) {
            llHistory.setVisibility(View.GONE);
            cgHistory.setVisibility(View.GONE);
        } else {
            llHistory.setVisibility(View.VISIBLE);
            cgHistory.setVisibility(View.VISIBLE);
            String[] items = histStr.split(";;;");
            for (String item : items) {
                if (item.trim().isEmpty())
                    continue;
                Chip chip = new Chip(this);
                chip.setText(item);
                chip.setOnClickListener(v -> {
                    etSearch.setText(item);
                    etSearch.setSelection(item.length());
                });
                cgHistory.addView(chip);
            }
        }
    }

    private void saveSearchHistory(String query) {
        SharedPreferences prefs = getSharedPreferences("SearchAppData", MODE_PRIVATE);
        String histStr = prefs.getString("search_history", "");
        List<String> items = new ArrayList<>();
        if (!histStr.isEmpty()) {
            items.addAll(Arrays.asList(histStr.split(";;;")));
        }

        // Remove if exists to push to front
        items.remove(query);
        items.add(0, query);

        // Keep max 10
        if (items.size() > 10) {
            items = items.subList(0, 10);
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            b.append(items.get(i));
            if (i < items.size() - 1)
                b.append(";;;");
        }

        prefs.edit().putString("search_history", b.toString()).apply();
        loadSearchHistory();
    }

    private void loadStaticLessons() {
        allLessons.clear();
        addStaticCategory("Phác thảo khuôn mặt Chibi", "Donal", "15 min", 5.0f);
        addStaticCategory("Tỷ lệ cơ thể đầu to", "Mai Anh", "10 min", 4.0f);
        addStaticCategory("Vẽ mắt to tròn đáng yêu", "Quốc Bảo", "25 min", 4.5f);
        addStaticCategory("Lên màu pastel cơ bản", "Mai Anh", "60 min", 5.0f);

        addStaticCategory("Phong cảnh đồi núi", "Thùy Chi", "45 min", 4.5f);
        addStaticCategory("Rừng thông sương mù", "Tuấn Vũ", "50 min", 5.0f);
        addStaticCategory("Bầu trời hoàng hôn", "Hải Nam", "30 min", 4.0f);
        addStaticCategory("Kỹ thuật vẽ mây", "Tuấn Vũ", "25 min", 3.5f);
        addStaticCategory("Lá cây mùa thu", "Thùy Chi", "20 min", 4.5f);

        addStaticCategory("Vẽ hoa màu nước", "Hoàng Lam", "25 min", 5.0f);
        addStaticCategory("Phong cảnh hồ thu", "Hoàng Lam", "40 min", 4.5f);
        addStaticCategory("Cánh đồng hoa cúc", "Thu Thủy", "60 min", 4.0f);
        addStaticCategory("Vẽ cá vàng", "Hoàng Lam", "35 min", 5.0f);

        addStaticCategory("Phác thảo Manga", "Linh Trần", "40 min", 4.5f);
        addStaticCategory("Tỷ lệ khuôn mặt", "Nhật Anh", "30 min", 4.0f);
        addStaticCategory("Vẽ mắt Manga", "Linh Trần", "25 min", 5.0f);
        addStaticCategory("Trang phục nữ sinh", "Nhật Anh", "45 min", 4.5f);

        addStaticCategory("Làm quen Procreate", "Minh Khang", "25 min", 5.0f);
        addStaticCategory("Layer và Blending", "Tuấn Vũ", "40 min", 4.5f);
        addStaticCategory("Màu sắc Digital", "Minh Khang", "60 min", 4.0f);
    }

    private void addStaticCategory(String title, String author, String duration, float rating) {
        Lesson l = new Lesson();
        l.setTitle(title);
        l.setAuthor(author);
        // Using level field temporarily to store string duration to save parsing,
        // usually duration is int
        l.setLevel(duration);
        l.setRating(rating);
        allLessons.add(l);
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            llSuggestions.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.GONE);
            return;
        }

        llSuggestions.setVisibility(View.GONE);
        unifiedResults.clear();
        adapter.notifyDataSetChanged();

        String normalizedQuery = removeAccents(query.toLowerCase());

        ChipGroup cgCategory = findViewById(R.id.cg_categories);
        boolean searchLessons = true;
        boolean searchProjects = true;
        boolean searchMentors = true;

        if (cgCategory != null) {
            for (int i = 0; i < cgCategory.getChildCount(); i++) {
                View child = cgCategory.getChildAt(i);
                if (child instanceof Chip && ((Chip) child).isChecked()) {
                    String chipText = ((Chip) child).getText().toString();
                    if ("Bài học".equals(chipText)) {
                        searchProjects = false;
                        searchMentors = false;
                    } else if ("Dự án".equals(chipText) || "Tác phẩm".equals(chipText) || "Hashtag".equals(chipText)) {
                        searchLessons = false;
                        searchMentors = false;
                    } else if ("Nghệ sĩ".equals(chipText)) {
                        searchLessons = false;
                        searchProjects = false;
                    }
                    break; // Use the first checked
                }
            }
        }

        if (searchLessons) {
            for (Lesson lesson : allLessons) {
                String title = lesson.getTitle() != null ? lesson.getTitle().toLowerCase() : "";
                if (removeAccents(title).contains(normalizedQuery)) {
                    unifiedResults.add(new SearchResultItem("LESSON", lesson.getTitle(), "Bởi " + lesson.getAuthor(),
                            String.valueOf(lesson.getRating()) + "★", null, lesson.getTitle()));
                }
            }
            checkEmptyAndNotify();
        }

        if (searchProjects) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Projects")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String title = doc.getString("title");
                            String desc = doc.getString("description");
                            String tnorm = title != null ? removeAccents(title.toLowerCase()) : "";
                            String dnorm = desc != null ? removeAccents(desc.toLowerCase()) : "";
                            if (tnorm.contains(normalizedQuery) || dnorm.contains(normalizedQuery)) {
                                String authorName = doc.getString("authorName");
                                String imageUrl = doc.getString("imageUrl");
                                unifiedResults.add(new SearchResultItem("PROJECT", title,
                                        authorName != null ? authorName : "Thành viên", "Dự án", imageUrl,
                                        doc.getId()));
                            }
                        }
                        checkEmptyAndNotify();
                    });
        }

        if (searchMentors) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Users")
                    .whereEqualTo("role", "mentor")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            java.util.Map<String, Object> profile = (java.util.Map<String, Object>) doc.get("profile");
                            if (profile != null) {
                                String name = (String) profile.get("fullName");
                                String bio = (String) profile.get("bio");
                                String avatarUrl = (String) profile.get("avatarUrl");
                                String nnorm = name != null ? removeAccents(name.toLowerCase()) : "";

                                if (nnorm.contains(normalizedQuery)) {
                                    unifiedResults.add(new SearchResultItem("ARTIST", name,
                                            bio != null ? bio : "Họa sĩ", "Nghệ sĩ", avatarUrl, doc.getId()));
                                }
                            }
                        }
                        checkEmptyAndNotify();
                    });
        }
    }

    private void checkEmptyAndNotify() {
        if (unifiedResults.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvResults.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }

    private String removeAccents(String s) {
        if (s == null)
            return "";
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private static class SearchResultItem {
        String type;
        String title;
        String subtitle;
        String extraInfo;
        String imageUrl;
        String id;

        public SearchResultItem(String type, String title, String subtitle, String extraInfo, String imageUrl,
                String id) {
            this.type = type;
            this.title = title;
            this.subtitle = subtitle;
            this.extraInfo = extraInfo;
            this.imageUrl = imageUrl;
            this.id = id;
        }
    }

    private class UnifiedSearchAdapter extends RecyclerView.Adapter<UnifiedSearchAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SearchResultItem item = unifiedResults.get(position);
            holder.tvTitle.setText(item.title != null ? item.title : "");
            holder.tvSubtitle.setText(item.subtitle != null ? item.subtitle : "");
            holder.tvType.setText(item.extraInfo != null ? item.extraInfo : "");

            if ("LESSON".equals(item.type)) {
                holder.ivImage.setImageResource(R.drawable.ve_thien_nhien); // Default
                holder.tvType.setTextColor(android.graphics.Color.parseColor("#E67E22"));
            } else if ("PROJECT".equals(item.type)) {
                holder.tvType.setTextColor(android.graphics.Color.parseColor("#4272D0"));
                if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                    com.bumptech.glide.Glide.with(holder.itemView.getContext()).load(item.imageUrl).centerCrop()
                            .into(holder.ivImage);
                } else
                    holder.ivImage.setImageResource(R.mipmap.ic_launcher);
            } else if ("ARTIST".equals(item.type)) {
                holder.tvType.setTextColor(android.graphics.Color.parseColor("#2ECC71"));
                if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                    com.bumptech.glide.Glide.with(holder.itemView.getContext()).load(item.imageUrl).circleCrop()
                            .into(holder.ivImage);
                } else
                    holder.ivImage.setImageResource(R.drawable.ic_default_user);
            }

            holder.itemView.setOnClickListener(v -> {
                if ("LESSON".equals(item.type)) {
                    Intent intent = new Intent(SearchActivity.this, LessonDetailActivity.class);
                    intent.putExtra("LESSON_TITLE", item.title);
                    startActivity(intent);
                } else if ("PROJECT".equals(item.type)) {
                    Intent intent = new Intent(SearchActivity.this,
                            com.example.appdraw.project.ProjectDetailActivity.class);
                    intent.putExtra("PROJECT_ID", item.id);
                    startActivity(intent);
                } else if ("ARTIST".equals(item.type)) {
                    Intent intent = new Intent(SearchActivity.this, ArtistDetailActivity.class);
                    intent.putExtra("ARTIST_ID", item.id);
                    intent.putExtra("ARTIST_NAME", item.title);
                    intent.putExtra("ARTIST_BIO", item.subtitle);
                    intent.putExtra("ARTIST_AVATAR", item.imageUrl);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return unifiedResults.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle, tvType;
            ImageView ivImage;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_result_title);
                tvSubtitle = itemView.findViewById(R.id.tv_result_subtitle);
                tvType = itemView.findViewById(R.id.tv_result_type);
                ivImage = itemView.findViewById(R.id.iv_result_image);
            }
        }
    }
}
