package com.example.esjoguet;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.esjoguet.databinding.ActivityStackerGameBinding;

public class Activity_StackerGame extends AppCompatActivity implements GestureDetector.OnGestureListener {

    private static final String TAG = "MainActivity";
    private static final int COLUMNAS = 7;
    private static final int FILAS = 12;
    private static final int CELL_MARGIN = 4;

    private GestureDetector gestureDetector;
    private float startX, startY, endX, endY;
    private Boolean hasGameStarted = false;
    private ImageView[][] gridCells;
    private Handler handler = new Handler();
    private int currentColumn = 0;
    private int direction = 1;
    private final int DELAY = 300;
    private int currentRow = 11;
    private final int INITIAL_BLOCK_SIZE = 3;
    private int BLOCK_SIZE = INITIAL_BLOCK_SIZE;
    private int currentDelay = 300;
    private final int[] FLOOR_DELAYS = {
            25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity has been created");
        super.onCreate(savedInstanceState);
        startStuff(savedInstanceState);
        initializeGestureDetector();
        setUpBoard();
    }
    private void initializeGestureDetector() {
        gestureDetector = new GestureDetector(this, this);
    }

    private void setUpBoard() {
        GridLayout gridLayout = initializeGridLayout();
        gridCells = new ImageView[FILAS][COLUMNAS];
        createCellGrid(gridLayout);
    }

    private GridLayout initializeGridLayout() {
        GridLayout gridLayout = findViewById(R.id.gridLayoutStacker);
        gridLayout.setColumnCount(COLUMNAS);
        gridLayout.setRowCount(FILAS);
        return gridLayout;
    }

    private void createCellGrid(GridLayout gridLayout) {
        for (int fila = 0; fila < FILAS; fila++) {
            for (int col = 0; col < COLUMNAS; col++) {
                ImageView cell = createCell(fila, col);
                gridCells[fila][col] = cell;
                gridLayout.addView(cell);
            }
        }
    }

    private ImageView createCell(int fila, int col) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(Color.LTGRAY);
        imageView.setLayoutParams(createCellLayoutParams(fila, col));
        return imageView;
    }

    private GridLayout.LayoutParams createCellLayoutParams(int fila, int col) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(fila, 1f);
        params.columnSpec = GridLayout.spec(col, 1f);
        params.width = 0;
        params.height = 0;
        params.setMargins(CELL_MARGIN, CELL_MARGIN, CELL_MARGIN, CELL_MARGIN);
        return params;
    }

    private void startGame() {
        // Reinicializar variables
        currentRow = 11;
        currentColumn = 0;
        BLOCK_SIZE = INITIAL_BLOCK_SIZE;  // Usamos la constante en lugar del valor directo
        direction = 1;
        currentDelay = FLOOR_DELAYS[11];  // Establecemos el delay inicial

        // Limpiar el tablero entero (por si es un reinicio)
        for (int fila = 0; fila < FILAS; fila++) {
            for (int col = 0; col < COLUMNAS; col++) {
                gridCells[fila][col].setBackgroundColor(Color.LTGRAY);
            }
        }

        // Iniciar el juego con los bloques moviéndose
        startMovingBlocks();
    }



    private void startMovingBlocks() {
        // Limpiar la fila actual
        for (int col = 0; col < COLUMNAS; col++) {
            gridCells[currentRow][col].setBackgroundColor(Color.LTGRAY);
        }

        // Pintar el bloque inicial con el tamaño actual
        for (int i = 0; i < BLOCK_SIZE; i++) {
            if (currentColumn + i < COLUMNAS) {
                gridCells[currentRow][currentColumn + i].setBackgroundColor(Color.BLUE);
            }
        }

        // Iniciar la animación
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveBlocks();
                handler.postDelayed(this, DELAY);
            }
        }, DELAY);
    }

    private void fixBlocks() {
        stopCurrentMovement();
        boolean[] supported = checkBlockSupport();
        int supportedCount = countSupportedBlocks(supported);

        if (supportedCount == 0) {
            endGame();
            return;
        }

        updateGameState(supported, supportedCount);
    }

    private void stopCurrentMovement() {
        handler.removeCallbacksAndMessages(null);
    }

    private boolean[] checkBlockSupport() {
        boolean[] supported = new boolean[COLUMNAS];

        for (int col = 0; col < COLUMNAS; col++) {
            if (isActiveBlock(col)) {
                supported[col] = hasSupport(col);
            }
        }

        return supported;
    }

    private boolean isActiveBlock(int col) {
        return ((ColorDrawable)gridCells[currentRow][col].getBackground()).getColor() == Color.BLUE;
    }

    private boolean hasSupport(int col) {
        if (currentRow == FILAS - 1) {
            return true;
        }

        if (((ColorDrawable)gridCells[currentRow + 1][col].getBackground()).getColor() == Color.BLUE) {
            return true;
        } else {
            gridCells[currentRow][col].setBackgroundColor(Color.LTGRAY);
            return false;
        }
    }

    private int countSupportedBlocks(boolean[] supported) {
        int count = 0;
        for (boolean isSupported : supported) {
            if (isSupported) count++;
        }
        return count;
    }

    private void updateGameState(boolean[] supported, int supportedCount) {
        updateBlockSize(supportedCount);
        int newColumn = findFirstSupportedColumn(supported);
        moveToNextRow();

        if (currentRow < 0) {
            handleWin();
            return;
        }

        updateSpeedAndPosition(newColumn);
        startMovingBlocksForNextRow();
    }

    private void updateBlockSize(int supportedCount) {
        BLOCK_SIZE = supportedCount;
        adjustBlockSizeForLevel();
    }

    private void adjustBlockSizeForLevel() {
        if (currentRow == 7) {
            BLOCK_SIZE = Math.min(BLOCK_SIZE, 2);
        }
        else if (currentRow == 4) {
            BLOCK_SIZE = Math.min(BLOCK_SIZE, 1);
        }
    }

    private int findFirstSupportedColumn(boolean[] supported) {
        for (int col = 0; col < COLUMNAS; col++) {
            if (supported[col]) return col;
        }
        return 0;
    }

    private void moveToNextRow() {
        currentRow--;
    }

    private void handleWin() {
        Toast.makeText(this, "¡Ganaste!", Toast.LENGTH_LONG).show();
    }

    private void updateSpeedAndPosition(int newColumn) {
        if (currentRow >= 0) {
            currentDelay = FLOOR_DELAYS[currentRow];
            currentColumn = newColumn;
            Log.d(TAG, String.format("Nueva fila: %d, Bloques: %d, Columna: %d",
                    currentRow, BLOCK_SIZE, currentColumn));
        }
    }

    private void startMovingBlocksForNextRow() {
        clearCurrentRow();
        paintInitialBlocks();
        setInitialDirection();
        startBlockMovement();
    }

    private void clearCurrentRow() {
        for (int col = 0; col < COLUMNAS; col++) {
            gridCells[currentRow][col].setBackgroundColor(Color.LTGRAY);
        }
    }

    private void paintInitialBlocks() {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            int col = currentColumn + i;
            if (isValidColumn(col)) {
                gridCells[currentRow][col].setBackgroundColor(Color.BLUE);
            }
        }
    }

    private boolean isValidColumn(int col) {
        return col >= 0 && col < COLUMNAS;
    }

    private void setInitialDirection() {
        if (currentColumn + BLOCK_SIZE >= COLUMNAS) {
            direction = -1;
        } else if (currentColumn <= 0) {
            direction = 1;
        } else {
            direction = (Math.random() > 0.5) ? 1 : -1;
        }
    }

    private void startBlockMovement() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveBlocks();
                handler.postDelayed(this, currentDelay);
            }
        }, currentDelay);
    }

    // Mejorar moveBlocks para manejar correctamente los límites
    private void moveBlocks() {
        clearCurrentRow();
        updateDirection();
        updatePosition();
        drawBlocks();
    }

    private void updateDirection() {
        if (direction > 0 && (currentColumn + BLOCK_SIZE >= COLUMNAS)) {
            direction = -1;
            Log.d(TAG, "Rebote derecho: " + currentColumn);
        } else if (direction < 0 && currentColumn <= 0) {
            direction = 1;
            Log.d(TAG, "Rebote izquierdo: " + currentColumn);
        }
    }

    private void updatePosition() {
        currentColumn += direction;
        enforcePositionBounds();
    }

    private void enforcePositionBounds() {
        if (currentColumn < 0) currentColumn = 0;
        if (currentColumn + BLOCK_SIZE > COLUMNAS) {
            currentColumn = COLUMNAS - BLOCK_SIZE;
        }
    }

    private void drawBlocks() {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            int col = currentColumn + i;
            if (isValidColumn(col)) {
                gridCells[currentRow][col].setBackgroundColor(Color.BLUE);
            }
        }
    }

    private void endGame() {
        Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show();
    }


    public void startStuff(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stacker_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //parte de abajo de la pantalla mayor valor Y
        //parte derecha de la pantalla mayor valor X
        Log.d(TAG, "onFling: Fling gesture detected with velocityX = " + velocityX + " and velocityY = " + velocityY);

        float movementX = startX - endX;
        float movementY = startY - endY;

        if (movementY > movementX && movementY > -movementX) {
            Log.d(TAG, "onFling: Fling gesture detected up");
        } else if (movementX > movementY && movementX > -movementY) {
            Log.d(TAG, "onFling: Fling gesture detected left");
        } else if (movementY > movementX && movementY < -movementX) {
            Log.d(TAG, "onFling: Fling gesture detected right");
        } else if (movementX > movementY && movementX < -movementY) {
            Log.d(TAG, "onFling: Fling gesture detected down");
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        startX = e.getX();
        startY = e.getY();

        if (!hasGameStarted) {
            hasGameStarted = true;
            startGame();
        } else {
            fixBlocks();
        }

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
}