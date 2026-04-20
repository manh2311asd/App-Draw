package com.example.appdraw.explore;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdraw.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllCategoriesActivity extends AppCompatActivity {
    private RecyclerView rvCategories;
    private AllCategoryAdapter adapter;
    private List<Map<String, Object>> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_categories);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvCategories = findViewById(R.id.rv_all_categories);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 2)); // 2 columns
        adapter = new AllCategoryAdapter(categoryList, title -> {
            Intent intent = new Intent(this, LessonListActivity.class);
            intent.putExtra("TITLE", title);
            startActivity(intent);
        });
        rvCategories.setAdapter(adapter);

        fetchCategories();
    }

    private void fetchCategories() {
        FirebaseFirestore.getInstance().collection("Categories").orderBy("order").limit(50).get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                categoryList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("title", doc.getString("title"));
                    data.put("courseCount", doc.getString("courseCount"));
                    data.put("imageRes", doc.getString("imageRes"));
                    data.put("imageUrl", doc.getString("imageUrl"));
                    categoryList.add(data);
                }
                adapter.notifyDataSetChanged();
            });
    }
}
