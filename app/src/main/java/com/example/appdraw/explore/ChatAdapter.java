package com.example.appdraw.explore;

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

import com.example.appdraw.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<GeminiVisionService.ChatMessage> messages;

    public ChatAdapter(List<GeminiVisionService.ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        GeminiVisionService.ChatMessage msg = messages.get(position);

        if ("model".equals(msg.role)) {
            holder.llAiMessage.setVisibility(View.VISIBLE);
            holder.llUserMessage.setVisibility(View.GONE);
            holder.tvAiText.setText(msg.text);
        } else {
            holder.llUserMessage.setVisibility(View.VISIBLE);
            holder.llAiMessage.setVisibility(View.GONE);
            holder.tvUserText.setText(msg.text);

            if (msg.base64Image != null && !msg.base64Image.isEmpty()) {
                holder.ivUserImage.setVisibility(View.VISIBLE);
                try {
                    String cleanBase64 = msg.base64Image;
                    if (cleanBase64.contains(",")) {
                        cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
                    }
                    byte[] decodedString = Base64.decode(cleanBase64, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.ivUserImage.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                holder.ivUserImage.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        View llAiMessage, llUserMessage;
        TextView tvAiText, tvUserText;
        ImageView ivUserImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            llAiMessage = itemView.findViewById(R.id.ll_ai_message);
            llUserMessage = itemView.findViewById(R.id.ll_user_message);
            tvAiText = itemView.findViewById(R.id.tv_ai_text);
            tvUserText = itemView.findViewById(R.id.tv_user_text);
            ivUserImage = itemView.findViewById(R.id.iv_user_image);
        }
    }
}
