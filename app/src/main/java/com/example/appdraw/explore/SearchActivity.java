package com.example.appdraw.explore;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.appdraw.R;
import com.example.appdraw.community.PostDetailActivity;
import com.example.appdraw.community.PostMediaAdapter;
import com.example.appdraw.model.Post;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private LinearLayout llSuggestions;
    private LinearLayout llEmpty;
    private RecyclerView rvResults;
    private PostMediaAdapter adapter;
    private List<Post> allPosts = new ArrayList<>();
    private List<Post> filteredPosts = new ArrayList<>();

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

        rvResults.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new PostMediaAdapter(filteredPosts, post -> {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("POST_ID", post.getId());
            startActivity(intent);
        });
        rvResults.setAdapter(adapter);

        loadPostsFromFirebase();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPosts(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadPostsFromFirebase() {
        FirebaseFirestore.getInstance().collection("Posts")
            .addSnapshotListener((value, error) -> {
                if (error != null || value == null) return;
                allPosts.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Post post = doc.toObject(Post.class);
                    // Only load posts that actually have images (Artworks for explore page)
                    if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                        allPosts.add(post);
                    }
                }
                allPosts.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                
                // If user is already typing, refresh the filter immediately
                filterPosts(etSearch.getText().toString().trim());
            });
    }

    private void filterPosts(String query) {
        if (query.isEmpty()) {
            llSuggestions.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.GONE);
            return;
        }

        llSuggestions.setVisibility(View.GONE);
        filteredPosts.clear();
        String lowerQuery = query.toLowerCase();
        
        for (Post post : allPosts) {
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            // We match by post caption/content
            if (content.contains(lowerQuery)) {
                filteredPosts.add(post);
            }
        }
        
        if (filteredPosts.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            rvResults.setVisibility(View.VISIBLE);
            llEmpty.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
        }
    }
}
