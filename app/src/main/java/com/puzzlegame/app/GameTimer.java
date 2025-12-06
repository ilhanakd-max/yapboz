package com.puzzlegame.app;

import android.os.Handler;
import android.os.Looper;

public class GameTimer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startTime = 0;
    private boolean running = false;
    private TimerListener listener;

    public interface TimerListener {
        void onTick(String formattedTime);
    }

    public void setListener(TimerListener listener) {
        this.listener = listener;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
        handler.post(timerRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(timerRunnable);
    }

    public void reset() {
        stop();
        startTime = 0;
        if (listener != null) {
            listener.onTick("00:00");
        }
    }

    public String getFormattedTime() {
        long elapsed = System.currentTimeMillis() - startTime;
        int seconds = (int) (elapsed / 1000) % 60;
        int minutes = (int) (elapsed / 1000) / 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                if (listener != null) {
                    listener.onTick(getFormattedTime());
                }
                handler.postDelayed(this, 1000);
            }
        }
    };
}
