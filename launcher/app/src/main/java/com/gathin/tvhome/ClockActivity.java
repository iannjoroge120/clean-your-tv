package com.gathin.tvhome;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * The clock screensaver shown by Home itself after home_idle_clock_minutes without input. Some TVs' own
 * screens (CVTE's live TV) do not keep the screen on, so the system screensaver would cut into programmes;
 * this one only ever starts from Home. Any key returns to Home.
 */
public class ClockActivity extends Activity {
    @Override
    protected void attachBaseContext(android.content.Context base) {
        super.attachBaseContext(LauncherLocale.wrap(base));   // locale
    }


    private ClockScene scene;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView score;
    private boolean scoreFetching;
    /** Bumped on every pause, so a fetch that returns after it does not touch the screen. */
    private int scoreRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dream_clock);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        scene = new ClockScene(getWindow().getDecorView());
        scene.useHomeWallpaper(this);
        // clock_keep_screen_on: the TV's own sleep timer leaves the clock up, like a desk clock.
        if (getResources().getBoolean(R.bool.clock_keep_screen_on)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (!getString(R.string.football_leagues).trim().isEmpty()) score = findViewById(R.id.dreamScore);
    }

    /** The live score: every minute around a match, every 15 minutes otherwise, while the clock is on screen. */
    private final Runnable scoreTick = new Runnable() {
        @Override
        public void run() {
            if (score == null || scoreFetching) return;
            scoreFetching = true;
            int run = scoreRun;
            String leagues = getString(R.string.football_leagues);
            new Thread(() -> {
                Football.Result r = Football.fetch(leagues);
                handler.post(() -> {
                    scoreFetching = false;
                    if (run != scoreRun) return;
                    if (r != null) {
                        score.setText(r.line == null ? "" : r.line);
                        score.setVisibility(r.line == null ? View.GONE : View.VISIBLE);
                    }
                    handler.postDelayed(scoreTick, r != null && r.busy ? Football.LIVE_MS : Football.IDLE_MS);
                });
            }, "clock-football").start();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        scene.start();
        handler.post(scoreTick);
    }

    @Override
    protected void onPause() {
        scene.stop();
        scoreRun++;
        handler.removeCallbacks(scoreTick);
        super.onPause();
        finish();   // never left behind in the back stack
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // The key that wakes the clock should not also act on Home, so it is consumed here.
        if (event.getAction() == KeyEvent.ACTION_UP) finish();
        return true;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
