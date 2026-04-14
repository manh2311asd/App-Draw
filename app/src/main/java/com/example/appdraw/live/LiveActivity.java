package com.example.appdraw.live;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appdraw.R;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment;

public class LiveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        boolean isHost = getIntent().getBooleanExtra("IS_HOST", false);
        String liveID = getIntent().getStringExtra("LIVE_ID");
        String userID = getIntent().getStringExtra("USER_ID");
        String userName = getIntent().getStringExtra("USER_NAME");

        long appID = 1789482713L;
        String appSign = "0b541e07e352f0a8438a7d9f0f4e329b1e0093bedd72d6f9e0e59b27e68e358b";

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        boolean isHost = getIntent().getBooleanExtra("IS_HOST", false);
        String liveID = getIntent().getStringExtra("LIVE_ID");

        if (isHost && liveID != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("Livestreams").document(liveID).delete();
        }
    }
}
