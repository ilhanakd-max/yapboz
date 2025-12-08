package com.puzzlegame.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConfettiView extends View {

    private final List<Confetti> confettiList;
    private final Paint paint;
    private final Random random;
    private boolean isAnimating = false;
    private final int[] colors = {
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#FFE66D"),
        Color.parseColor("#A8E6CF"),
        Color.parseColor("#FF8B94"),
        Color.parseColor("#DDA0DD")
    };

    private static class Confetti {
        float x, y;
        float speedY;
        float speedX;
        float rotation;
        float rotationSpeed;
        int color;
        float size;
    }

    public ConfettiView(Context context) {
        super(context);
        confettiList = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        random = new Random();
    }

    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        confettiList = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        random = new Random();
    }

    public ConfettiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        confettiList = new ArrayList<>();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        random = new Random();
    }

    public void startConfetti() {
        confettiList.clear();
        isAnimating = true;

        for (int i = 0; i < 100; i++) {
            Confetti c = new Confetti();
            c.x = random.nextFloat() * getWidth();
            c.y = -random.nextFloat() * getHeight();
            c.speedY = 5 + random.nextFloat() * 10;
            c.speedX = -3 + random.nextFloat() * 6;
            c.rotation = random.nextFloat() * 360;
            c.rotationSpeed = -5 + random.nextFloat() * 10;
            c.color = colors[random.nextInt(colors.length)];
            c.size = 10 + random.nextFloat() * 15;
            confettiList.add(c);
        }

        invalidate();
    }

    public void stopConfetti() {
        isAnimating = false;
        confettiList.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!isAnimating) return;

        boolean hasVisible = false;

        for (Confetti c : confettiList) {
            if (c.y < getHeight()) {
                hasVisible = true;

                paint.setColor(c.color);

                canvas.save();
                canvas.translate(c.x, c.y);
                canvas.rotate(c.rotation);
                canvas.drawRect(-c.size / 2, -c.size / 4, c.size / 2, c.size / 4, paint);
                canvas.restore();

                c.y += c.speedY;
                c.x += c.speedX;
                c.rotation += c.rotationSpeed;
            }
        }

        if (hasVisible) {
            postInvalidateDelayed(16);
        } else {
            isAnimating = false;
        }
    }
}
