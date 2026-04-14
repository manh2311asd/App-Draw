package com.example.appdraw;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LiveStreamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);

        boolean isHost = getIntent().getBooleanExtra("IS_HOST", false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = (user != null) ? user.getUid() : "test_user_id";
        String userName = isHost ? "Host" : "Viewer"; // Có thể thay bằng dữ liệu thật sau này
        String liveID = isHost ? userID : getIntent().getStringExtra("ROOM_ID");
        if (liveID == null) liveID = "test_room_id";

        // YÊU CẦU: Điền thông tin thật từ ZEGOCLOUD Console vào đây
        long appID = 123456789; 
        String appSign = "test_app_sign_123";

        ZegoUIKitPrebuiltLiveStreamingConfig config;
        if (isHost) {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.host();
        } else {
            config = ZegoUIKitPrebuiltLiveStreamingConfig.audience();
        }

        ZegoUIKitPrebuiltLiveStreamingFragment fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                appID, appSign, userID, userName, liveID, config);
        
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }
}
