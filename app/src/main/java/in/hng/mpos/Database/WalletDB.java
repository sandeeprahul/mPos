package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.WalletDetails;

public class WalletDB extends DBManager {
    public static final String TABLE_NAME = "WalletDetails";
    public static final String WALLET_ID = "WALLET_ID";
    public static final String WALLET_NAME = "WALLET_NAME";
    public static final String WALLET_AMOUNT = "WALLET_AMOUNT";
    public static final String WALLET_OTP = "WALLET_OTP";
    public static final String WALLET_TRANS_ID = "WALLET_TRANS_ID";

    public WalletDB(Context context) {
        super(context);

        sqliteDB = open();
        createWalletDetailsTable();


    }

    public void createWalletDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + WALLET_ID + " text,"
                + WALLET_NAME + " text,"
                + WALLET_AMOUNT + " text,"
                + WALLET_OTP + " text,"
                + WALLET_TRANS_ID + " text);";

        sqliteDB.execSQL(CreateTable);
    }

    public void deleteWalletTable() {
        String qry = "drop table if exists " + TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void insertWalletDetails(HashMap<String, String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(WALLET_ID, linedata.get("walletID"));
            values.put(WALLET_NAME, linedata.get("walletName"));
            values.put(WALLET_AMOUNT, linedata.get("walletAmount"));
            values.put(WALLET_OTP, linedata.get("walletOTP"));
            values.put(WALLET_TRANS_ID, linedata.get("walletTransID"));
            dbSql.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<WalletDetails> getWalletDetails() {

        String qry = "SELECT * FROM " + TABLE_NAME;
        ArrayList<WalletDetails> walletDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            walletDetails = new ArrayList<WalletDetails>();
            while (cursor.moveToNext()) {

                WalletDetails wallet = new WalletDetails();
                wallet.setWalletID(cursor.getString(cursor.getColumnIndexOrThrow(WALLET_ID)));
                wallet.setWalletName(cursor.getString(cursor.getColumnIndexOrThrow(WALLET_NAME)));
                wallet.setWalletAmount(cursor.getString(cursor.getColumnIndexOrThrow(WALLET_AMOUNT)));
                wallet.setWalletOTP(cursor.getString(cursor.getColumnIndexOrThrow(WALLET_OTP)));
                wallet.setWalletTransID(cursor.getString(cursor.getColumnIndexOrThrow(WALLET_TRANS_ID)));
                walletDetails.add(wallet);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return walletDetails;

    }

    public boolean isWalletused(String walletID) {
        boolean isUsed = true;
        String id = "";
        try {

            String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + WALLET_ID + " = '" + walletID + "'";
            Cursor c = sqliteDB.rawQuery(query, null);
            while (c.moveToNext()) {
                id = c.getString(0);
            }
            c.close();
            if (id.equalsIgnoreCase(walletID))
                isUsed = true;
            else
                isUsed = false;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return isUsed;
    }

    public String getTotalAmt() {
        String Totalamt = "0.00";
        try {

            String query = "SELECT SUM(WALLET_AMOUNT) FROM " + TABLE_NAME;
            Cursor c = sqliteDB.rawQuery(query, null);
            while (c.moveToNext()) {
                Totalamt = c.getString(0);
            }
            c.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return Totalamt;
    }
}
