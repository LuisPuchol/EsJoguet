package com.example.esjoguet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    private TextView tvTriesCounter, tvSmallPriceCounter, tvBigPriceCounter;
    private int tries, smallPrizes, bigPrizes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity has been created");
        super.onCreate(savedInstanceState);
        startStuff(savedInstanceState);
        initializeGestureDetector();
        setUpBoard();

        tvTriesCounter = findViewById(R.id.triesCounter);
        tvSmallPriceCounter = findViewById(R.id.smallPriceCounter);
        tvBigPriceCounter = findViewById(R.id.bigPriceCounter);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(Activity_StackerGame.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    private void initializeGestureDetector() {
        gestureDetector = new GestureDetector(this, this);
    }

    /**
     * Configura el tablero de juego.
     */
    private void setUpBoard() {
        GridLayout gridLayout = initializeGridLayout();
        gridCells = new ImageView[FILAS][COLUMNAS];
        createCellGrid(gridLayout);
    }

    /**
     * Inicializa el GridLayout que contendrá el tablero del juego.
     *
     * @return GridLayout inicializado con filas y columnas configuradas
     */
    private GridLayout initializeGridLayout() {
        GridLayout gridLayout = findViewById(R.id.gridLayoutStacker);
        gridLayout.setColumnCount(COLUMNAS);
        gridLayout.setRowCount(FILAS);
        return gridLayout;
    }

    /**
     * Crea la cuadrícula de celdas para el tablero del juego.
     *
     * @param gridLayout El layout donde se añadirán las celdas
     */
    private void createCellGrid(GridLayout gridLayout) {
        for (int fila = 0; fila < FILAS; fila++) {
            for (int col = 0; col < COLUMNAS; col++) {
                ImageView cell = createCell(fila, col);
                gridCells[fila][col] = cell;
                gridLayout.addView(cell);
            }
        }
    }

    /**
     * Crea una celda individual para el tablero.
     *
     * @param fila Posición de fila para la celda
     * @param col  Posición de columna para la celda
     * @return ImageView configurado como celda
     */
    private ImageView createCell(int fila, int col) {
        ImageView imageView = new ImageView(this);
        imageView.setBackgroundColor(Color.LTGRAY);
        imageView.setLayoutParams(createCellLayoutParams(fila, col));
        return imageView;
    }

    /**
     * Crea los parámetros de layout para una celda.
     *
     * @param fila Posición de fila para los parámetros
     * @param col  Posición de columna para los parámetros
     * @return Parámetros de layout configurados
     */
    private GridLayout.LayoutParams createCellLayoutParams(int fila, int col) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.rowSpec = GridLayout.spec(fila, 1f);
        params.columnSpec = GridLayout.spec(col, 1f);
        params.width = 0;
        params.height = 0;
        params.setMargins(CELL_MARGIN, CELL_MARGIN, CELL_MARGIN, CELL_MARGIN);
        return params;
    }

    /**
     * Inicia el juego, inicializando las variables y el tablero.
     */
    private void startGame() {
        currentRow = 11;
        currentColumn = 0;
        BLOCK_SIZE = INITIAL_BLOCK_SIZE;
        direction = 1;
        currentDelay = FLOOR_DELAYS[11];

        for (int fila = 0; fila < FILAS; fila++) {
            for (int col = 0; col < COLUMNAS; col++) {
                gridCells[fila][col].setBackgroundColor(Color.LTGRAY);
            }
        }

        startMovingBlocks();
    }

    /**
     * Inicia el movimiento de los bloques en la fila actual.
     */
    private void startMovingBlocks() {
        for (int col = 0; col < COLUMNAS; col++) {
            gridCells[currentRow][col].setBackgroundColor(Color.LTGRAY);
        }

        for (int i = 0; i < BLOCK_SIZE; i++) {
            if (currentColumn + i < COLUMNAS) {
                gridCells[currentRow][currentColumn + i].setBackgroundColor(Color.BLUE);
            }
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveBlocks();
                handler.postDelayed(this, DELAY);
            }
        }, DELAY);
    }

    /**
     * Fija los bloques en su posición actual y verifica la continuidad del juego.
     */
    private void fixBlocks() {
        stopCurrentMovement();
        boolean[] supported = checkBlockSupport();
        int supportedCount = countSupportedBlocks(supported);

        if (supportedCount == 0) {
            gameOver();
            handleEndGame();
            return;
        }

        updateGameState(supported, supportedCount);
    }

    /**
     * Detiene el movimiento actual de los bloques.
     */
    private void stopCurrentMovement() {
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Verifica qué bloques tienen soporte debajo.
     *
     * @return Array de booleanos indicando qué bloques tienen soporte
     */
    private boolean[] checkBlockSupport() {
        boolean[] supported = new boolean[COLUMNAS];

        for (int col = 0; col < COLUMNAS; col++) {
            if (isActiveBlock(col)) {
                supported[col] = hasSupport(col);
            }
        }

        return supported;
    }

    /**
     * Determina si una posición contiene un bloque activo.
     *
     * @param col Columna a verificar
     * @return true si hay un bloque activo, false en caso contrario
     */
    private boolean isActiveBlock(int col) {
        return ((ColorDrawable) gridCells[currentRow][col].getBackground()).getColor() == Color.BLUE;
    }

    /**
     * Verifica si un bloque tiene soporte debajo.
     *
     * @param col Columna del bloque a verificar
     * @return true si el bloque tiene soporte, false en caso contrario
     */
    private boolean hasSupport(int col) {
        if (currentRow == FILAS - 1) {
            return true;
        }

        if (((ColorDrawable) gridCells[currentRow + 1][col].getBackground()).getColor() == Color.BLUE) {
            return true;
        } else {
            gridCells[currentRow][col].setBackgroundColor(Color.LTGRAY);
            return false;
        }
    }

    /**
     * Cuenta la cantidad de bloques que tienen soporte.
     *
     * @param supported Array con el estado de soporte de cada bloque
     * @return Cantidad de bloques con soporte
     */
    private int countSupportedBlocks(boolean[] supported) {
        int count = 0;
        for (boolean isSupported : supported) {
            if (isSupported) count++;
        }
        return count;
    }

    /**
     * Actualiza el estado del juego después de fijar bloques.
     *
     * @param supported      Array con el estado de soporte de cada bloque
     * @param supportedCount Cantidad de bloques con soporte
     */
    private void updateGameState(boolean[] supported, int supportedCount) {
        updateBlockSize(supportedCount);
        int newColumn = findFirstSupportedColumn(supported);
        moveToNextRow();

        if (currentRow < 0) {
            handleWin();
            handleEndGame();
            return;
        }

        updateSpeedAndPosition(newColumn);
        startMovingBlocksForNextRow();
    }

    /**
     * Actualiza el tamaño del bloque según la cantidad de bloques soportados.
     *
     * @param supportedCount Cantidad de bloques con soporte
     */
    private void updateBlockSize(int supportedCount) {
        BLOCK_SIZE = supportedCount;
        adjustBlockSizeForLevel();
    }

    /**
     * Ajusta el tamaño del bloque según el nivel actual.
     */
    private void adjustBlockSizeForLevel() {
        if (currentRow == 7) {
            BLOCK_SIZE = Math.min(BLOCK_SIZE, 2);
        } else if (currentRow == 4) {
            BLOCK_SIZE = Math.min(BLOCK_SIZE, 1);
        }
    }

    /**
     * Encuentra la primera columna con un bloque soportado.
     *
     * @param supported Array con el estado de soporte de cada bloque
     * @return Índice de la primera columna con soporte
     */
    private int findFirstSupportedColumn(boolean[] supported) {
        for (int col = 0; col < COLUMNAS; col++) {
            if (supported[col]) return col;
        }
        return 0;
    }

    /**
     * Mueve el juego a la siguiente fila (hacia arriba).
     */
    private void moveToNextRow() {
        currentRow--;
    }

    /**
     * Maneja el evento de victoria del juego.
     */
    private void handleWin() {
        Toast.makeText(this, "¡Ganaste!", Toast.LENGTH_LONG).show();
    }

    /**
     * Actualiza la velocidad y posición para la siguiente fila.
     *
     * @param newColumn Nueva posición de columna para el bloque
     */
    private void updateSpeedAndPosition(int newColumn) {
        if (currentRow >= 0) {
            currentDelay = FLOOR_DELAYS[currentRow];
            currentColumn = newColumn;
            Log.d(TAG, String.format("Nueva fila: %d, Bloques: %d, Columna: %d",
                    currentRow, BLOCK_SIZE, currentColumn));
        }
    }

    /**
     * Inicia el movimiento de bloques para la siguiente fila.
     */
    private void startMovingBlocksForNextRow() {
        clearCurrentRow();
        paintInitialBlocks();
        setInitialDirection();
        startBlockMovement();
    }

    /**
     * Limpia la fila actual para preparar el nuevo movimiento.
     */
    private void clearCurrentRow() {
        for (int col = 0; col < COLUMNAS; col++) {
            gridCells[currentRow][col].setBackgroundColor(Color.LTGRAY);
        }
    }

    /**
     * Pinta los bloques iniciales en su posición.
     */
    private void paintInitialBlocks() {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            int col = currentColumn + i;
            if (isValidColumn(col)) {
                gridCells[currentRow][col].setBackgroundColor(Color.BLUE);
            }
        }
    }

    /**
     * Verifica si una columna está dentro de los límites válidos.
     *
     * @param col Índice de columna a verificar
     * @return true si la columna es válida, false en caso contrario
     */
    private boolean isValidColumn(int col) {
        return col >= 0 && col < COLUMNAS;
    }

    /**
     * Establece la dirección inicial del movimiento de los bloques.
     */
    private void setInitialDirection() {
        if (currentColumn + BLOCK_SIZE >= COLUMNAS) {
            direction = -1;
        } else if (currentColumn <= 0) {
            direction = 1;
        } else {
            direction = (Math.random() > 0.5) ? 1 : -1;
        }
    }

    /**
     * Inicia el movimiento de los bloques con el delay actual.
     */
    private void startBlockMovement() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveBlocks();
                handler.postDelayed(this, currentDelay);
            }
        }, currentDelay);
    }

    /**
     * Mueve los bloques horizontalmente según la dirección actual.
     */
    private void moveBlocks() {
        clearCurrentRow();
        updateDirection();
        updatePosition();
        drawBlocks();
    }

    /**
     * Actualiza la dirección del movimiento si se alcanza un límite.
     */
    private void updateDirection() {
        if (direction > 0 && (currentColumn + BLOCK_SIZE >= COLUMNAS)) {
            direction = -1;
            Log.d(TAG, "Rebote derecho: " + currentColumn);
        } else if (direction < 0 && currentColumn <= 0) {
            direction = 1;
            Log.d(TAG, "Rebote izquierdo: " + currentColumn);
        }
    }

    /**
     * Actualiza la posición de los bloques según la dirección.
     */
    private void updatePosition() {
        currentColumn += direction;
        enforcePositionBounds();
    }

    /**
     * Asegura que la posición de los bloques se mantenga dentro de los límites.
     */
    private void enforcePositionBounds() {
        if (currentColumn < 0) currentColumn = 0;
        if (currentColumn + BLOCK_SIZE > COLUMNAS) {
            currentColumn = COLUMNAS - BLOCK_SIZE;
        }
    }

    /**
     * Dibuja los bloques en la posición actual.
     */
    private void drawBlocks() {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            int col = currentColumn + i;
            if (isValidColumn(col)) {
                gridCells[currentRow][col].setBackgroundColor(Color.BLUE);
            }
        }
    }

    /**
     * Maneja el evento de fin de juego por derrota.
     */
    private void gameOver() {
        Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show();
    }

    /**
     * Procesa el final del juego, actualizando estadísticas y base de datos.
     */
    private void handleEndGame() {
        tries++;
        if (currentRow <= 4) smallPrizes++;
        if (currentRow == 0) bigPrizes++;

        tvTriesCounter.setText(String.valueOf(tries));
        tvSmallPriceCounter.setText(String.valueOf(smallPrizes));
        tvBigPriceCounter.setText(String.valueOf(bigPrizes));

        DBAssistant dbAssistant = new DBAssistant(this);
        String username = getUsername();
        int userId = dbAssistant.getUserId(username);

        tries = Integer.parseInt(tvTriesCounter.getText().toString());
        smallPrizes = Integer.parseInt(tvSmallPriceCounter.getText().toString());
        bigPrizes = Integer.parseInt(tvBigPriceCounter.getText().toString());

        dbAssistant.insertGameStackerRecord(userId, tries, smallPrizes, bigPrizes);
    }

    /**
     * Obtiene el nombre de usuario almacenado en preferencias.
     *
     * @return Nombre de usuario o "Guest" si no está definido
     */
    private String getUsername() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("username", "Guest");
    }

    /**
     * Inicializa la actividad con configuraciones de interfaz.
     *
     * @param savedInstanceState Estado guardado de la actividad
     */
    public void startStuff(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stacker_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Maneja el gesto de deslizamiento rápido en la pantalla.
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
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

    /**
     * Maneja el evento de toque en la pantalla.
     * Inicia el juego al primer toque y fija los bloques en los toques subsiguientes.
     */
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