package com.example.esjoguet;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.esjoguet.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{


    private static final String TAG = "MainActivity";
    private GestureDetector gestureDetector;
    private float startX, startY, endX, endY;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity has been created");

        super.onCreate(savedInstanceState);

        startStuff(savedInstanceState);

        gestureDetector = new GestureDetector(this,this);

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_login);

        // Ejemplo: Botón de iniciar sesión
        findViewById(R.id.btnLogin).setOnClickListener((View v) -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finaliza LoginActivity
        });

    }

    public void startStuff(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
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