package in.hng.mpos.Database;

import android.content.Context;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.LoyaltyCredentials;
import in.hng.mpos.gettersetter.ProductList;
import in.hng.mpos.gettersetter.UserDetails;

/**
 * Created by Cbly on 14-Mar-18.
 */

public class LoyaltyCredentialsDB extends DBManager {


    public static final String TABLE_NAME = "LoyaltyCredentials";
    public static final String ACCOUNT_ID = "ACCOUNT_ID";
    public static final String STORE_ID = "STORE_ID";
    public static final String STORE_OUTLET_ID = "STORE_OUTLET_ID";
    public static final String CUSTOMER_ID = "CUSTOMER_ID";

    public LoyaltyCredentialsDB(Context context) {
        super(context);

        sqliteDB = open();
        createLoyaltyCredentialsDetailsTable();


    }
    public void createLoyaltyCredentialsDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + ACCOUNT_ID + " text,"
                + STORE_ID + " text,"
                + STORE_OUTLET_ID + " text,"
                + CUSTOMER_ID + " text);";

        sqliteDB.execSQL(CreateTable);
    }

    public void deleteLoyaltyCredentialsDetailsTable()
    {
        String qry = "drop table if exists "+TABLE_NAME;
        sqliteDB.execSQL(qry);
    }
    public void insertLoyaltyCredentialsDetails(HashMap<String,String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(ACCOUNT_ID, linedata.get("accountID"));
            values.put(STORE_ID, linedata.get("storeID"));
            values.put(STORE_OUTLET_ID, linedata.get("storeOutletID"));
            values.put(CUSTOMER_ID, linedata.get("customerID"));

            dbSql.insert(TABLE_NAME, null, values);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<LoyaltyCredentials> getLoyaltyCredentialsDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        ArrayList<LoyaltyCredentials> LoyaltyCredentialDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            LoyaltyCredentialDetails = new ArrayList<LoyaltyCredentials>();
            while (cursor.moveToNext()) {

                LoyaltyCredentials user = new LoyaltyCredentials();
                user.setAccountID(cursor.getString(cursor.getColumnIndexOrThrow(ACCOUNT_ID)));
                user.setStoreID(cursor.getString(cursor.getColumnIndexOrThrow(STORE_ID)));
                user.setStoreOutletID(cursor.getString(cursor.getColumnIndexOrThrow(STORE_OUTLET_ID)));
                user.setCustomerID(cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_ID)));

                LoyaltyCredentialDetails.add(user);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return LoyaltyCredentialDetails;

    }
}
