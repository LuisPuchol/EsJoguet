package com.example.esjoguet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.esjoguet.leaderboards.LeaderBoardEntry2048;
import com.example.esjoguet.leaderboards.LeaderBoardEntryStacker;

import java.util.ArrayList;
import java.util.List;

public class DBAssistant extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "GameCenter.db";
    private static final int DATABASE_VERSION = 1;

    public DBAssistant(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT);";
        db.execSQL(createUsersTable);

        String createTable2048 = "CREATE TABLE game_2048 (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "moves INTEGER, " +
                "time TEXT, " +
                "score INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);";
        db.execSQL(createTable2048);

        String createTableStacker = "CREATE TABLE game_stacker (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "attempts INTEGER, " +
                "small_prizes INTEGER, " +
                "big_prizes INTEGER, " +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);";
        db.execSQL(createTableStacker);
    }


    // Verifica si el usuario existe y devuelve su contraseña
    public String getPasswordForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE username = ?", new String[]{username});

        if (cursor.moveToFirst()) {
            String password = cursor.getString(0);
            cursor.close();
            return password;
        }

        cursor.close();
        return null; // No se encontró el usuario
    }

    // Inserta un nuevo usuario en la base de datos
    public void insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        db.insert("users", null, values);
        db.close();
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM users WHERE username = ?", new String[]{username});

        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(0);
            cursor.close();
            return userId;
        }

        cursor.close();
        return -1; // Devuelve -1 si el usuario no existe
    }

    public List<LeaderBoardEntryStacker> getStackerLeaderboard() {
        List<LeaderBoardEntryStacker> entries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = this.getReadableDatabase();

            // Consulta SQL para obtener los récords ordenados (por ejemplo por intentos ascendente)
            String query = "SELECT u.username, g.attempts, g.small_prizes, g.big_prizes " +
                    "FROM game_stacker g " +
                    "INNER JOIN users u ON g.user_id = u.id " +
                    "ORDER BY g.big_prizes DESC, g.small_prizes DESC, g.attempts ASC";

            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String username = cursor.getString(0);
                    int tries = cursor.getInt(1);
                    int smallPrizes = cursor.getInt(2);
                    int bigPrizes = cursor.getInt(3);

                    LeaderBoardEntryStacker entry = new LeaderBoardEntryStacker(
                            username, tries, smallPrizes, bigPrizes);
                    entries.add(entry);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBAssistant", "Error getting Stacker leaderboard: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return entries;
    }


    public List<LeaderBoardEntry2048> get2048Leaderboard() {
        List<LeaderBoardEntry2048> leaderboard = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT u.username, " +
                "COALESCE(g.moves, 0), COALESCE(g.time, '00:00'), COALESCE(g.score, 0) " +
                "FROM users u " +
                "LEFT JOIN game_2048 g ON u.id = g.user_id " +
                "ORDER BY g.score DESC, g.moves ASC, g.time ASC";

        try {
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    String username = cursor.getString(0);
                    int moves = cursor.getInt(1);
                    String time = cursor.getString(2);
                    int score = cursor.getInt(3);

                    leaderboard.add(new LeaderBoardEntry2048(username, moves, time, score));
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            Log.e("DB_ERROR", "Error en la consulta SQL", e);
        } finally {
            db.close();
        }

        return leaderboard;
    }




    public void insertGame2048Record(int userId, int moves, String time, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("moves", moves);
        values.put("time", time);
        values.put("score", score);

        db.insert("game_2048", null, values);
        db.close();
    }

    public void insertGameStackerRecord(int userId, int tries, int smallPrizes, int bigPrizes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("attempts", tries);
        values.put("small_prizes", smallPrizes);
        values.put("big_prizes", bigPrizes);

        db.insert("game_stacker", null, values);
        db.close();
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si se necesita actualizar la estructura de la base de datos
    }


}

