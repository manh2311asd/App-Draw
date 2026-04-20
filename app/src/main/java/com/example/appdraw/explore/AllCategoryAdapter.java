package com.example.appdraw.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;

import java.util.List;
import java.util.Map;

public class AllCategoryAdapter extends RecyclerView.Adapter<AllCategoryAdapter.ViewHolder> {
    private List<Map<String, Object>> categories;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String title);
    }

    public AllCategoryAdapter(List<Map<String, Object>> categories, OnItemClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_all_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> cat = categories.get(position);
        
        String title = (String) cat.get("title");
        String count = (String) cat.get("courseCount");
        String imageResStr = (String) cat.get("imageRes");
        String imageUrl = (String) cat.get("imageUrl");

        holder.tvTitle.setText(title != null ? title : "");
        holder.tvCount.setText(count != null ? count : "0 bài học");

        if (imageResStr != null && !imageResStr.isEmpty() && !imageResStr.matches("-?\\d+")) {
            try {
                int resId = holder.itemView.getContext().getResources().getIdentifier(imageResStr, "drawable", holder.itemView.getContext().getPackageName());
                if (resId != 0) holder.ivCat.setImageResource(resId);
            } catch (Exception e) {}
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext()).load(imageUrl).centerCrop().into(holder.ivCat);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(title);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCount;
        ImageView ivCat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_category_name);
            tvCount = itemView.findViewById(R.id.tv_category_count);
            ivCat = itemView.findViewById(R.id.iv_category);
        }
    }
}
