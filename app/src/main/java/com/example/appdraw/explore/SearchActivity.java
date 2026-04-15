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
            java.util.Set<String> seenLessonTitles = new java.util.HashSet<>();
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("Lessons")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String title = doc.getString("title");
                            if (title == null) continue;
                            String tnorm = removeAccents(title.toLowerCase());
                            if (tnorm.contains(normalizedQuery)) {
                                if (seenLessonTitles.contains(title)) continue;
                                seenLessonTitles.add(title);
                                
                                String authorName = doc.getString("authorName");
                                if (authorName == null) authorName = doc.getString("author");
                                String imageUrl = doc.getString("imageRes");
                                if (title.equals("Core tỷ lệ khuôn mặt")) {
                                    imageUrl = "core_ty_le_khuon_mat";
                                }
                                if (imageUrl == null || imageUrl.isEmpty() || imageUrl.matches("-?\\d+")) {
                                    imageUrl = doc.getString("thumbnailUrl");
                                    if (imageUrl == null || imageUrl.isEmpty()) imageUrl = doc.getString("imageUrl");
                                }
                                String category = doc.getString("category");
                                Double rating = doc.getDouble("rating");
                                String ratingStr = rating != null ? String.format(java.util.Locale.US, "%.1f★", rating) : "4.5★";
                                
                                unifiedResults.add(new SearchResultItem("LESSON", title,
                                        authorName != null ? (authorName.startsWith("Bởi") ? authorName : "Bởi " + authorName) : "Bởi AppDraw",
                                        ratingStr, imageUrl, doc.getId(), category));
                            }
                        }
                        checkEmptyAndNotify();
                    });
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
                                        doc.getId(), null));
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
                                            bio != null ? bio : "Họa sĩ", "Nghệ sĩ", avatarUrl, doc.getId(), null));
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
        String category;

        public SearchResultItem(String type, String title, String subtitle, String extraInfo, String imageUrl,
                String id, String category) {
            this.type = type;
            this.title = title;
            this.subtitle = subtitle;
            this.extraInfo = extraInfo;
            this.imageUrl = imageUrl;
            this.id = id;
            this.category = category;
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
                holder.tvType.setTextColor(android.graphics.Color.parseColor("#E67E22"));
                if (item.imageUrl != null && !item.imageUrl.isEmpty() && !item.imageUrl.startsWith("http") && !item.imageUrl.startsWith("data:")) {
                    try {
                        int resId = holder.itemView.getContext().getResources().getIdentifier(item.imageUrl, "drawable", holder.itemView.getContext().getPackageName());
                        if (resId != 0) holder.ivImage.setImageResource(resId);
                        else holder.ivImage.setImageResource(R.drawable.ve_thien_nhien);
                    } catch (Exception e) {}
                } else if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                    if (item.imageUrl.startsWith("data:image")) {
                        try {
                            byte[] decodedBytes = android.util.Base64.decode(item.imageUrl.split(",")[1], android.util.Base64.DEFAULT);
                            com.bumptech.glide.Glide.with(holder.itemView.getContext()).load(decodedBytes).centerCrop().into(holder.ivImage);
                        } catch (Exception e) {}
                    } else {
                        com.bumptech.glide.Glide.with(holder.itemView.getContext()).load(item.imageUrl).centerCrop().into(holder.ivImage);
                    }
                } else {
                    holder.ivImage.setImageResource(R.drawable.ve_thien_nhien);
                }
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
                    intent.putExtra("LESSON_ID", item.id);
                    intent.putExtra("IMAGE_RES", item.imageUrl);
                    if (item.category != null) {
                        intent.putExtra("CATEGORY", item.category);
                    }
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
