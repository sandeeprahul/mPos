package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.StoreDetails;
import in.hng.mpos.gettersetter.UserDetails;

public class StoreDB extends DBManager {
    public static final String TABLE_NAME = "StoreDetails";
    public static final String USER_ID = "USER_ID";
    public static final String STORE_ID = "STORE_ID";
    public static final String LOYALTY_ID = "LOYALTY_ID";
    public static final String LOYALTY_PWD = "LOYALTY_PWD";
    public static final String TILL_NO = "TILL_NO";


    public StoreDB(Context context) {
        super(context);

        sqliteDB = open();
        createStoreDetailsTable();


    }

    public void createStoreDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + USER_ID + " text,"
                + STORE_ID + " text,"
                + LOYALTY_ID + " text,"
                + LOYALTY_PWD + " text,"
                + TILL_NO + " text);";


        sqliteDB.execSQL(CreateTable);
    }

    public void deleteStoreTable() {
        String qry = "drop table if exists " + TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void insertStoreDetails(HashMap<String, String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(USER_ID, linedata.get("userID"));
            values.put(STORE_ID, linedata.get("storeID"));
            values.put(LOYALTY_ID, linedata.get("loyaltyID"));
            values.put(LOYALTY_PWD, linedata.get("loyaltyPWD"));
            values.put(TILL_NO, linedata.get("tillNo"));
            dbSql.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<StoreDetails> getStoreDetails() {

        String qry = "SELECT * FROM " + TABLE_NAME;
        ArrayList<StoreDetails> storeDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            storeDetails = new ArrayList<StoreDetails>();
            while (cursor.moveToNext()) {

                StoreDetails store = new StoreDetails();
                store.setUserID(cursor.getString(cursor.getColumnIndexOrThrow(USER_ID)));
                store.setStoreID(cursor.getString(cursor.getColumnIndexOrThrow(STORE_ID)));
                store.setLoyaltyID(cursor.getString(cursor.getColumnIndexOrThrow(LOYALTY_ID)));
                store.setLoyaltyPWD(cursor.getString(cursor.getColumnIndexOrThrow(LOYALTY_PWD)));
                store.setTillNo(cursor.getString(cursor.getColumnIndexOrThrow(TILL_NO)));
                storeDetails.add(store);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return storeDetails;

    }

    public boolean isOrderTaking() {


        String qry = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_NAME + "'";
        String result = "";

        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            while (cursor.moveToNext()) {

                result = cursor.getString(cursor.getColumnIndexOrThrow("name"));

            }


        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        if (result.equalsIgnoreCase(""))
            return false;
        else
            return true;


    }


}
