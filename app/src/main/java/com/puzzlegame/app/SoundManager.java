package com.puzzlegame.app;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class SoundManager {
    private static final int SAMPLE_RATE = 44100;
    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void playTap() {
        if (!enabled) return;
        playTone(600, 800, 80);
    }

    public void playSwap() {
        if (!enabled) return;
        playTone(400, 600, 120);
    }

    public void playCorrect() {
        if (!enabled) return;
        new Thread(() -> {
            playToneSync(523, 523, 100);
            playToneSync(659, 659, 100);
            playToneSync(784, 784, 100);
        }).start();
    }

    public void playShuffle() {
        if (!enabled) return;
        playTone(300, 600, 150);
    }

    public void playWin() {
        if (!enabled) return;
        new Thread(() -> {
            int[] notes = {523, 587, 659, 784, 880, 1047};
            for (int note : notes) {
                playToneSync(note, note, 120);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    private void playTone(int startFreq, int endFreq, int durationMs) {
        new Thread(() -> playToneSync(startFreq, endFreq, durationMs)).start();
    }

    private void playToneSync(int startFreq, int endFreq, int durationMs) {
        try {
            int numSamples = (durationMs * SAMPLE_RATE) / 1000;
            double[] samples = new double[numSamples];
            byte[] buffer = new byte[2 * numSamples];

            for (int i = 0; i < numSamples; i++) {
                double freq = startFreq + (endFreq - startFreq) * ((double) i / numSamples);
                samples[i] = Math.sin(2 * Math.PI * freq * i / SAMPLE_RATE);

                double envelope = 1.0;
                if (i < numSamples * 0.1) {
                    envelope = i / (numSamples * 0.1);
                } else if (i > numSamples * 0.7) {
                    envelope = (numSamples - i) / (numSamples * 0.3);
                }
                samples[i] *= envelope;
            }

            int idx = 0;
            for (double sample : samples) {
                short val = (short) (sample * 32767 * 0.3);
                buffer[idx++] = (byte) (val & 0x00ff);
                buffer[idx++] = (byte) ((val & 0xff00) >>> 8);
            }

            AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffer.length,
                AudioTrack.MODE_STATIC
            );

            audioTrack.write(buffer, 0, buffer.length);
            audioTrack.play();

            Thread.sleep(durationMs + 50);
            audioTrack.release();
        } catch (Exception ignored) {
        }
    }
}
