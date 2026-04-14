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
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private LinearLayout llSuggestions;
    private LinearLayout llEmpty;
    private RecyclerView rvResults;
    private LessonSearchAdapter adapter;
    private List<Lesson> allLessons = new ArrayList<>();
    private List<Lesson> filteredLessons = new ArrayList<>();

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
        adapter = new LessonSearchAdapter();
        rvResults.setAdapter(adapter);

        loadStaticLessons();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLessons(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadStaticLessons() {
        allLessons.clear();
        addStaticCategory("Gấp hạc giấy cơ bản", "Donal", "15 min", 5.0f);
        addStaticCategory("Ếch nhảy Origami", "Mai Anh", "10 min", 4.0f);
        addStaticCategory("Xếp hoa sen giấy", "Quốc Bảo", "25 min", 4.5f);
        addStaticCategory("Gấp rồng cổ đại", "Mai Anh", "60 min", 5.0f);
        
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
        // Using level field temporarily to store string duration to save parsing, usually duration is int
        l.setLevel(duration); 
        l.setRating(rating);
        allLessons.add(l);
    }

    private void filterLessons(String query) {
        if (query.isEmpty()) {
            llSuggestions.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.GONE);
            return;
        }

        llSuggestions.setVisibility(View.GONE);
        filteredLessons.clear();
        String normalizedQuery = removeAccents(query.toLowerCase());
        
        for (Lesson lesson : allLessons) {
            String title = lesson.getTitle() != null ? lesson.getTitle().toLowerCase() : "";
            if (removeAccents(title).contains(normalizedQuery)) {
                filteredLessons.add(lesson);
            }
        }
        
        if (filteredLessons.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvResults.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }
    
    private String removeAccents(String s) {
        if (s == null) return "";
        String normalized = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private class LessonSearchAdapter extends RecyclerView.Adapter<LessonSearchAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson_list, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Lesson lesson = filteredLessons.get(position);
            holder.tvTitle.setText(lesson.getTitle());
            holder.tvAuthor.setText("Bởi " + lesson.getAuthor());
            holder.tvDuration.setText(lesson.getLevel()); // Stored string duration in level field
            holder.ratingBar.setRating(lesson.getRating());
            
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this, LessonDetailActivity.class);
                intent.putExtra("LESSON_TITLE", lesson.getTitle());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return filteredLessons.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvAuthor, tvDuration;
            RatingBar ratingBar;
            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_lesson_title);
                tvAuthor = itemView.findViewById(R.id.tv_author);
                tvDuration = itemView.findViewById(R.id.tv_duration);
                ratingBar = itemView.findViewById(R.id.rating_bar);
            }
        }
    }
}
