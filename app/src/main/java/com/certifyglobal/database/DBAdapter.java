package com.certifyglobal.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.certifyglobal.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class DBAdapter {

    private static final String LOG = "DBAdapter - ";
    private SQLiteDatabase mDatabase;

    public DBAdapter(Context context) {
        try {
            DBHelper mDBHelper = new DBHelper(context, 26);
            mDatabase = mDBHelper.getWritableDatabase();
        } catch (Exception e) {
            Logger.error(LOG + "DBAdapter(Context context)", e.getMessage());
        }
    }

    public SQLiteDatabase getWritableDatabaseDB() {
        return mDatabase;
    }

    public static class DBHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "certifyglobal-authenticator";


        public DBHelper(Context context, int version) {
            super(context, DB_NAME, null, version);
        }

        private static final String CREATE_TABLE_I_FACE = "CREATE TABLE " + IFaceHelper.TABLE_NAME + " (" +
                IFaceHelper.COLUMN_EMAIL + " TEXT PRIMARY KEY," +
                IFaceHelper.COLUMN_IMAGE + " BLOB" +
                ");";
        private static final String CREATE_TABLE_COMPANY = "CREATE TABLE " + CompanyHelper.TABLE_NAME + " (" +
                CompanyHelper.COLUMN_HOST_NAME + " TEXT PRIMARY KEY," +
                CompanyHelper.COLUMN_IMAGE + " BLOB," +
                CompanyHelper.COLUMN_NAME + " TEXT," +
                CompanyHelper.COLUMN_USER_ID + " TEXT" +
                ");";


        public static final String CREATE_TABLE_FACE_SETTING = "CREATE TABLE "
                + FaceSettingHelper.SETTING_TABLE_NAME + "(" +
                FaceSettingHelper.COLUMN_HOST_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FaceSettingHelper.COLUMN_SETTING_NAME + " TEXT," +
                FaceSettingHelper.COLUMN_SETTING_USER_ID + " TEXT," +
                FaceSettingHelper.COLUMN_SETTING_ID + " TEXT," +
                FaceSettingHelper.COLUMN_SETTING_VERSION + " TEXT," +
                FaceSettingHelper.COLUMN_MIN_VALUE + " TEXT, " +
                FaceSettingHelper.COLUMN_MAX_VALUE + " TEXT, " +
                FaceSettingHelper.COLUMN_IS_CONFIGURED + " TEXT, " +
                FaceSettingHelper.COLUMN_MATCH_SCORE + " TEXT " +
                ")";

        public static final String CREATE_TABLE_FACE_ERROR = "CREATE TABLE "
                + FaceSettingError.SETTING_TABLE_NAME + "(" +
                FaceSettingError.COLUMN_HOST_NAME + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FaceSettingError.COLUMN_KEY_DATA + " TEXT," +
                FaceSettingError.COLUMN_VALUE_DATA + " TEXT " +
                ")";


        public static void createAllTables(SQLiteDatabase db) {
            dropAllTables(db);
            db.execSQL(CREATE_TABLE_I_FACE);
            db.execSQL(CREATE_TABLE_COMPANY);
            db.execSQL(CREATE_TABLE_FACE_SETTING);
            db.execSQL(CREATE_TABLE_FACE_ERROR);
        }

        public static void dropAllTables(SQLiteDatabase db) {
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                List<String> tables = new ArrayList<>(cursor.getCount());
                while (cursor.moveToNext()) tables.add(cursor.getString(0));
                for (String table : tables) {
                    if (table.startsWith("sqlite_")) continue;
                    db.execSQL("DROP TABLE IF EXISTS " + table);
                }
            } finally {
                cursor.close();
            }
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                if (db != null) DBAdapter.DBHelper.createAllTables(db);
            } catch (Exception e) {
                Logger.error(LOG + "DBHelper -> onCreate(SQLiteDatabase db)", e.getMessage());
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                if (db != null) DBAdapter.DBHelper.createAllTables(db);
            } catch (Exception e) {
                Logger.error(LOG + "DBHelper -> onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion): oldVersion = " + oldVersion
                        + ", newVersion = " + newVersion, e.getMessage());
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                if (db != null) DBAdapter.DBHelper.createAllTables(db);
            } catch (Exception e) {
                Logger.error(LOG + "DBHelper -> onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion): oldVersion = " + oldVersion
                        + ", newVersion = " + newVersion, e.getMessage());
            }
        }
    }
}
