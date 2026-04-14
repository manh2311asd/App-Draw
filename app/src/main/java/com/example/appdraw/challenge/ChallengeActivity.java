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

    private void setupData() {
        rvChallenges = findViewById(R.id.rv_challenges);
        if (rvChallenges != null) {
            rvChallenges.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
            challengeAdapter = new ChallengeAdapter(this, challengeList);
            rvChallenges.setAdapter(challengeAdapter);
            loadChallengesFromFirestore();
        }
    }

    private void loadChallengesFromFirestore() {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isMentorUser = false;
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        isMentorUser = "mentor".equalsIgnoreCase(role);
                    }
                    if (challengeAdapter != null) {
                        challengeAdapter.setMentor(isMentorUser);
                    }
                    
                    android.view.View btnAddChallenge = findViewById(R.id.btn_add_challenge);
                    if (btnAddChallenge != null) {
                        btnAddChallenge.setVisibility(isMentorUser ? android.view.View.VISIBLE : android.view.View.GONE);
                    }
                    
                    com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout_challenge);
                    if (tabLayout != null && !isMentorUser && tabLayout.getTabCount() >= 4) {
                        tabLayout.removeTabAt(3);
                    }

                    fetchChallengesData(db);
                })
                .addOnFailureListener(e -> fetchChallengesData(db));
        } else {
            fetchChallengesData(db);
            
            android.view.View btnAddChallenge = findViewById(R.id.btn_add_challenge);
            if (btnAddChallenge != null) btnAddChallenge.setVisibility(android.view.View.GONE);
            
            com.google.android.material.tabs.TabLayout tabLayout = findViewById(R.id.tab_layout_challenge);
            if (tabLayout != null && tabLayout.getTabCount() >= 4) tabLayout.removeTabAt(3);
        }
    }

    private void fetchChallengesData(com.google.firebase.firestore.FirebaseFirestore db) {
        db.collection("Challenges")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                challengeList.clear();
                
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
                
                challengeList.addAll(docs);
                
                if (challengeAdapter != null) {
                    challengeAdapter.notifyDataSetChanged();
                }
            })
            .addOnFailureListener(e -> {
                android.widget.Toast.makeText(this, "Lỗi khi tải thử thách", android.widget.Toast.LENGTH_SHORT).show();
            });
    }
}
