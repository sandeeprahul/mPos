package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;

/**
 * Created by Cbly on 08-Mar-18.
 */

public class UserDB extends DBManager  {
    public static final String TABLE_NAME = "UserDetails";
    public static final String USER_ID = "USER_ID";
    public static final String STORE_ID = "STORE_ID";
    public static final String LOYALTY_ID = "LOYALTY_ID";
    public static final String LOYALTY_PWD = "LOYALTY_PWD";
    public static final String TILL_NO = "TILL_NO";
    public static final String IS_PB = "IS_PB";
    public static final String STORE_EMAIL = "STORE_EMAIL";
    public static final String STORE_PWD = "STORE_PWD";
    public static final String TO_EMAIL = "TO_EMAIL";
    public UserDB(Context context) {
        super(context);

            sqliteDB = open();
            createUserDetailsTable();


    }
    public void createUserDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + USER_ID + " text,"
                + STORE_ID + " text,"
                + LOYALTY_ID + " text,"
                + LOYALTY_PWD + " text,"
                + TILL_NO + " text,"
                + IS_PB + " text,"
                + STORE_EMAIL + " text,"
                + STORE_PWD + " text,"
                + TO_EMAIL + " text);";

        sqliteDB.execSQL(CreateTable);
    }
    public void deleteUserTable()
    {
        String qry = "drop table if exists "+TABLE_NAME;
        sqliteDB.execSQL(qry);
    }
    public void insertUserDetails(HashMap<String,String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(USER_ID, linedata.get("userID"));
            values.put(STORE_ID, linedata.get("storeID"));
            values.put(LOYALTY_ID, linedata.get("loyaltyID"));
            values.put(LOYALTY_PWD, linedata.get("loyaltyPWD"));
            values.put(TILL_NO, linedata.get("tillNo"));
            values.put(IS_PB, linedata.get("isPBcoupon"));
            values.put(STORE_EMAIL, linedata.get("storeEmail"));
            values.put(STORE_PWD, linedata.get("storePwd"));
            values.put(TO_EMAIL, linedata.get("toEmail"));
            dbSql.insert(TABLE_NAME, null, values);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<UserDetails> getUserDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        ArrayList<UserDetails> userDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            userDetails = new ArrayList<UserDetails>();
            while (cursor.moveToNext()) {

                UserDetails user = new UserDetails();
                user.setUserID(cursor.getString(cursor.getColumnIndexOrThrow(USER_ID)));
                user.setStoreID(cursor.getString(cursor.getColumnIndexOrThrow(STORE_ID)));
                user.setLoyaltyID(cursor.getString(cursor.getColumnIndexOrThrow(LOYALTY_ID)));
                user.setLoyaltyPWD(cursor.getString(cursor.getColumnIndexOrThrow(LOYALTY_PWD)));
                user.setTillNo(cursor.getString(cursor.getColumnIndexOrThrow(TILL_NO)));
                user.setIsPB(cursor.getString(cursor.getColumnIndexOrThrow(IS_PB)));
                user.setStoreEmail(cursor.getString(cursor.getColumnIndexOrThrow(STORE_EMAIL)));
                user.setStorePwd(cursor.getString(cursor.getColumnIndexOrThrow(STORE_PWD)));
                user.setSendEmailID(cursor.getString(cursor.getColumnIndexOrThrow(TO_EMAIL)));
                userDetails.add(user);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return userDetails;

    }



}
