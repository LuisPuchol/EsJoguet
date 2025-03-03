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
    private static final int DATABASE_VERSION = 3;

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

    /**
     * Password checker and input user
     */
    public String getPasswordForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM users WHERE username = ?", new String[]{username});

        if (cursor.moveToFirst()) {
            String password = cursor.getString(0);
            cursor.close();
            return password;
        }

        cursor.close();
        return null;
    }

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
            int userName = cursor.getInt(0);
            cursor.close();
            return userName;
        }

        cursor.close();
        return -1;
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

        // Primero verificamos si ya existe un registro para este usuario
        Cursor cursor = db.rawQuery(
                "SELECT id FROM game_stacker WHERE user_id = ?",
                new String[]{String.valueOf(userId)});

        boolean recordExists = cursor.moveToFirst();
        int recordId = -1;

        if (recordExists) {
            recordId = cursor.getInt(0);
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("attempts", tries);
        values.put("small_prizes", smallPrizes);
        values.put("big_prizes", bigPrizes);

        if (recordExists) {
            // Actualizar el registro existente
            db.update("game_stacker", values, "id = ?", new String[]{String.valueOf(recordId)});
            Log.d("DBAssistant", "Registro de Stacker actualizado para userId: " + userId);
        } else {
            // Insertar un nuevo registro
            db.insert("game_stacker", null, values);
            Log.d("DBAssistant", "Nuevo registro de Stacker creado para userId: " + userId);
        }

        db.close();
    }


    public int getUserTries(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        int userId = getUserId(username);

        Cursor cursor = db.rawQuery(
                "SELECT attempts FROM game_stacker WHERE user_id = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId)});


        if (cursor.moveToFirst()) {
            int userAttepmts = cursor.getInt(0);
            cursor.close();
            return userAttepmts;
        }

        cursor.close();
        return 0;
    }

    public int getUserSmallPrizes(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        int userId = getUserId(username);

        Cursor cursor = db.rawQuery(
                "SELECT small_prizes FROM game_stacker WHERE user_id = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            int userSmallPrize = cursor.getInt(0);
            cursor.close();
            return userSmallPrize;
        }

        cursor.close();
        return 0;
    }

    public int getUserBigPrizes(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        int userId = getUserId(username);

        Cursor cursor = db.rawQuery(
                "SELECT big_prizes FROM game_stacker WHERE user_id = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            int userBigPrizes = cursor.getInt(0);
            cursor.close();
            return userBigPrizes;
        }

        cursor.close();
        return 0;
    }

    public int getUserBestScore(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        int userId = getUserId(username);

        Cursor cursor = db.rawQuery(
                "SELECT score FROM game_2048 WHERE user_id = ?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            int userBestScore = cursor.getInt(0);
            cursor.close();
            return userBestScore;
        }

        cursor.close();
        return 0;
    }


    /**
     * Elimina todos los registros de la tabla game_stacker.
     *
     * @return El número de filas eliminadas o -1 si ocurre un error.
     */
    public int clearStackerTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = -1;

        try {
            // DELETE FROM game_stacker elimina todos los registros de la tabla
            rowsDeleted = db.delete("game_stacker", null, null);
            Log.d("DBAssistant", "Tabla game_stacker limpiada: " + rowsDeleted + " registros eliminados");
        } catch (Exception e) {
            Log.e("DBAssistant", "Error al limpiar la tabla game_stacker: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return rowsDeleted;
    }

    /**
     * Elimina todos los registros de la tabla game_2048.
     *
     * @return El número de filas eliminadas o -1 si ocurre un error.
     */
    public int clear2048Table() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = -1;

        try {
            // DELETE FROM game_2048 elimina todos los registros de la tabla
            rowsDeleted = db.delete("game_2048", null, null);
            Log.d("DBAssistant", "Tabla game_2048 limpiada: " + rowsDeleted + " registros eliminados");
        } catch (Exception e) {
            Log.e("DBAssistant", "Error al limpiar la tabla game_2048: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return rowsDeleted;
    }

    /**
     * Método para poblar la base de datos con datos de prueba
     * Crea 15 usuarios con contraseña "1234" y un registro en cada juego para cada usuario
     */
    public void populateDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Habilitar integridad referencial
            db.execSQL("PRAGMA foreign_keys = ON;");

            // Iniciar transacción
            db.beginTransaction();

            // Array con nombres de usuario
            String[] usernames = {
                    "carlos", "maria", "juan", "sofia", "alejandro",
                    "laura", "miguel", "ana", "david", "elena",
                    "pedro", "isabel", "javier", "lucia", "roberto"
            };

            // Contraseña común
            String password = "1234";

            // Eliminar registros previos de los juegos para evitar duplicados
            db.delete("game_stacker", null, null);
            db.delete("game_2048", null, null);

            // Insertar o actualizar usuarios y sus registros
            for (String username : usernames) {
                long userId;

                // Comprobar si el usuario ya existe
                Cursor cursor = db.rawQuery(
                        "SELECT id FROM users WHERE username = ?",
                        new String[]{username});

                if (cursor.moveToFirst()) {
                    // El usuario ya existe, obtener su ID
                    userId = cursor.getLong(0);
                    cursor.close();
                } else {
                    cursor.close();
                    // Crear nuevo usuario
                    ContentValues userValues = new ContentValues();
                    userValues.put("username", username);
                    userValues.put("password", password);
                    userId = db.insert("users", null, userValues);
                }

                if (userId != -1) {
                    try {
                        // Insertar registro para juego 2048
                        ContentValues values2048 = new ContentValues();
                        values2048.put("user_id", userId);
                        values2048.put("moves", 10 + (int) (Math.random() * 90));

                        int minutes = (int) (Math.random() * 10);
                        int seconds = (int) (Math.random() * 60);
                        String time = String.format("%02d:%02d", minutes, seconds);
                        values2048.put("time", time);

                        values2048.put("score", 500 + (int) (Math.random() * 2000));
                        long game2048Id = db.insert("game_2048", null, values2048);

                        if (game2048Id == -1) {
                            Log.e("DBAssistant", "Error al insertar registro en 2048 para usuario: " + username);
                        } else {
                            Log.d("DBAssistant", "Registro 2048 insertado para usuario: " + username);
                        }
                    } catch (Exception e) {
                        Log.e("DBAssistant", "Error en inserción de juego 2048: " + e.getMessage());
                    }

                    try {
                        // Insertar registro para juego Stacker
                        ContentValues valuesStacker = new ContentValues();
                        valuesStacker.put("user_id", userId);
                        valuesStacker.put("attempts", 5 + (int) (Math.random() * 20));
                        valuesStacker.put("small_prizes", (int) (Math.random() * 10));
                        valuesStacker.put("big_prizes", (int) (Math.random() * 3));

                        long stackerId = db.insert("game_stacker", null, valuesStacker);

                        if (stackerId == -1) {
                            Log.e("DBAssistant", "Error al insertar registro en Stacker para usuario: " + username);
                        } else {
                            Log.d("DBAssistant", "Registro Stacker insertado para usuario: " + username);
                        }
                    } catch (Exception e) {
                        Log.e("DBAssistant", "Error en inserción de juego Stacker: " + e.getMessage());
                    }
                } else {
                    Log.e("DBAssistant", "Error con el usuario: " + username);
                }
            }

            // Marcar la transacción como exitosa
            db.setTransactionSuccessful();
            Log.d("DBAssistant", "Base de datos poblada correctamente");
        } catch (Exception e) {
            Log.e("DBAssistant", "Error general en populateDatabase: " + e.getMessage());
        } finally {
            // Finalizar transacción
            if (db.inTransaction()) {
                db.endTransaction();
            }
            // Cerrar conexión
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    /**
     * Método auxiliar para verificar que las tablas existen y están bien configuradas
     * Puedes llamar a este método antes de poblar la base de datos para diagnosticar problemas
     */
    public void checkTables() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            // Verificar tabla users
            Cursor cursorUsers = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'", null);
            boolean usersExists = cursorUsers.getCount() > 0;
            cursorUsers.close();

            // Verificar tabla game_2048
            Cursor cursor2048 = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='game_2048'", null);
            boolean game2048Exists = cursor2048.getCount() > 0;
            cursor2048.close();

            // Verificar tabla game_stacker
            Cursor cursorStacker = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='game_stacker'", null);
            boolean gameStackerExists = cursorStacker.getCount() > 0;
            cursorStacker.close();

            Log.d("DBAssistant", "Comprobación de tablas: users=" + usersExists +
                    ", game_2048=" + game2048Exists + ", game_stacker=" + gameStackerExists);

            // Si las tablas existen, verificar sus columnas
            if (gameStackerExists) {
                Cursor cursorInfo = db.rawQuery("PRAGMA table_info(game_stacker)", null);
                if (cursorInfo.moveToFirst()) {
                    do {
                        int nameIndex = cursorInfo.getColumnIndex("name");
                        if (nameIndex != -1) {
                            String columnName = cursorInfo.getString(nameIndex);
                            Log.d("DBAssistant", "Columna en game_stacker: " + columnName);
                        }
                    } while (cursorInfo.moveToNext());
                }
                cursorInfo.close();
            }
        } catch (Exception e) {
            Log.e("DBAssistant", "Error al verificar tablas: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    /**
     * Método para reiniciar la base de datos por completo
     * Útil cuando hay problemas con la estructura de las tablas
     */
    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Deshabilitar temporalmente integridad referencial para poder borrar las tablas
            db.execSQL("PRAGMA foreign_keys = OFF;");

            // Eliminar todas las tablas si existen
            db.execSQL("DROP TABLE IF EXISTS game_stacker;");
            db.execSQL("DROP TABLE IF EXISTS game_2048;");
            db.execSQL("DROP TABLE IF EXISTS users;");

            Log.d("DBAssistant", "Tablas eliminadas correctamente");

            // Volver a crear las tablas
            onCreate(db);
            Log.d("DBAssistant", "Tablas recreadas correctamente");
        } catch (Exception e) {
            Log.e("DBAssistant", "Error al reiniciar la base de datos: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Habilitar integridad referencial
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Actualización desde versión 1
        if (oldVersion < 2) {
            // Verificar si la tabla game_stacker ya existe
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='game_stacker'",
                    null);
            boolean tableExists = cursor != null && cursor.moveToFirst();
            cursor.close();

            if (!tableExists) {
                String createTableStacker = "CREATE TABLE game_stacker (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "attempts INTEGER, " +
                        "small_prizes INTEGER, " +
                        "big_prizes INTEGER, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);";
                db.execSQL(createTableStacker);
                Log.d("DBAssistant", "Tabla game_stacker creada durante actualización a v2");
            }
        }

        // Actualización a versión 3
        if (oldVersion < 3) {
            // Verificar si la tabla game_2048 ya existe
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='game_2048'",
                    null);
            boolean tableExists = cursor != null && cursor.moveToFirst();
            cursor.close();

            if (!tableExists) {
                String createTable2048 = "CREATE TABLE game_2048 (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "moves INTEGER, " +
                        "time TEXT, " +
                        "score INTEGER, " +
                        "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);";
                db.execSQL(createTable2048);
                Log.d("DBAssistant", "Tabla game_2048 creada durante actualización a v3");
            }
        }
    }


}

