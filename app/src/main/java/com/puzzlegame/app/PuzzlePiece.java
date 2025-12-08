package com.puzzlegame.app;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class PuzzlePiece {
    private Bitmap bitmap;
    private int originalIndex;
    private int currentIndex;
    private final RectF bounds;
    private boolean isCorrect;
    private boolean isSelected;
    private boolean isHinted;

    public PuzzlePiece(Bitmap bitmap, int originalIndex) {
        this.bitmap = bitmap;
        this.originalIndex = originalIndex;
        this.currentIndex = originalIndex;
        this.bounds = new RectF();
        this.isCorrect = false;
        this.isSelected = false;
        this.isHinted = false;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public int getOriginalIndex() {
        return originalIndex;
    }

    public void setOriginalIndex(int index) {
        this.originalIndex = index;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    public RectF getBounds() {
        return bounds;
    }

    public void setBounds(float left, float top, float right, float bottom) {
        bounds.set(left, top, right, bottom);
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        this.isCorrect = correct;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isHinted() {
        return isHinted;
    }

    public void setHinted(boolean hinted) {
        this.isHinted = hinted;
    }

    public boolean contains(float x, float y) {
        return bounds.contains(x, y);
    }
}
