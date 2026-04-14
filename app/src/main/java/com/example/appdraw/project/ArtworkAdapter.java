package com.example.appdraw.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appdraw.R;
import com.example.appdraw.model.Artwork;

import java.util.List;

public class ArtworkAdapter extends RecyclerView.Adapter<ArtworkAdapter.ViewHolder> {
    private List<Artwork> artworks;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Artwork artwork);
    }

    public ArtworkAdapter(List<Artwork> artworks, OnItemClickListener listener) {
        this.artworks = artworks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artwork_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artwork artwork = artworks.get(position);
        holder.tvTitle.setText(artwork.getTitle());
        
        if (Artwork.STATUS_COMPLETED.equals(artwork.getStatus())) {
            holder.tvStatus.setText("Hoàn thành");
            holder.tvStatus.setTextColor(0xFF2ECC71); // Xanh lá
        } else {
            holder.tvStatus.setText("Đang làm");
            holder.tvStatus.setTextColor(0xFFF0B259); // Vàng
        }

        if (artwork.getImageUrl() != null && !artwork.getImageUrl().isEmpty()) {
            if (artwork.getImageUrl().startsWith("data:image")) {
                byte[] b = android.util.Base64.decode(artwork.getImageUrl().split(",")[1], android.util.Base64.DEFAULT);
                Glide.with(holder.itemView.getContext())
                        .load(b)
                        .fitCenter()
                        .into(holder.ivThumbnail);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(artwork.getImageUrl())
                        .fitCenter()
                        .into(holder.ivThumbnail);
            }
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.backgroud_app_draw); // Ảnh mặc định
        }

        // Format Date
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        String dateStr = sdf.format(new java.util.Date(artwork.getCreatedAt()));
        holder.tvDate.setText(dateStr);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(artwork);
        });
    }

    @Override
    public int getItemCount() {
        return artworks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle, tvStatus, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_artwork);
            tvTitle = itemView.findViewById(R.id.tv_artwork_title);
            tvStatus = itemView.findViewById(R.id.tv_artwork_status);
            tvDate = itemView.findViewById(R.id.tv_artwork_date);
        }
    }
}
