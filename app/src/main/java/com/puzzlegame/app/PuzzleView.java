package com.puzzlegame.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PuzzleView extends View {

    private Bitmap originalImage;
    private final List<PuzzlePiece> pieces;
    private int gridSize = 2;
    private int pieceSize;
    private int boardSize;
    private int offsetX, offsetY;
    private int selectedIndex = -1;

    private final Paint borderPaint;
    private final Paint selectedPaint;
    private final Paint correctPaint;
    private final Paint hintPaint;
    private final Paint checkPaint;

    private PuzzleListener listener;

    public interface PuzzleListener {
        void onPieceSwapped(int moveDelta, int correctCount);
        void onPuzzleComplete();
    }

    public PuzzleView(Context context) {
        super(context);
        pieces = new ArrayList<>();
        borderPaint = createBorderPaint();
        selectedPaint = createSelectedPaint();
        correctPaint = createCorrectPaint();
        hintPaint = createHintPaint();
        checkPaint = createCheckPaint();
    }

    public PuzzleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        pieces = new ArrayList<>();
        borderPaint = createBorderPaint();
        selectedPaint = createSelectedPaint();
        correctPaint = createCorrectPaint();
        hintPaint = createHintPaint();
        checkPaint = createCheckPaint();
    }

    public PuzzleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        pieces = new ArrayList<>();
        borderPaint = createBorderPaint();
        selectedPaint = createSelectedPaint();
        correctPaint = createCorrectPaint();
        hintPaint = createHintPaint();
        checkPaint = createCheckPaint();
    }

    public void setListener(PuzzleListener listener) {
        this.listener = listener;
    }

    public void setGridSize(int size) {
        this.gridSize = size;
        if (originalImage != null) {
            createPuzzle();
        }
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setImage(Bitmap bitmap) {
        this.originalImage = Utils.cropToSquare(bitmap);
        createPuzzle();
    }

    public Bitmap getOriginalImage() {
        return originalImage;
    }

    public void shuffle() {
        if (pieces.isEmpty()) return;

        int[] indices = new int[pieces.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = pieces.get(i).getOriginalIndex();
        }
        Utils.shuffleArray(indices);

        for (int i = 0; i < pieces.size(); i++) {
            pieces.get(i).setOriginalIndex(indices[i]);
            pieces.get(i).setSelected(false);
            pieces.get(i).setHinted(false);

            int row = indices[i] / gridSize;
            int col = indices[i] % gridSize;
            Bitmap pieceBitmap = Bitmap.createBitmap(
                originalImage,
                col * pieceSize,
                row * pieceSize,
                pieceSize,
                pieceSize
            );
            pieces.get(i).setBitmap(pieceBitmap);
        }

        selectedIndex = -1;
        checkCorrectPieces();
        invalidate();
    }

    public void showHint() {
        for (PuzzlePiece piece : pieces) {
            piece.setHinted(false);
        }

        for (int i = 0; i < pieces.size(); i++) {
            PuzzlePiece piece = pieces.get(i);
            if (piece.getOriginalIndex() != i) {
                piece.setHinted(true);
                invalidate();

                postDelayed(() -> {
                    piece.setHinted(false);
                    invalidate();
                }, 2000);

                break;
            }
        }
    }

    private void createPuzzle() {
        if (originalImage == null) return;

        pieces.clear();
        selectedIndex = -1;

        int viewSize = Math.min(getWidth(), getHeight());
        if (viewSize == 0) {
            post(this::createPuzzle);
            return;
        }

        boardSize = viewSize - 40;
        pieceSize = boardSize / gridSize;
        boardSize = pieceSize * gridSize;

        offsetX = (getWidth() - boardSize) / 2;
        offsetY = (getHeight() - boardSize) / 2;

        Bitmap scaledImage = Utils.scaleBitmap(originalImage, boardSize);
        this.originalImage = scaledImage;

        for (int i = 0; i < gridSize * gridSize; i++) {
            int row = i / gridSize;
            int col = i % gridSize;

            Bitmap pieceBitmap = Bitmap.createBitmap(
                scaledImage,
                col * pieceSize,
                row * pieceSize,
                pieceSize,
                pieceSize
            );

            PuzzlePiece piece = new PuzzlePiece(pieceBitmap, i);
            piece.setCurrentIndex(i);

            float left = offsetX + col * pieceSize;
            float top = offsetY + row * pieceSize;
            piece.setBounds(left, top, left + pieceSize, top + pieceSize);

            pieces.add(piece);
        }

        shuffle();
    }

    private void checkCorrectPieces() {
        int correctCount = 0;

        for (int i = 0; i < pieces.size(); i++) {
            PuzzlePiece piece = pieces.get(i);
            boolean isCorrect = piece.getOriginalIndex() == i;
            piece.setCorrect(isCorrect);
            if (isCorrect) correctCount++;
        }

        if (listener != null) {
            listener.onPieceSwapped(0, correctCount);

            if (correctCount == pieces.size()) {
                listener.onPuzzleComplete();
            }
        }
    }

    public int getCorrectCount() {
        int count = 0;
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).getOriginalIndex() == i) count++;
        }
        return count;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (pieces.isEmpty()) return;

        for (int i = 0; i < pieces.size(); i++) {
            PuzzlePiece piece = pieces.get(i);
            RectF bounds = piece.getBounds();

            int row = i / gridSize;
            int col = i % gridSize;
            float left = offsetX + col * pieceSize;
            float top = offsetY + row * pieceSize;

            canvas.drawBitmap(piece.getBitmap(), left, top, null);
            canvas.drawRect(left, top, left + pieceSize, top + pieceSize, borderPaint);

            if (piece.isSelected()) {
                canvas.drawRect(left + 3, top + 3, left + pieceSize - 3, top + pieceSize - 3, selectedPaint);
            } else if (piece.isHinted()) {
                canvas.drawRect(left + 3, top + 3, left + pieceSize - 3, top + pieceSize - 3, hintPaint);
            } else if (piece.isCorrect()) {
                canvas.drawRect(left + 2, top + 2, left + pieceSize - 2, top + pieceSize - 2, correctPaint);
                canvas.drawText("✓", left + pieceSize - 15, top + 20, checkPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            int touchedIndex = -1;

            for (int i = 0; i < pieces.size(); i++) {
                int row = i / gridSize;
                int col = i % gridSize;
                float left = offsetX + col * pieceSize;
                float top = offsetY + row * pieceSize;

                if (x >= left && x <= left + pieceSize && y >= top && y <= top + pieceSize) {
                    touchedIndex = i;
                    break;
                }
            }

            if (touchedIndex >= 0) {
                handlePieceTouch(touchedIndex);
            }

            return true;
        }
        return super.onTouchEvent(event);
    }

    private void handlePieceTouch(int index) {
        if (selectedIndex == -1) {
            selectedIndex = index;
            pieces.get(index).setSelected(true);
        } else if (selectedIndex == index) {
            pieces.get(index).setSelected(false);
            selectedIndex = -1;
        } else {
            swapPieces(selectedIndex, index);
            pieces.get(selectedIndex).setSelected(false);
            selectedIndex = -1;
        }
        invalidate();
    }

    private void swapPieces(int index1, int index2) {
        PuzzlePiece piece1 = pieces.get(index1);
        PuzzlePiece piece2 = pieces.get(index2);

        Bitmap tempBitmap = piece1.getBitmap();
        int tempOriginal = piece1.getOriginalIndex();

        piece1.setBitmap(piece2.getBitmap());
        piece1.setOriginalIndex(piece2.getOriginalIndex());

        piece2.setBitmap(tempBitmap);
        piece2.setOriginalIndex(tempOriginal);

        piece1.setHinted(false);
        piece2.setHinted(false);

        checkCorrectPieces();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (originalImage != null) {
            createPuzzle();
        }
    }

    private Paint createBorderPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        return paint;
    }

    private Paint createSelectedPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        paint.setColor(Color.parseColor("#667EEA"));
        return paint;
    }

    private Paint createCorrectPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.parseColor("#4ECDC4"));
        return paint;
    }

    private Paint createHintPaint() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6);
        paint.setColor(Color.parseColor("#FFE66D"));
        return paint;
    }

    private Paint createCheckPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#4ECDC4"));
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        return paint;
    }
}
