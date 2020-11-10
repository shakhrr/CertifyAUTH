package com.certifyglobal.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.certifyglobal.authenticator.ApplicationWrapper;
import com.certifyglobal.utils.Logger;


import java.util.ArrayList;
import java.util.HashMap;


public class DBIFaceAdapter {

    private static final String LOG = "DBIFaceAdapter - ";
    private SQLiteDatabase mDatabase;

    //get a list of columns to be retrieved, we need all of them
    String[] iFaceColumns = {
            IFaceHelper.COLUMN_EMAIL,
            IFaceHelper.COLUMN_IMAGE
    };

    public DBIFaceAdapter() {
        mDatabase = ApplicationWrapper.getWritableDatabase().getWritableDatabaseDB();
    }


    public HashMap<String,byte[]> readIFaceData(String userId) {
        byte[] bytes = null;
        String userID;
        HashMap<String, byte[]> imgArray = new HashMap<String, byte[]>();


        try {
            Cursor cursor;
            if (userId.equals("")) {
                cursor = mDatabase.query((IFaceHelper.TABLE_NAME), iFaceColumns, null, null, null, null, null);
            } else
                cursor = mDatabase.query((IFaceHelper.TABLE_NAME), iFaceColumns, IFaceHelper.COLUMN_EMAIL + "='" + userId + "'", null, null, null, null);
            Logger.debug("cursor count", cursor.getCount() + "");
            if (cursor != null && cursor.moveToFirst()) {
                do {

                    bytes = cursor.getBlob(cursor.getColumnIndex(IFaceHelper.COLUMN_IMAGE));
                    userID = cursor.getString(cursor.getColumnIndex(IFaceHelper.COLUMN_EMAIL));
                    imgArray.put(userID,bytes);


                }
                while (cursor.moveToNext());
            }
            if (cursor != null && !cursor.isClosed()) cursor.close();

        } catch (Exception e) {
            Logger.error(LOG + "readCallLogTypes()", e.getMessage());
        }
        return imgArray;
    }


    public void insertIFace(String email, byte[] image) {
        try {
            String sql = "INSERT OR REPLACE INTO " + (IFaceHelper.TABLE_NAME) + " VALUES (?,?);";
            //compile the statement and start a transaction
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            mDatabase.beginTransaction();
            statement.clearBindings();
            //for a given column index, simply bind the data to be put inside that index

            statement.bindString(1, email);
            statement.bindBlob(2, image);
            statement.execute();
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        } catch (Exception e) {
            Logger.error(LOG + "insertCallLogType(String email, byte[] image): email = " + email, e.getMessage());
        }
    }

    public void deleteFace() {
        try {
            mDatabase.delete(IFaceHelper.TABLE_NAME, null, null);
            // Logger.debug("eeeeeeeeeeeeeee", "deleteFace  ");
        } catch (Exception e) {
            Logger.error(LOG + "deleteFace", e.getMessage());
        }
    }
}
