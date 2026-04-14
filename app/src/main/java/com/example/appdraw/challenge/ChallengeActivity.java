package com.example.appdraw.challenge;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.appdraw.R;

public class ChallengeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        Toolbar toolbar = findViewById(R.id.toolbar_challenge);
        toolbar.setNavigationOnClickListener(v -> finish());

        android.view.View btnAddChallenge = findViewById(R.id.btn_add_challenge);
        if (btnAddChallenge != null) {
            btnAddChallenge.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(this, com.example.appdraw.challenge.CreateChallengeActivity.class);
                startActivity(intent);
            });
        }
        
        setupData();
    }
    
    private androidx.recyclerview.widget.RecyclerView rvChallenges;
    private ChallengeAdapter challengeAdapter;
    private java.util.List<com.google.firebase.firestore.DocumentSnapshot> challengeList = new java.util.ArrayList<>();
    private java.util.List<com.google.firebase.firestore.DocumentSnapshot> allChallengeList = new java.util.ArrayList<>();
    private com.facebook.shimmer.ShimmerFrameLayout shimmerContainer;
    private android.widget.LinearLayout llEmptyState;

    private void setupData() {
        rvChallenges = findViewById(R.id.rv_challenges);
        shimmerContainer = findViewById(R.id.shimmer_view_container);
        llEmptyState = findViewById(R.id.ll_empty_state);
        
        if (shimmerContainer != null) {
            shimmerContainer.setVisibility(android.view.View.VISIBLE);
            shimmerContainer.startShimmer();
        }
        if (rvChallenges != null) rvChallenges.setVisibility(android.view.View.GONE);
        if (llEmptyState != null) llEmptyState.setVisibility(android.view.View.GONE);

        if (rvChallenges != null) {
            rvChallenges.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            challengeAdapter = new ChallengeAdapter(this, challengeList);
            rvChallenges.setAdapter(challengeAdapter);
            
            setupTabs();
            loadChallengesFromFirestore();
        }
    }

    private void setupTabs() {
        com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout_challenge);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    filterListByTab(tab.getPosition());
                }
                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            });
        }
    }
    
    private void filterListByTab(int tabIndex) {
        if (allChallengeList.isEmpty()) return;
        
        challengeList.clear();
        long now = System.currentTimeMillis();
        long oneWeek = 7L * 24 * 60 * 60 * 1000;
        long oneMonth = 30L * 24 * 60 * 60 * 1000;

        for (com.google.firebase.firestore.DocumentSnapshot doc : allChallengeList) {
            Long endTime = doc.getLong("endTimeMillis");
            if (endTime == null) endTime = Long.MAX_VALUE;
            
            if (tabIndex == 0) { // Tất cả
                challengeList.add(doc);
            } else if (tabIndex == 1) { // Tuần này
                if (endTime > now && (endTime - now) <= oneWeek) {
                    challengeList.add(doc);
                }
            } else if (tabIndex == 2) { // Tháng này
                if (endTime > now && (endTime - now) <= oneMonth) {
                    challengeList.add(doc);
                }
            } else if (tabIndex == 3) { // Đã kết thúc
                if (endTime <= now) {
                    challengeList.add(doc);
                }
            } else if (tabIndex == 4) { // Chấm điểm
                challengeList.add(doc); // Or keep specific logic for grading
            }
        }
        
        updateUIState();
    }
    
    private void updateUIState() {
        if (shimmerContainer != null) {
            shimmerContainer.stopShimmer();
            shimmerContainer.setVisibility(android.view.View.GONE);
        }
        
        if (challengeList.isEmpty()) {
            if (rvChallenges != null) rvChallenges.setVisibility(android.view.View.GONE);
            if (llEmptyState != null) llEmptyState.setVisibility(android.view.View.VISIBLE);
        } else {
            if (rvChallenges != null) rvChallenges.setVisibility(android.view.View.VISIBLE);
            if (llEmptyState != null) llEmptyState.setVisibility(android.view.View.GONE);
            if (challengeAdapter != null) challengeAdapter.notifyDataSetChanged();
        }
    }

    private void loadChallengesFromFirestore() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isMentorUser = false;
                    String mentorNameStr = null;
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        isMentorUser = "mentor".equalsIgnoreCase(role);
                        
                        java.util.Map<String, Object> profile = (java.util.Map<String, Object>) documentSnapshot.get("profile");
                        if (profile != null && profile.containsKey("fullName")) {
                            mentorNameStr = "Mentor: " + profile.get("fullName");
                        }
                    }
                    if (challengeAdapter != null) {
                        challengeAdapter.setMentor(isMentorUser, mentorNameStr);
                    }
                    
                    android.view.View btnAddChallenge = findViewById(R.id.btn_add_challenge);
                    if (btnAddChallenge != null) {
                        btnAddChallenge.setVisibility(isMentorUser ? android.view.View.VISIBLE : android.view.View.GONE);
                    }
                    
                    com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout_challenge);
                    if (tabLayout != null && !isMentorUser && tabLayout.getTabCount() >= 5) {
                        tabLayout.removeTabAt(4);
                    }

                    fetchChallengesData(db);
                })
                .addOnFailureListener(e -> fetchChallengesData(db));
        } else {
            fetchChallengesData(db);
            
            android.view.View btnAddChallenge = findViewById(R.id.btn_add_challenge);
            if (btnAddChallenge != null) btnAddChallenge.setVisibility(android.view.View.GONE);
            
            com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout_challenge);
            if (tabLayout != null && tabLayout.getTabCount() >= 5) tabLayout.removeTabAt(4);
        }
    }

    private void fetchChallengesData(com.google.firebase.firestore.FirebaseFirestore db) {
        db.collection("Challenges")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                allChallengeList.clear();
                
                java.util.List<com.google.firebase.firestore.DocumentSnapshot> docs = new java.util.ArrayList<>(queryDocumentSnapshots.getDocuments());
                docs.sort((doc1, doc2) -> {
                    Long end1 = doc1.getLong("endTimeMillis");
                    Long end2 = doc2.getLong("endTimeMillis");
                    if(end1 == null) end1 = Long.MAX_VALUE;
                    if(end2 == null) end2 = Long.MAX_VALUE;
                    
                    long now = System.currentTimeMillis();
                    boolean active1 = end1 > now;
                    boolean active2 = end2 > now;
                    
                    if(active1 && !active2) return -1;
                    if(!active1 && active2) return 1;
                    return end1.compareTo(end2);
                });
                
                allChallengeList.addAll(docs);
                
                com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout_challenge);
                int selectedTab = tabLayout != null ? tabLayout.getSelectedTabPosition() : 0;
                filterListByTab(selectedTab);
            })
            .addOnFailureListener(e -> {
                updateUIState();
                android.widget.Toast.makeText(this, "Lỗi khi tải thử thách", android.widget.Toast.LENGTH_SHORT).show();
            });
    }
}
