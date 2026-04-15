package com.example.appdraw.utils;

import com.example.appdraw.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationHelper {

    public static void sendNotification(String targetUserId, String type, String message, String targetId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;
        String senderId = auth.getUid();
        
        // Don't notify self (unless it's an event system message, maybe?)
        if (senderId.equals(targetUserId) && !type.equals("EVENT")) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Fetch sender info
        db.collection("Users").document(senderId).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String senderName = "Người dùng";
                String senderAvatar = "";
                
                if (userDoc.contains("profile")) {
                    java.util.Map<String, Object> profile = (java.util.Map<String, Object>) userDoc.get("profile");
                    if (profile != null) {
                        senderName = profile.containsKey("fullName") ? (String) profile.get("fullName") : "Người dùng";
                        senderAvatar = profile.containsKey("avatarUrl") ? (String) profile.get("avatarUrl") : "";
                    }
                }

                // Create Notification
                String notifId = db.collection("Notifications").document().getId();
                Notification notification = new Notification(
                        notifId,
                        targetUserId,
                        senderId,
                        senderName,
                        senderAvatar,
                        type,
                        message,
                        targetId,
                        System.currentTimeMillis()
                );

                db.collection("Notifications").document(notifId).set(notification);
            }
        });
    }
}
