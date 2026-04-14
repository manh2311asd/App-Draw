package com.example.appdraw;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdraw.community.OtherUserProfileActivity;
import com.example.appdraw.community.PostDetailActivity;
import com.example.appdraw.community.EventScheduleActivity;
import com.example.appdraw.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notificationList;
    private Context context;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notificationList.get(position);

        // Name Formatting (Bold)
        String boldName = "<b>" + notif.getSenderName() + "</b>";
        String contentText = "";
        switch (notif.getType()) {
            case "LIKE":
                contentText = boldName + " đã thích bài viết của bạn.";
                break;
            case "COMMENT":
                contentText = boldName + " đã bình luận về bài viết của bạn.";
                break;
            case "FOLLOW":
                contentText = boldName + " đánh giá cao tác phẩm và bắt đầu theo dõi bạn.";
                break;
            case "EVENT":
                contentText = "Hệ thống: " + notif.getMessage();
                break;
            default:
                contentText = boldName + " " + notif.getMessage();
        }
        holder.tvMessage.setText(Html.fromHtml(contentText));

        // Format Time
        long elapsed = System.currentTimeMillis() - notif.getTimestamp();
        long minutes = elapsed / 60000;
        if (minutes < 60) holder.tvTime.setText(minutes + " phút trước");
        else if (minutes < 1440) holder.tvTime.setText((minutes / 60) + " giờ trước");
        else holder.tvTime.setText((minutes / 1440) + " ngày trước");

        // Set Avatar
        if (notif.getSenderAvatar() != null && !notif.getSenderAvatar().isEmpty()) {
            if (notif.getSenderAvatar().startsWith("data:image")) {
                byte[] b = android.util.Base64.decode(notif.getSenderAvatar().split(",")[1], android.util.Base64.DEFAULT);
                Glide.with(context).load(b).circleCrop().into(holder.ivAvatar);
            } else {
                Glide.with(context).load(notif.getSenderAvatar()).circleCrop().into(holder.ivAvatar);
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_default_user);
        }

        // Action button for Follow
        if ("FOLLOW".equals(notif.getType())) {
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setText("+ Theo dõi");
            holder.btnAction.setOnClickListener(v -> {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                if (auth.getCurrentUser() != null) {
                    holder.btnAction.setEnabled(false);
                    String currentUid = auth.getUid();
                    
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> data = new HashMap<>();
                    data.put("follower", currentUid);
                    data.put("following", notif.getSenderId());
                    data.put("timestamp", System.currentTimeMillis());
                    
                    db.collection("Follows").document(currentUid + "_" + notif.getSenderId())
                        .set(data).addOnSuccessListener(aVoid -> {
                            holder.btnAction.setText("Đang theo dõi");
                            holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                            Toast.makeText(context, "Đã theo dõi lại", Toast.LENGTH_SHORT).show();
                        });
                }
            });
        } else {
            holder.btnAction.setVisibility(View.GONE);
        }

        // Parent click action
        holder.itemView.setOnClickListener(v -> {
            // Mark as read in Firestore
            FirebaseFirestore.getInstance().collection("Notifications").document(notif.getId()).update("isRead", true);
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE);

            if ("LIKE".equals(notif.getType()) || "COMMENT".equals(notif.getType())) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("POST_ID", notif.getTargetId());
                context.startActivity(intent);
            } else if ("FOLLOW".equals(notif.getType())) {
                Intent intent = new Intent(context, OtherUserProfileActivity.class);
                intent.putExtra("USER_ID", notif.getSenderId());
                context.startActivity(intent);
            } else if ("EVENT".equals(notif.getType())) {
                Intent intent = new Intent(context, EventScheduleActivity.class);
                context.startActivity(intent);
            }
        });

        if (!notif.isRead()) {
            holder.itemView.setBackgroundColor(android.graphics.Color.parseColor("#E8F0FE"));
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvMessage, tvTime, btnAction;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_notif_avatar);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            btnAction = itemView.findViewById(R.id.btn_notif_action);
        }
    }
}
