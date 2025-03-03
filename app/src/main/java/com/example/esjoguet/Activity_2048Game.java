package com.example.esjoguet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import android.os.Handler;
import android.widget.Toast;

public class Activity_2048Game extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String TAG = "MainActivity";
    private GestureDetector gestureDetector;
    private float startX, startY, endX, endY;

    private static final int GRID_SIZE = 4;

    private static final int MOVE_UP = -1;
    private static final int MOVE_DOWN = 1;
    private static final int MOVE_LEFT = -1;
    private static final int MOVE_RIGHT = 1;
    private static final int NO_MOVE = 0;

    private static final int LAST_ROW = GRID_SIZE - 1; // Última fila válida (3 si GRID_SIZE = 4)
    private static final int LAST_COLUMN = GRID_SIZE - 1; // Última columna válida
    private static final int FIRST_COLUMN = 0; // Primera columna válida
    private static final int FIRST_ROW = 0; // Primera fila válida
    private static final int ALWAYS_TRUE = GRID_SIZE + 1; // Valor imposible dentro del rango de la matriz

    private final Map<Integer, Integer> tileColors = new HashMap<>();
    private Integer[][] board; // valores
    private TextView[][] tiles; // Referencias
    private TextView tvScoreCounter, tvBestScoreCounter, tvMovesCounter, tvTimeCounter, tvUsername;
    private Integer scoreCounter = 0, movesCounter = 0, timeCounter = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private Random random = new Random();
    private Boolean hasGameStarted = false;

    private Integer[][] previousBoard;
    private int previousScore;
    private int previousMoves;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity has been created");
        super.onCreate(savedInstanceState);

        startStuff(savedInstanceState);
        gestureDetector = new GestureDetector(this, this);

        startGameLayout();

        DBAssistant db = new DBAssistant(this);
        tvBestScoreCounter.setText(String.valueOf(db.getUserBestScore(getUsername())));
        tvUsername.setText(getUsername());
        initializeTileColors();

        restoreData(savedInstanceState);

        Button backButton = findViewById(R.id.BackBttn);
        backButton.setOnClickListener(v -> undoLastMove());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(Activity_2048Game.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            restoreData(savedInstanceState);
        } else {
            startGame();
        }
    }

    private void restoreData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            scoreCounter = savedInstanceState.getInt("scoreCounter", 0);
            movesCounter = savedInstanceState.getInt("movesCounter", 0);
            timeCounter = savedInstanceState.getInt("timeCounter", 0);
            hasGameStarted = savedInstanceState.getBoolean("hasGameStarted", false);
            previousScore = savedInstanceState.getInt("previousScore", 0);
            previousMoves = savedInstanceState.getInt("previousMoves", 0);

            //Matriz tablero
            Integer[] flatBoard = (Integer[]) savedInstanceState.getSerializable("board");
            if (flatBoard != null) {
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        board[i][j] = flatBoard[i * GRID_SIZE + j];
                    }
                }
            }

            //matriz tablero previo
            Integer[] flatPreviousBoard = (Integer[]) savedInstanceState.getSerializable("previousBoard");
            if (flatPreviousBoard != null) {
                previousBoard = new Integer[GRID_SIZE][GRID_SIZE];
                for (int i = 0; i < GRID_SIZE; i++) {
                    for (int j = 0; j < GRID_SIZE; j++) {
                        previousBoard[i][j] = flatPreviousBoard[i * GRID_SIZE + j];
                    }
                }
            }

            updateBoardUI();
            tvScoreCounter.setText(String.valueOf(scoreCounter));
            tvMovesCounter.setText(String.valueOf(movesCounter));
            tvTimeCounter.setText(String.format("%02d:%02d", timeCounter / 60, timeCounter % 60));

            if (hasGameStarted) {
                startTimer();
            }
        }
    }

    private void startGame() {
        spawnRandomTile();
        spawnRandomTile();
    }

    private void startTimer() {
        runnable = new Runnable() {
            @Override
            public void run() {
                timeCounter++;

                int minutes = timeCounter / 60;
                int seconds = timeCounter % 60;
                String timeString = String.format("%02d:%02d", minutes, seconds);
                tvTimeCounter.setText(timeString);

                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void stopTimer() {
        handler.removeCallbacks(runnable);
    }

    private void startGameLayout() {
        tvScoreCounter = findViewById(R.id.scoreCounter);
        tvBestScoreCounter = findViewById(R.id.bestScoreCounter);
        tvMovesCounter = findViewById(R.id.movesCounter);
        tvTimeCounter = findViewById(R.id.timeCounter);
        tvUsername = findViewById(R.id.userTextView);

        board = new Integer[GRID_SIZE][GRID_SIZE];
        tiles = new TextView[GRID_SIZE][GRID_SIZE];

        tiles[0][0] = findViewById(R.id.tv1x1);
        tiles[0][1] = findViewById(R.id.tv1x2);
        tiles[0][2] = findViewById(R.id.tv1x3);
        tiles[0][3] = findViewById(R.id.tv1x4);

        tiles[1][0] = findViewById(R.id.tv2x1);
        tiles[1][1] = findViewById(R.id.tv2x2);
        tiles[1][2] = findViewById(R.id.tv2x3);
        tiles[1][3] = findViewById(R.id.tv2x4);

        tiles[2][0] = findViewById(R.id.tv3x1);
        tiles[2][1] = findViewById(R.id.tv3x2);
        tiles[2][2] = findViewById(R.id.tv3x3);
        tiles[2][3] = findViewById(R.id.tv3x4);

        tiles[3][0] = findViewById(R.id.tv4x1);
        tiles[3][1] = findViewById(R.id.tv4x2);
        tiles[3][2] = findViewById(R.id.tv4x3);
        tiles[3][3] = findViewById(R.id.tv4x4);
    }

    private Boolean isEmptyTile(Integer tileValue) {
        return Objects.isNull(tileValue);
    }

    /**
     * Spawns a new tile (either 2 or 4) in a random empty position on the board.
     * A 90% probability is given for a 2 and a 10% probability for a 4.
     * If no empty positions are available, the method does nothing.
     */
    private void spawnRandomTile() {
        ArrayList<Integer[]> emptyTiles = new ArrayList<>();
        searchEmptyTiles(emptyTiles);

        if (emptyTiles.isEmpty()) {
            return;
        }

        Integer[] position = emptyTiles.get(random.nextInt(emptyTiles.size()));
        Integer row = position[0];
        Integer col = position[1];

        Integer value = random.nextFloat() < 0.9 ? 2 : 4;

        board[row][col] = value;
        updateTileUI(row, col, value);
    }


    private void searchEmptyTiles(ArrayList<Integer[]> emptyTiles) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (isEmptyTile(board[i][j])) {
                    emptyTiles.add(new Integer[]{i, j});
                }
            }
        }
    }

    private void initializeTileColors() {
        tileColors.put(2, R.color.tile_2);
        tileColors.put(4, R.color.tile_4);
        tileColors.put(8, R.color.tile_8);
        tileColors.put(16, R.color.tile_16);
        tileColors.put(32, R.color.tile_32);
        tileColors.put(64, R.color.tile_64);
        tileColors.put(128, R.color.tile_128);
        tileColors.put(256, R.color.tile_256);
        tileColors.put(512, R.color.tile_512);
        tileColors.put(1024, R.color.tile_1024);
        tileColors.put(2048, R.color.tile_2048);
        tileColors.put(4096, R.color.tile_4096);
        tileColors.put(8192, R.color.tile_8192);
    }

    private void updateTileUI(Integer row, Integer col, Integer value) {
        TextView tile = tiles[row][col];

        if (value == null || value <= 0) {
            tile.setText("");
        } else {
            tile.setText(String.valueOf(value));
        }

        Integer colorResource = tileColors.getOrDefault(value != null ? value : 0, R.color.tile_default);
        tile.setBackgroundResource(colorResource);
    }

    /**
     * Saves the current state of the board, for a possible future rollback.
     */
    private void savePreviousState() {
        previousBoard = new Integer[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            previousBoard[i] = board[i].clone(); // Copia superficial de la fila
        }

        previousScore = scoreCounter;
        previousMoves = movesCounter;
    }

    /**
     * Restores the previous state of the board and scores.
     */
    private void undoLastMove() {
        if (previousBoard != null) {
            for (int i = 0; i < GRID_SIZE; i++) {
                board[i] = previousBoard[i].clone();
            }
            scoreCounter = previousScore;
            movesCounter = previousMoves;

            updateBoardUI();
            tvScoreCounter.setText(String.valueOf(scoreCounter));
            tvMovesCounter.setText(String.valueOf(movesCounter));
        }
    }

    /**
     * Using updateTileUI searches and updates all the board tiles.
     */
    private void updateBoardUI() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                Integer value = board[row][col];
                updateTileUI(row, col, (value != null) ? value : 0);
            }
        }
    }

    /**
     * Moves the tiles in the given direction and spawns a new tile.
     *
     * @param direction The movement direction:
     *                  - 0: Up
     *                  - 1: Left
     *                  - 2: Right
     *                  - 3: Down
     */
    private void moveTiles(int direction) {
        savePreviousState();
        updateMovesCounter();

        if (!hasGameStarted) {
            hasGameStarted = true;
            startTimer();
        }

        if (direction == 0) {
            moveUp();
        } else if (direction == 1) {
            moveLeft();
        } else if (direction == 2) {
            moveRight();
        } else if (direction == 3) {
            moveDown();
        }

        spawnRandomTile();

        if (isGameOver()) {
            handleGameOver();
        }
    }

    /**
     * Determines if the game is over by checking if there are no empty tiles
     * and no possible moves where adjacent tiles can merge.
     *
     * @return true if no moves are possible, false otherwise.
     */
    private boolean isGameOver() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] == null) {
                    return false;
                }
            }
        }

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int current = board[i][j];
                if (j < GRID_SIZE - 1 && board[i][j + 1].equals(current)) {
                    return false;
                }
                if (i < GRID_SIZE - 1 && board[i + 1][j].equals(current)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void handleGameOver() {
        Toast.makeText(this, "¡Game Over! No hay más movimientos.", Toast.LENGTH_LONG).show();


        DBAssistant dbAssistant = new DBAssistant(this);
        String username = getUsername();
        int userId = dbAssistant.getUserId(username);

        String time = tvTimeCounter.getText().toString();

        dbAssistant.insertGame2048Record(userId, movesCounter, time, scoreCounter);

        resetGame();
    }

    /**
     * Restart the game
     */
    private void resetGame() {
        //timer
        stopTimer();

        //counters
        scoreCounter = 0;
        movesCounter = 0;
        timeCounter = 0;

        //interface
        tvScoreCounter.setText("0");
        tvMovesCounter.setText("0");
        tvTimeCounter.setText("00:00");

        //board
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                board[i][j] = null;
                updateTileUI(i, j, 0);
            }
        }

        //stats
        hasGameStarted = false;
        previousBoard = null;
        previousScore = 0;
        previousMoves = 0;

        //new game
        startGame();
    }

    private String getUsername() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("username", "Guest");
    }

    private void moveDown() {
        for (int i = GRID_SIZE - 1; i >= 0; i--) {
            for (int j = GRID_SIZE - 1; j >= 0; j--) {
                if (!isEmptyTile(board[i][j])) {
                    checkMoveAndAddNearbyTile(i, j, MOVE_DOWN, NO_MOVE, LAST_ROW, ALWAYS_TRUE);
                }
            }
        }
    }

    private void moveRight() {
        for (int i = GRID_SIZE - 1; i >= 0; i--) {
            for (int j = GRID_SIZE - 1; j >= 0; j--) {
                if (!isEmptyTile(board[i][j])) {
                    checkMoveAndAddNearbyTile(i, j, NO_MOVE, MOVE_RIGHT, ALWAYS_TRUE, LAST_COLUMN);
                }
            }
        }
    }

    private void moveLeft() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!isEmptyTile(board[i][j])) {
                    checkMoveAndAddNearbyTile(i, j, NO_MOVE, MOVE_LEFT, ALWAYS_TRUE, FIRST_COLUMN);
                }
            }
        }
    }

    private void moveUp() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!isEmptyTile(board[i][j])) {
                    checkMoveAndAddNearbyTile(i, j, MOVE_UP, NO_MOVE, FIRST_ROW, ALWAYS_TRUE);
                }
            }
        }
    }


    /**
     * Checks if a tile can move or merge with an adjacent tile.
     *
     * @param i                 Current row position.
     * @param j                 Current column position.
     * @param moveRow           Row movement (-1 for up, 1 for down, 0 for none).
     * @param moveColumn        Column movement (-1 for left, 1 for right, 0 for none).
     * @param breakConditionRow Row boundary condition for stopping.
     * @param breakConditionCol Column boundary condition for stopping.
     */
    private void checkMoveAndAddNearbyTile(int i, int j, int moveRow, int moveColumn, int breakConditionRow, int breakConditionCol) {
        int newRow = i + moveRow;
        int newColumn = j + moveColumn;

        while (i != breakConditionRow && j != breakConditionCol) {
            if (!isEmptyTile(board[newRow][newColumn]) && board[newRow][newColumn].equals(board[i][j])) {
                // Merge tiles
                updateTile(i, j, newRow, newColumn, board[i][j] * 2);
                updateScore(newRow, newColumn);
                break;
            } else if (!isEmptyTile(board[newRow][newColumn])) {
                // Can't move further
                break;
            }

            // Move tile
            updateTile(i, j, newRow, newColumn, board[i][j]);

            // Adjust indices
            if (moveRow != 0) {
                i += moveRow;
                newRow += moveRow;
            } else if (moveColumn != 0) {
                j += moveColumn;
                newColumn += moveColumn;
            }
        }
    }

    private void updateMovesCounter() {
        movesCounter++;
        tvMovesCounter.setText(movesCounter.toString());
    }

    private void updateScore(Integer newRow, Integer newColumn) {
        scoreCounter += board[newRow][newColumn];
        tvScoreCounter.setText(scoreCounter.toString());
    }

    private void updateTile(int row, int col, int newRow, int newCol, int value) {
        updateTileUI(newRow, newCol, value);
        updateTileUI(row, col, 0);
        board[newRow][newCol] = value;
        board[row][col] = null;
    }

    /**
     * Handles swipe gestures and moves tiles accordingly.
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling detected with velocityX = " + velocityX + " and velocityY = " + velocityY);

        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (deltaX > 0) {
                moveTiles(2); // Right
            } else {
                moveTiles(1); // Left
            }
        } else {
            if (deltaY > 0) {
                moveTiles(3); // Down
            } else {
                moveTiles(0); // Up
            }
        }
        return true;
    }

    public void startStuff(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity2048_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        board = new Integer[GRID_SIZE][GRID_SIZE];
    }

    @Override
    public boolean onDown(MotionEvent e) {
        startX = e.getX();
        startY = e.getY();
        Log.d(TAG, "onDown: User touched the screen");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "onShowPress: User is pressing on the screen");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(TAG, "onSingleTapUp: Single tap detected");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        endX = e2.getX();
        endY = e2.getY();
        Log.d(TAG, "onScroll: Scroll gesture detected with distanceX = " + distanceX + " and distanceY = " + distanceY);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress: Long press detected");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity is starting");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity has resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Activity is pausing");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity has stopped");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: Activity is restarting");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity is being destroyed");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //de matriz a array
        Integer[] flatBoard = new Integer[GRID_SIZE * GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                flatBoard[i * GRID_SIZE + j] = board[i][j];
            }
        }
        outState.putSerializable("board", flatBoard);

        //de matriz a array previo
        if (previousBoard != null) {
            Integer[] flatPreviousBoard = new Integer[GRID_SIZE * GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    flatPreviousBoard[i * GRID_SIZE + j] = previousBoard[i][j];
                }
            }
            outState.putSerializable("previousBoard", flatPreviousBoard);
        }

        outState.putInt("scoreCounter", scoreCounter);
        outState.putInt("movesCounter", movesCounter);
        outState.putInt("timeCounter", timeCounter);
        outState.putBoolean("hasGameStarted", hasGameStarted);
        outState.putInt("previousScore", previousScore);
        outState.putInt("previousMoves", previousMoves);
    }

}