package com.example.restudy;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Local SQLite database: all check-ins are saved in a file on the phone (restudy.db).
 *
 * HOW IT WORKS
 * - SQLiteOpenHelper manages the database file. The first time getWritableDatabase() is called,
 *   Android creates the file and runs onCreate() (our CREATE TABLE). After that the file
 *   already exists and onCreate() is skipped.
 * - DB_VERSION: if you change the table later (add a column...), raise this number. Android then
 *   calls onUpgrade() once on phones that have the old version.
 * - One instance for the whole app (getInstance), so every screen shares the same connection.
 *
 * TABLE: sessions (one row = one run of: check-in before -> lockdown -> check-in after)
 *   id             auto number, used to pass the session between screens
 *   created_at     local date/time the row was made, e.g. "2026-11-06 14:30:00"
 *   before_stress / before_mood / before_energy   1-5, NULL if not answered
 *   after_stress  / after_mood  / after_energy    1-5, NULL if not answered
 *
 * NULL (not 0) for skipped answers matters: SQL's AVG() ignores NULLs, so a skipped answer
 * doesn't drag the averages down later.
 *
 * The data stays on this phone only and is deleted if the app is uninstalled.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "restudy.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_SESSIONS = "sessions";
    public static final String COL_ID = "id";
    public static final String COL_CREATED_AT = "created_at";

    /** Use this when a screen has no session yet. */
    public static final long NO_SESSION = -1;

    private static DatabaseHelper instance;

    /** Shared helper. getApplicationContext() so we don't keep an old Activity alive. */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SESSIONS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                // SQLite fills this in automatically with the phone's local time
                + COL_CREATED_AT + " TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), "
                + "before_stress INTEGER, before_mood INTEGER, before_energy INTEGER, "
                + "after_stress INTEGER, after_mood INTEGER, after_energy INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to migrate yet (only version 1 exists). When you bump DB_VERSION, put the
        // changes here, e.g. db.execSQL("ALTER TABLE sessions ADD COLUMN note TEXT");
    }

    /**
     * Saves one check-in.
     *
     * @param sessionId NO_SESSION to start a new row, or the id returned by an earlier call
     *                  to fill in the same row (the "after" answers go next to the "before" ones)
     * @param isAfter   false = before_* columns, true = after_* columns
     * @param stress    1-5, or 0 if the user didn't pick one (saved as NULL)
     * @return the session id to pass to the next screen
     */
    public long saveCheckIn(long sessionId, boolean isAfter, int stress, int mood, int energy) {
        String prefix = isAfter ? "after_" : "before_";
        ContentValues values = new ContentValues(); // column name -> value, like a Map
        putAnswer(values, prefix + "stress", stress);
        putAnswer(values, prefix + "mood", mood);
        putAnswer(values, prefix + "energy", energy);

        SQLiteDatabase db = getWritableDatabase();
        if (sessionId == NO_SESSION) {
            // New row. insert() returns the new id (created_at is filled by the DEFAULT).
            // nullColumnHack: SQLite can't insert an empty row, so if every answer was skipped
            // it inserts NULL into before_stress to still create the session.
            return db.insert(TABLE_SESSIONS, "before_stress", values);
        }
        // Existing row: only the columns in `values` change, the rest stay as they are.
        // "id = ?" with the id passed separately avoids building SQL from strings.
        db.update(TABLE_SESSIONS, values, COL_ID + " = ?",
                new String[]{String.valueOf(sessionId)});
        return sessionId;
    }

    private static void putAnswer(ContentValues values, String column, int answer) {
        if (answer > 0) values.put(column, answer);
        else values.putNull(column);
    }
}
