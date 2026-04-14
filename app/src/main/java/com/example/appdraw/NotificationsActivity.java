package com.example.appdraw;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appdraw.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar_notifications);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        rvNotifications = findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(this, notificationList);
        rvNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        
        String uid = auth.getUid();
        FirebaseFirestore.getInstance().collection("Notifications")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        notificationList.clear();
                        for (DocumentSnapshot doc : value) {
                            Notification notif = doc.toObject(Notification.class);
                            if (notif != null) {
                                notif.setId(doc.getId());
                                notificationList.add(notif);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
