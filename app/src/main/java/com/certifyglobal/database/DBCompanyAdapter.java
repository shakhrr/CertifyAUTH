package com.certifyglobal.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.SparseArray;

import com.certifyglobal.authenticator.ApplicationWrapper;
import com.certifyglobal.pojo.CompanyInfo;
import com.certifyglobal.utils.EndPoints;
import com.certifyglobal.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;


public class DBCompanyAdapter {

    private static final String LOG = "DBCompanyAdapter - ";
    private SQLiteDatabase mDatabase;
    //get a list of columns to be retrieved, we need all of them
    private String[] companyColumn = {
            CompanyHelper.COLUMN_HOST_NAME,
            CompanyHelper.COLUMN_IMAGE,
            CompanyHelper.COLUMN_NAME
    };
    String[] companyLogColumns = {
            CompanyHelper.COLUMN_HOST_NAME,
            CompanyHelper.COLUMN_IMAGE
    };
    String[] companysettingColumns = {
            FaceSettingHelper.COLUMN_SETTING_NAME,
            FaceSettingHelper.COLUMN_MIN_VALUE,
            FaceSettingHelper.COLUMN_MAX_VALUE,
            FaceSettingHelper.COLUMN_IS_CONFIGURED,
            FaceSettingHelper.COLUMN_MATCH_SCORE,
            FaceSettingHelper.COLUMN_SETTING_USER_ID,
            FaceSettingHelper.COLUMN_SETTING_VERSION,
            FaceSettingHelper.COLUMN_SETTING_ID
    };
    String[] columnFaceError = {
            FaceSettingError.COLUMN_KEY_DATA,
            FaceSettingError.COLUMN_VALUE_DATA
    };
    public DBCompanyAdapter() {
        mDatabase = ApplicationWrapper.getWritableDatabase().getWritableDatabaseDB();
    }

    public HashMap<String, byte[]> readCompanyData() {
        HashMap<String,byte[]> listCompany = new HashMap<>();
        try {
            Cursor cursor = mDatabase.query((CompanyHelper.TABLE_NAME), companyColumn, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                if (EndPoints.deployment == EndPoints.Mode.Local)
                    Logger.debug(LOG, " " + cursor.getCount());
                do {
                    CompanyInfo companyInfo = new CompanyInfo();
                    companyInfo.hostName = cursor.getString(cursor.getColumnIndex(CompanyHelper.COLUMN_HOST_NAME));
                    companyInfo.image = cursor.getBlob(cursor.getColumnIndex(CompanyHelper.COLUMN_IMAGE));
                    listCompany.put(companyInfo.hostName, companyInfo.image);
                }
                while (cursor.moveToNext());
            }
            if (cursor != null && !cursor.isClosed()) cursor.close();

        } catch (Exception e) {
            Logger.error(LOG + "readCompanyData()", e.getMessage());
        }
        return listCompany;
    }


    public void insertCompany(String hostName,String userId, String companyName, byte[] image) {
        try {
            String sql = "INSERT OR REPLACE INTO " + (CompanyHelper.TABLE_NAME) + " VALUES (?,?,?,?);";
            //compile the statement and start a transaction
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            mDatabase.beginTransaction();
            statement.clearBindings();
            //for a given column index, simply bind the data to be put inside that index

            statement.bindString(1, hostName);
            statement.bindBlob(2, image);
            statement.bindString(3, companyName);
            statement.bindString(4, userId);
            statement.execute();
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        } catch (Exception e) {
            Logger.error(LOG + "insertCompany(String email, byte[] image): email = ", e.getMessage());
        }
    }
    public byte[] readCompanyLogo(String hostName) {
        byte[] bytes = null;
        try {
            Cursor cursor = mDatabase.query((CompanyHelper.TABLE_NAME), companyLogColumns, CompanyHelper.COLUMN_HOST_NAME+ "='" + hostName + "'", null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    bytes = cursor.getBlob(cursor.getColumnIndex(CompanyHelper.COLUMN_IMAGE));
                }
                while (cursor.moveToNext());
            }
            if (cursor != null && !cursor.isClosed()) cursor.close();

        } catch (Exception e) {
            Logger.error(LOG + "readCallLogTypes()", e.getMessage());
        }
        return bytes;
    }

    public long insertFaceSetting(String userid,String version,String name, String minval, String maxval,String isconfigured,String matchScore,String id) {
        long rowIdInserted = 0;
        try {
        ContentValues cv = new ContentValues();
        cv.put(FaceSettingHelper.COLUMN_SETTING_USER_ID, userid);
        cv.put(FaceSettingHelper.COLUMN_SETTING_ID, id);
        cv.put(FaceSettingHelper.COLUMN_SETTING_VERSION, version);
        cv.put(FaceSettingHelper.COLUMN_SETTING_NAME, name);
        cv.put(FaceSettingHelper.COLUMN_MIN_VALUE, minval);
        cv.put(FaceSettingHelper.COLUMN_MAX_VALUE, maxval);
        cv.put(FaceSettingHelper.COLUMN_IS_CONFIGURED,isconfigured);
        cv.put(FaceSettingHelper.COLUMN_MATCH_SCORE,matchScore);

        rowIdInserted = mDatabase.insert(FaceSettingHelper.SETTING_TABLE_NAME, null, cv);
        }catch (Exception e){
            Logger.error(" insertFaceSetting(String name, float minval, float maxval,Boolean isconfigured)",e.getMessage());
        }

        return rowIdInserted;

    }

    public int updateFaceSetting(String userid,String version,String name, String minval, String maxval,String isconfigured,String matchScore,String id) {
        int rowAffected = 0;

        try {
            ContentValues cv = new ContentValues();
            cv.put(FaceSettingHelper.COLUMN_SETTING_USER_ID, userid);
            cv.put(FaceSettingHelper.COLUMN_SETTING_ID, id);
            cv.put(FaceSettingHelper.COLUMN_SETTING_VERSION, version);
            cv.put(FaceSettingHelper.COLUMN_SETTING_NAME, name);
            cv.put(FaceSettingHelper.COLUMN_MIN_VALUE, minval);
            cv.put(FaceSettingHelper.COLUMN_MAX_VALUE, maxval);
            cv.put(FaceSettingHelper.COLUMN_IS_CONFIGURED, isconfigured);
            cv.put(FaceSettingHelper.COLUMN_MATCH_SCORE, matchScore);


            rowAffected = mDatabase.update(FaceSettingHelper.SETTING_TABLE_NAME, cv,
                    FaceSettingHelper.COLUMN_SETTING_USER_ID + " = '" + userid + "'", null);


        }catch (Exception e){
            Logger.error(" updateFaceSetting(String userid,String version,String name, String minval, String maxval,String isconfigured,String matchScore)",e.getMessage());
        }
        return rowAffected;

    }


    public ArrayList<HashMap<String, String>> getSettingList(String user) {

        ArrayList<HashMap<String, String>> setting_arraylist = new ArrayList<HashMap<String, String>>();
        String name,isconfigure,userid,version;
        String minor,major,matchScore,id;
        try {
            Cursor cursor =  mDatabase.query((FaceSettingHelper.SETTING_TABLE_NAME), companysettingColumns, FaceSettingHelper.COLUMN_SETTING_USER_ID + "  ='" + user +"'", null, null, null, null);


            if (cursor.moveToFirst()) {

                do {
                    HashMap<String, String> setting_hashmap = new HashMap<String, String>();
                    name = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_SETTING_NAME));
                    minor = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_MIN_VALUE));
                    major = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_MAX_VALUE));
                    isconfigure = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_IS_CONFIGURED));
                    matchScore = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_MATCH_SCORE));
                    userid = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_SETTING_USER_ID));
                    version = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_SETTING_VERSION));
                    id = cursor.getString(cursor.getColumnIndex
                            (FaceSettingHelper.COLUMN_SETTING_ID));



                    setting_hashmap.put("name", name);
                    setting_hashmap.put("min", String.valueOf(minor));
                    setting_hashmap.put("max", String.valueOf(major));
                    setting_hashmap.put("isconfigure", isconfigure);
                    setting_hashmap.put("matchscore", matchScore);
                    setting_hashmap.put("userid", userid);
                    setting_hashmap.put("version", version);
                    setting_hashmap.put("id", id);

                    setting_arraylist.add(setting_hashmap);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e){
            // this gets called even if there is an exception somewhere above
            Logger.error("ArrayList<HashMap<String, String>> getSettingList()",e.getMessage());

        }
        return setting_arraylist;
    }


    public long insertFaceError(String name, String value) {
        long rowIdInserted = 0;
        try {
            ContentValues cv = new ContentValues();
            cv.put(FaceSettingError.COLUMN_KEY_DATA, name);
            cv.put(FaceSettingError.COLUMN_VALUE_DATA, value);


            rowIdInserted = mDatabase.insert(FaceSettingError.SETTING_TABLE_NAME, null, cv);
        }catch (Exception e){
            Logger.error(" insertFaceError(String name, String value)",e.getMessage());
        }

        return rowIdInserted;

    }
    public void insertFaceErrorNew(String cId,String name, String value) {
        try {
            String sql = "INSERT OR REPLACE INTO " + (FaceSettingError.SETTING_TABLE_NAME) + " VALUES (?,?,?);";
            //compile the statement and start a transaction
            SQLiteStatement statement = mDatabase.compileStatement(sql);
            mDatabase.beginTransaction();
            statement.clearBindings();

            statement.bindString(1, cId);
            statement.bindString(2, name);
            statement.bindString(3, value);

            statement.execute();
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        } catch (Exception e) {
            Logger.error(LOG + "insertFaceErrorNew(String name, String value)", e.getMessage());
        }
    }
    public ArrayList<HashMap<String, String>> getFaceError() {

        ArrayList<HashMap<String, String>> setting_arraylist = new ArrayList<HashMap<String, String>>();
        String name,value;

        try {
            Cursor cursor = mDatabase.query((FaceSettingError.SETTING_TABLE_NAME), columnFaceError, null, null, null, null, null);

            if (cursor.moveToFirst()) {

                do {
                    HashMap<String, String> setting_hashmap = new HashMap<String, String>();
                    name = cursor.getString(cursor.getColumnIndex
                            (FaceSettingError.COLUMN_KEY_DATA));
                    value = cursor.getString(cursor.getColumnIndex
                            (FaceSettingError.COLUMN_VALUE_DATA));



                    setting_hashmap.put(name, value);
                  //  setting_hashmap.put("value", value);


                    setting_arraylist.add(setting_hashmap);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception e){
            // this gets called even if there is an exception somewhere above
            Logger.error("ArrayList<HashMap<String, String>> getSettingList()",e.getMessage());

        }
        return setting_arraylist;
    }


}
