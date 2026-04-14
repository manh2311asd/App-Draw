package com.example.appdraw.live;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.example.appdraw.model.LiveRoom;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LiveListActivity extends AppCompatActivity {

    private RecyclerView rvLiveList;
    private TextView tvEmptyLive;
    private FloatingActionButton fabCreateLive;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private LiveRoomAdapter adapter;
    private List<LiveRoom> liveRooms;

    private String currentUserID;
    private String currentUserName = "User";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_list);

        Toolbar toolbar = findViewById(R.id.toolbar_live);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvLiveList = findViewById(R.id.rv_live_list);
        tvEmptyLive = findViewById(R.id.tv_empty_live);
        fabCreateLive = findViewById(R.id.fab_create_live);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        liveRooms = new ArrayList<>();

        rvLiveList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LiveRoomAdapter(liveRooms);
        rvLiveList.setAdapter(adapter);

        if (auth.getCurrentUser() != null) {
            currentUserID = auth.getCurrentUser().getUid();
            checkUserRole();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
        }

        fabCreateLive.setOnClickListener(v -> {
            startHostLivestream();
        });

        listenForLiveRooms();
    }

    private void checkUserRole() {
        db.collection("Users").document(currentUserID).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String role = doc.getString("role");
                if ("mentor".equals(role)) {
                    fabCreateLive.setVisibility(View.VISIBLE);
                }
                
                Map<String, Object> profile = (Map<String, Object>) doc.get("profile");
                if (profile != null && profile.containsKey("fullName")) {
                    currentUserName = (String) profile.get("fullName");
                }
            }
        });
    }

    private void listenForLiveRooms() {
        db.collection("Livestreams").addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            liveRooms.clear();
            if (value != null && !value.isEmpty()) {
                for (QueryDocumentSnapshot doc : value) {
                    LiveRoom room = doc.toObject(LiveRoom.class);
                    room.roomId = doc.getId();
                    liveRooms.add(room);
                }
            }
            adapter.notifyDataSetChanged();

            if (liveRooms.isEmpty()) {
                tvEmptyLive.setVisibility(View.VISIBLE);
                rvLiveList.setVisibility(View.GONE);
            } else {
                tvEmptyLive.setVisibility(View.GONE);
                rvLiveList.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startHostLivestream() {
        // Tạo Live ID ngẫu nhiên hoặc dùng UID làm host
        String liveID = "room_" + currentUserID;
        
        LiveRoom myRoom = new LiveRoom();
        myRoom.hostId = currentUserID;
        myRoom.hostName = currentUserName;
        myRoom.roomName = "Livestream của " + currentUserName;
        // Optionally add an avatar
        
        db.collection("Livestreams").document(liveID).set(myRoom).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(this, LiveActivity.class);
            intent.putExtra("IS_HOST", true);
            intent.putExtra("LIVE_ID", liveID);
            intent.putExtra("USER_ID", currentUserID);
            intent.putExtra("USER_NAME", currentUserName);
            startActivity(intent);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Không thể tạo phòng Live: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private class LiveRoomAdapter extends RecyclerView.Adapter<LiveRoomAdapter.ViewHolder> {
        private List<LiveRoom> rooms;

        public LiveRoomAdapter(List<LiveRoom> rooms) {
            this.rooms = rooms;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_live_room, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LiveRoom room = rooms.get(position);
            holder.tvRoomName.setText(room.roomName);
            holder.tvHostName.setText("Mentor: " + room.hostName);

            // Set avatar if host has one. Here we just query it locally or set default.
            db.collection("Users").document(room.hostId).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Map<String, Object> profile = (Map<String, Object>) doc.get("profile");
                    if (profile != null && profile.containsKey("avatarUrl")) {
                        String avatarUrl = (String) profile.get("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty() && avatarUrl.startsWith("data:image")) {
                            byte[] b = android.util.Base64.decode(avatarUrl.split(",")[1], android.util.Base64.DEFAULT);
                            Glide.with(LiveListActivity.this).load(b).circleCrop().into(holder.ivAvatar);
                        } else if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(LiveListActivity.this).load(avatarUrl).circleCrop().into(holder.ivAvatar);
                        }
                    }
                }
            });

            holder.btnJoinLive.setOnClickListener(v -> {
                Intent intent = new Intent(LiveListActivity.this, LiveActivity.class);
                intent.putExtra("IS_HOST", false);
                intent.putExtra("LIVE_ID", room.roomId);
                intent.putExtra("USER_ID", currentUserID);
                intent.putExtra("USER_NAME", currentUserName);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return rooms.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvRoomName, tvHostName;
            ImageView ivAvatar;
            MaterialButton btnJoinLive;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRoomName = itemView.findViewById(R.id.tv_room_name);
                tvHostName = itemView.findViewById(R.id.tv_host_name);
                ivAvatar = itemView.findViewById(R.id.iv_host_avatar);
                btnJoinLive = itemView.findViewById(R.id.btn_join_live);
            }
        }
    }
}
