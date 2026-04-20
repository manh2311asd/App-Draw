package com.example.appdraw.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.appdraw.R;
import com.example.appdraw.explore.ChatActivity;

public class FloatingChatbotManager implements Application.ActivityLifecycleCallbacks {

    private static FloatingChatbotManager instance;
    private View floatingView;
    private int currentX = -1;
    private int currentY = -1;

    private FloatingChatbotManager() {}

    public static FloatingChatbotManager getInstance() {
        if (instance == null) {
            instance = new FloatingChatbotManager();
        }
        return instance;
    }

    public void init(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }

    // Exclude specific screens from displaying the floating widget
    private boolean isExcluded(Activity activity) {
        String name = activity.getClass().getSimpleName();
        return name.equals("SplashActivity") ||
               name.equals("ChatActivity") || 
               name.equals("LoginOptionsActivity") || 
               name.equals("LoginActivity") ||
               name.equals("RegisterActivity") ||
               name.equals("RegisterProfileActivity") ||
               name.equals("RegisterInterestsActivity") ||
               name.equals("RegisterLevelActivity") ||
               name.equals("LiveListActivity") ||
               name.equals("LiveActivity");
    }

    private void attachToActivity(Activity activity) {
        if (isExcluded(activity)) return;

        ViewGroup root = activity.findViewById(android.R.id.content);
        if (root == null) return;

        // Ensure previously added view is removed just in case (e.g. Activity recreation)
        if (floatingView != null && floatingView.getParent() != null) {
             ((ViewGroup) floatingView.getParent()).removeView(floatingView);
        }

        // Inflate a fresh view bound to the current Activity's context
        floatingView = LayoutInflater.from(activity).inflate(R.layout.layout_floating_chatbot, root, false);
        
        setupTouchListener(floatingView, activity);

        root.post(() -> {
            if (currentX == -1 && currentY == -1) {
                // Initialize to bottom-right corner
                int screenWidth = root.getWidth();
                int screenHeight = root.getHeight();
                
                // Convert dp to px for margins
                int marginDpX = 16;
                int marginDpY = 120; // Enough to sit above bottom nav menus
                int marginPxX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDpX, activity.getResources().getDisplayMetrics());
                int marginPxY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDpY, activity.getResources().getDisplayMetrics());

                currentX = screenWidth - floatingView.getWidth() - marginPxX;
                currentY = screenHeight - floatingView.getHeight() - marginPxY;
            }
            updatePosition();
        });

        root.addView(floatingView);
        updatePosition(); // Apply any known position right away
    }

    private void detachFromActivity(Activity activity) {
        if (isExcluded(activity)) return;
        
        if (floatingView != null && floatingView.getParent() != null) {
            ((ViewGroup) floatingView.getParent()).removeView(floatingView);
        }
        floatingView = null; // Clear reference to avoid context leak
    }

    private void updatePosition() {
        if (floatingView != null && currentX != -1 && currentY != -1) {
            floatingView.setX(currentX);
            floatingView.setY(currentY);
        }
    }

    private void setupTouchListener(View view, Activity activity) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY, initialTouchX, initialTouchY;
            private long touchStartTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = view.getX();
                        initialY = view.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        touchStartTime = System.currentTimeMillis();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initialTouchX;
                        float dy = event.getRawY() - initialTouchY;
                        currentX = (int) (initialX + dx);
                        currentY = (int) (initialY + dy);
                        
                        // Keep within screen bounds
                        ViewGroup root = activity.findViewById(android.R.id.content);
                        if (root != null) {
                            int maxW = root.getWidth() - view.getWidth();
                            int maxH = root.getHeight() - view.getHeight();
                            
                            if (currentX < 0) currentX = 0;
                            if (currentX > maxW) currentX = maxW;
                            if (currentY < 0) currentY = 0;
                            if (currentY > maxH) currentY = maxH;
                        }
                        
                        updatePosition();
                        return true;

                    case MotionEvent.ACTION_UP:
                        long touchDuration = System.currentTimeMillis() - touchStartTime;
                        float moveDx = Math.abs(event.getRawX() - initialTouchX);
                        float moveDy = Math.abs(event.getRawY() - initialTouchY);
                        
                        // If tap duration is short and it didn't move much, treat as a click
                        if (touchDuration < 200 && moveDx < 10 && moveDy < 10) {
                            openChatActivity(activity);
                            // Avoid registering the click multiple times in rapid succession
                            touchStartTime = 0;
                        } else {
                            // Snap to edges horizontally if desired (optional polish)
                            /*
                            ViewGroup root = activity.findViewById(android.R.id.content);
                            if (root != null) {
                                int midScreen = root.getWidth() / 2;
                                if (currentX + view.getWidth()/2 < midScreen) {
                                    currentX = 0; // snap to left
                                } else {
                                    currentX = root.getWidth() - view.getWidth(); // snap to right
                                }
                                updatePosition();
                            }
                            */
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void openChatActivity(Activity activity) {
        Intent intent = new Intent(activity, ChatActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Optional depending on stack back requirements
        activity.startActivity(intent);
    }

    // --- Lifecycle Callbacks --- //

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        attachToActivity(activity);
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        detachFromActivity(activity);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {}
}
