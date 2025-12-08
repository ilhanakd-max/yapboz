package com.puzzlegame.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements PuzzleView.PuzzleListener, GameTimer.TimerListener {

    private PreferenceManager preferenceManager;
    private SoundManager soundManager;
    private GameTimer gameTimer;

    private PuzzleView puzzleView;
    private ImageView imgPreview;
    private TextView txtTimer, txtMoves, txtCorrect, txtProgress;
    private ProgressBar progressBar;
    private Button btnSound, btnEasy, btnMedium, btnHard;
    private FrameLayout puzzleContainer;

    private int moves = 0;
    private int gridSize = 2;
    private boolean gameStarted = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                loadImage(imageUri);
            }
        }
    );

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
        new ActivityResultContracts.RequestPermission(),
        isGranted -> {
            if (isGranted) {
                openImagePicker();
            } else {
                Toast.makeText(this, R.string.upload_first, Toast.LENGTH_SHORT).show();
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(this);
        soundManager = new SoundManager();
        soundManager.setEnabled(preferenceManager.isSoundEnabled());
        gameTimer = new GameTimer();
        gameTimer.setListener(this);

        Utils.setLocale(this, preferenceManager.getLanguage());

        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        updateSoundButton();
        updateDifficultyButtons();
    }

    private void initViews() {
        puzzleContainer = findViewById(R.id.puzzleContainer);
        imgPreview = findViewById(R.id.imgPreview);
        txtTimer = findViewById(R.id.txtTimer);
        txtMoves = findViewById(R.id.txtMoves);
        txtCorrect = findViewById(R.id.txtCorrect);
        txtProgress = findViewById(R.id.txtProgress);
        progressBar = findViewById(R.id.progressBar);
        btnSound = findViewById(R.id.btnSound);
        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);

        puzzleView = new PuzzleView(this);
        puzzleView.setListener(this);
        puzzleView.setGridSize(gridSize);
        puzzleContainer.addView(puzzleView);
    }

    private void setupListeners() {
        findViewById(R.id.btnUpload).setOnClickListener(v -> {
            soundManager.playTap();
            checkPermissionAndPick();
        });

        findViewById(R.id.btnShuffle).setOnClickListener(v -> {
            soundManager.playShuffle();
            shufflePuzzle();
        });

        findViewById(R.id.btnHint).setOnClickListener(v -> {
            soundManager.playTap();
            showHint();
        });

        btnSound.setOnClickListener(v -> {
            boolean newState = !soundManager.isEnabled();
            soundManager.setEnabled(newState);
            preferenceManager.setSoundEnabled(newState);
            updateSoundButton();
            if (newState) soundManager.playTap();
        });

        findViewById(R.id.btnLanguage).setOnClickListener(v -> {
            soundManager.playTap();
            showLanguageDialog();
        });

        btnEasy.setOnClickListener(v -> setDifficulty(2));
        btnMedium.setOnClickListener(v -> setDifficulty(3));
        btnHard.setOnClickListener(v -> setDifficulty(4));
    }

    private void checkPermissionAndPick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadImage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                imgPreview.setImageBitmap(bitmap);
                puzzleView.setImage(bitmap);
                startNewGame();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.upload_first, Toast.LENGTH_SHORT).show();
        }
    }

    private void startNewGame() {
        moves = 0;
        txtMoves.setText(getString(R.string.zero));
        gameTimer.reset();
        gameTimer.start();
        gameStarted = true;
        updateProgress();

        Toast.makeText(this, R.string.tap_to_swap, Toast.LENGTH_SHORT).show();
    }

    private void shufflePuzzle() {
        if (puzzleView.getOriginalImage() == null) {
            Toast.makeText(this, R.string.upload_first, Toast.LENGTH_SHORT).show();
            return;
        }
        puzzleView.shuffle();
        startNewGame();
    }

    private void showHint() {
        if (puzzleView.getOriginalImage() == null) {
            Toast.makeText(this, R.string.upload_first, Toast.LENGTH_SHORT).show();
            return;
        }
        puzzleView.showHint();
    }

    private void setDifficulty(int size) {
        soundManager.playTap();
        gridSize = size;
        puzzleView.setGridSize(size);
        updateDifficultyButtons();

        if (puzzleView.getOriginalImage() != null) {
            startNewGame();
        }
    }

    private void updateDifficultyButtons() {
        btnEasy.setBackground(ContextCompat.getDrawable(this,
            gridSize == 2 ? R.drawable.btn_difficulty_selected : R.drawable.btn_difficulty));
        btnMedium.setBackground(ContextCompat.getDrawable(this,
            gridSize == 3 ? R.drawable.btn_difficulty_selected : R.drawable.btn_difficulty));
        btnHard.setBackground(ContextCompat.getDrawable(this,
            gridSize == 4 ? R.drawable.btn_difficulty_selected : R.drawable.btn_difficulty));

        btnEasy.setTextColor(ContextCompat.getColor(this,
            gridSize == 2 ? R.color.primary_dark : R.color.white));
        btnMedium.setTextColor(ContextCompat.getColor(this,
            gridSize == 3 ? R.color.primary_dark : R.color.white));
        btnHard.setTextColor(ContextCompat.getColor(this,
            gridSize == 4 ? R.color.primary_dark : R.color.white));
    }

    private void updateSoundButton() {
        btnSound.setText(soundManager.isEnabled() ?
            getString(R.string.sound_on) : getString(R.string.sound_off));
    }

    private void updateProgress() {
        int total = gridSize * gridSize;
        int correct = puzzleView.getCorrectCount();
        int percent = (correct * 100) / total;

        progressBar.setProgress(percent);
        txtProgress.setText(String.format(getString(R.string.percent_format), percent));
        txtCorrect.setText(String.valueOf(correct));
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.turkish), getString(R.string.english)};

        new AlertDialog.Builder(this)
            .setTitle(R.string.select_language)
            .setItems(languages, (dialog, which) -> {
                String langCode = which == 0 ? "tr" : "en";
                preferenceManager.setLanguage(langCode);
                Utils.setLocale(this, langCode);
                recreate();
            })
            .show();
    }

    private void showCelebrationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_celebration, null);

        TextView txtTimeResult = dialogView.findViewById(R.id.txtTimeResult);
        TextView txtMovesResult = dialogView.findViewById(R.id.txtMovesResult);
        Button btnPlayAgain = dialogView.findViewById(R.id.btnPlayAgain);

        txtTimeResult.setText(String.format(getString(R.string.time_result), gameTimer.getFormattedTime()));
        txtMovesResult.setText(String.format(getString(R.string.moves_result), moves));

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_PuzzleGame_Dialog)
            .setView(dialogView)
            .setCancelable(false)
            .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnPlayAgain.setOnClickListener(v -> {
            soundManager.playTap();
            dialog.dismiss();
            shufflePuzzle();
        });

        dialog.show();
    }

    @Override
    public void onPieceSwapped(int moveDelta, int correctCount) {
        if (gameStarted && moveDelta == 0) {
            moves++;
            txtMoves.setText(String.valueOf(moves));
        }
        soundManager.playSwap();
        updateProgress();

        if (correctCount > 0) {
            soundManager.playCorrect();
        }
    }

    @Override
    public void onPuzzleComplete() {
        gameTimer.stop();
        gameStarted = false;
        soundManager.playWin();

        puzzleView.postDelayed(this::showCelebrationDialog, 500);
    }

    @Override
    public void onTick(String formattedTime) {
        txtTimer.setText(formattedTime);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameStarted) {
            gameTimer.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameStarted && puzzleView.getOriginalImage() != null) {
            gameTimer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameTimer.stop();
    }
}
