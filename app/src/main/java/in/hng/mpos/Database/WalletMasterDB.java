package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import in.hng.mpos.gettersetter.WalletMaster;

public class WalletMasterDB extends DBManager {
    public static final String TABLE_NAME = "WalletMaster";
    public static final String MK_KEY = "MK_KEY";
    public static final String MK_SMID = "MK_SMID";
    public static final String MK_GEN_OTP = "MK_GEN_OTP";
    public static final String PP_SALT_KEY = "PP_SALT_KEY";
    public static final String PP_SALT_INDEX = "PP_SALT_INDEX";
    public static final String PP_MERCHENT_ID = "PP_MERCHENT_ID";
    public static final String PP_INS_TYPE = "PP_INS_TYPE";
    public static final String PP_EXPIRES_IN = "PP_EXPIRES_IN";
    public static final String PP_STORE_ID = "PP_STORE_ID";
    public static final String PP_TERMINAL_ID = "PP_TERMINAL_ID";
    public static final String ZAG_KEY = "ZAG_KEY";


    public WalletMasterDB(Context context) {
        super(context);

        sqliteDB = open();
        createWalletMasterTable();


    }

    public void createWalletMasterTable() {

        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + MK_KEY + " text,"
                + MK_SMID + " text,"
                + MK_GEN_OTP + " text,"
                + PP_SALT_KEY + " text,"
                + PP_SALT_INDEX + " text,"
                + PP_MERCHENT_ID + " text,"
                + PP_INS_TYPE + " text,"
                + PP_EXPIRES_IN + " text,"
                + PP_STORE_ID + " text,"
                + PP_TERMINAL_ID + " text,"
                + ZAG_KEY + " text);";

        sqliteDB.execSQL(CreateTable);
    }

    public void deleteWalletMasterTable() {
        String qry = "drop table if exists " + TABLE_NAME;
        sqliteDB.execSQL(qry);
    }

    public void insertWalletMasterData(HashMap<String, String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(MK_KEY, linedata.get("walletID"));
            values.put(MK_SMID, linedata.get("walletName"));
            values.put(MK_GEN_OTP, linedata.get("walletAmount"));
            values.put(PP_SALT_KEY, linedata.get("walletOTP"));
            values.put(PP_SALT_INDEX, linedata.get("walletTransID"));
            values.put(PP_MERCHENT_ID, linedata.get("walletName"));
            values.put(PP_INS_TYPE, linedata.get("walletAmount"));
            values.put(PP_EXPIRES_IN, linedata.get("walletOTP"));
            values.put(PP_STORE_ID, linedata.get("walletTransID"));
            values.put(PP_TERMINAL_ID, linedata.get("walletOTP"));
            values.put(ZAG_KEY, linedata.get("walletTransID"));
            dbSql.insert(TABLE_NAME, null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<WalletMaster> getWalletMaster() {

        String qry = "SELECT * FROM " + TABLE_NAME;
        ArrayList<WalletMaster> walletMaster = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            walletMaster = new ArrayList<WalletMaster>();
            while (cursor.moveToNext()) {

                WalletMaster wallet = new WalletMaster();
                wallet.setMkKey(cursor.getString(cursor.getColumnIndexOrThrow(MK_KEY)));
                wallet.setMkSMID(cursor.getString(cursor.getColumnIndexOrThrow(MK_SMID)));
                wallet.setMkGenOTP(cursor.getString(cursor.getColumnIndexOrThrow(MK_GEN_OTP)));
                wallet.setPpSaltKey(cursor.getString(cursor.getColumnIndexOrThrow(PP_SALT_KEY)));
                wallet.setPpSaltIndex(cursor.getString(cursor.getColumnIndexOrThrow(PP_SALT_INDEX)));
                wallet.setPpMerchantID(cursor.getString(cursor.getColumnIndexOrThrow(PP_MERCHENT_ID)));
                wallet.setPpInsType(cursor.getString(cursor.getColumnIndexOrThrow(PP_INS_TYPE)));
                wallet.setPpExpiresIn(cursor.getString(cursor.getColumnIndexOrThrow(PP_EXPIRES_IN)));
                wallet.setPpStoreID(cursor.getString(cursor.getColumnIndexOrThrow(PP_STORE_ID)));
                wallet.setPpTerminalID(cursor.getString(cursor.getColumnIndexOrThrow(PP_TERMINAL_ID)));
                wallet.setZagKey(cursor.getString(cursor.getColumnIndexOrThrow(ZAG_KEY)));
                walletMaster.add(wallet);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return walletMaster;

    }
}
