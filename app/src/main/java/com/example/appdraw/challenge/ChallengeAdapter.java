package com.example.appdraw.challenge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    private final Context context;
    private final List<DocumentSnapshot> challengeList;
    private boolean isMentor = false;
    private String mentorName = null;

    public ChallengeAdapter(Context context, List<DocumentSnapshot> challengeList) {
        this.context = context;
        this.challengeList = challengeList;
    }

    public void setMentor(boolean mentor, String mentorName) {
        isMentor = mentor;
        this.mentorName = mentorName;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_challenge_list, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        DocumentSnapshot doc = challengeList.get(position);
        
        String title = doc.getString("title");
        String dateStr = doc.getString("dateStr");
        String participantsCount = doc.getString("participantsCount");
        String imageUrl = doc.getString("imageUrl");
        String imageRes = doc.getString("imageRes");

        if (title != null) holder.tvTitle.setText("Thử thách: " + title);
        if (dateStr != null) holder.tvDeadline.setText("Thời gian: " + dateStr);
        if (participantsCount != null) holder.tvParticipants.setText(participantsCount);

        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            try {
                String base64Image = imageUrl.split(",")[1];
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(context).load(decodedByte).centerCrop().into(holder.ivThumb);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (imageRes != null) {
            try {
                int resId = Integer.parseInt(imageRes);
                holder.ivThumb.setImageResource(resId);
            } catch (Exception e) {
                holder.ivThumb.setImageResource(R.drawable.ve_hoa_mau_nuoc);
            }
        } else {
            holder.ivThumb.setImageResource(R.drawable.ve_hoa_mau_nuoc);
        }

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String authorId = doc.getString("authorId");
        String author = doc.getString("author");

        holder.btnSecondary.setVisibility(View.GONE);
        holder.btnSecondary.setOnClickListener(null);

        Long endTimeMillis = doc.getLong("endTimeMillis");
        boolean isEnded = (endTimeMillis != null && endTimeMillis < System.currentTimeMillis());

        boolean isAuthor = false;
        if (user != null && authorId != null && authorId.equals(user.getUid())) {
            isAuthor = true;
        } else if (authorId == null && author != null && mentorName != null && author.equals(mentorName)) {
            isAuthor = true;
        }

        if (isEnded) {
            holder.btnAction.setText("Xem kết quả");
            holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#9E9E9E")));
        } else if (isAuthor) {
            holder.btnAction.setText("Quản lý");
            holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2D5A9E")));
        } else {
            holder.btnAction.setText("Tham gia");
            holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2D5A9E")));
            if (isMentor) {
                holder.btnSecondary.setVisibility(View.GONE);
                holder.btnAction.setText("Chấm điểm bài");
                holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
            } else if (user != null) {
                String challengeTitle = doc.getString("title");
                if (challengeTitle != null) {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("Users").document(user.getUid())
                        .collection("joinedChallenges").document(challengeTitle)
                        .get().addOnSuccessListener(shot -> {
                            if (shot.exists()) {
                                String status = shot.getString("status");
                                if ("SUBMITTED".equals(status) || "GRADED".equals(status)) {
                                    holder.btnAction.setText("Đã nộp");
                                    holder.btnAction.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50")));
                                } else if ("JOINED".equals(status)) {
                                    holder.btnAction.setText("Tiếp tục");
                                }
                            }
                        });
                }
            }
        }

        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, ChallengeDetailActivity.class);
            intent.putExtra("CHALLENGE_ID", doc.getId());
            intent.putExtra("CHALLENGE_TITLE", doc.getString("title"));
            intent.putExtra("CHALLENGE_IMAGE_URL", doc.getString("imageUrl"));
            intent.putExtra("CHALLENGE_RULES", doc.getString("rules"));
            intent.putExtra("CHALLENGE_REWARDS", doc.getString("rewards"));
            intent.putExtra("CHALLENGE_DEADLINE", doc.getString("dateStr"));
            context.startActivity(intent);
        });

        holder.btnAction.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(context, ChallengeDetailActivity.class);
            intent.putExtra("CHALLENGE_ID", doc.getId());
            intent.putExtra("CHALLENGE_TITLE", doc.getString("title"));
            intent.putExtra("CHALLENGE_IMAGE_URL", doc.getString("imageUrl"));
            intent.putExtra("CHALLENGE_RULES", doc.getString("rules"));
            intent.putExtra("CHALLENGE_REWARDS", doc.getString("rewards"));
            intent.putExtra("CHALLENGE_DEADLINE", doc.getString("dateStr"));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvDeadline, tvParticipants;
        MaterialButton btnAction, btnSecondary;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_challenge_thumb);
            tvTitle = itemView.findViewById(R.id.tv_challenge_title);
            tvDeadline = itemView.findViewById(R.id.tv_challenge_deadline);
            tvParticipants = itemView.findViewById(R.id.tv_challenge_participants);
            btnAction = itemView.findViewById(R.id.btn_challenge_action);
            btnSecondary = itemView.findViewById(R.id.btn_challenge_action_secondary);
        }
    }
}
