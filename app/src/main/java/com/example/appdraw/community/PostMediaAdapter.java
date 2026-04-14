package com.example.appdraw.community;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.example.appdraw.model.Post;

import java.util.List;

public class PostMediaAdapter extends RecyclerView.Adapter<PostMediaAdapter.ViewHolder> {
    private List<Post> posts;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Post post);
    }

    public PostMediaAdapter(List<Post> posts, OnItemClickListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            if (post.getImageUrl().startsWith("data:image")) {
                String base64Str = post.getImageUrl().substring(post.getImageUrl().indexOf(",") + 1);
                byte[] decodedBytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT);
                Glide.with(holder.itemView.getContext())
                        .load(decodedBytes)
                        .into(holder.ivMedia);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(post.getImageUrl())
                        .into(holder.ivMedia);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(post);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.iv_post_media);
        }
    }
}
