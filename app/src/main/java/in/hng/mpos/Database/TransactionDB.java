package in.hng.mpos.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import in.hng.mpos.gettersetter.TransactionDetails;
import in.hng.mpos.gettersetter.UserDetails;

/**
 * Created by Cbly on 22-Mar-18.
 */

public class TransactionDB  extends DBManager {

    public static final String OLD_TABLE_NAME = "TransactionDetails";
    public static final String TABLE_NAME = "CardTransactionDetails";
    public static final String BILL_NO = "BILL_NO";
    public static final String RESPONSE_CODE = "RESPONSE_CODE";
    public static final String RESPONSE_MESSAGE = "RESPONSE_MESSAGE";
    public static final String TRANS_DATE = "TRANS_DATE";
    public static final String TRANS_TIME = "TRANS_TIME";
    public static final String CARD_NO = "CARD_NO";
    public static final String TID = "TID";
    public static final String INV_NO = "INV_NO";
    public static final String RRN = "RRN";
    public static final String APPROVAL_CODE = "APPROVAL_CODE";


    public TransactionDB(Context context) {
        super(context);
        sqliteDB = open();
        deleteTransactionTable();
        createTransactionDetailsTable();


    }

    public void createTransactionDetailsTable() {


        String CreateTable = "create table if not exists " + TABLE_NAME + "("
                + BILL_NO + " text,"
                + RESPONSE_CODE + " text,"
                + RESPONSE_MESSAGE + " text,"
                + TRANS_DATE + " text,"
                + TRANS_TIME + " text,"
                + CARD_NO + " text,"
                + TID + " text,"
                + INV_NO + " text,"
                + RRN + " text,"
                + APPROVAL_CODE + " text);";

        sqliteDB.execSQL(CreateTable);
    }
    public void deleteTransactionTable()
    {
        String qry = "drop table if exists "+OLD_TABLE_NAME;
        sqliteDB.execSQL(qry);
    }


    public void insertTransactionDetails(HashMap<String,String> linedata) {
        try {

            SQLiteDatabase dbSql = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(BILL_NO, linedata.get("billNo"));
            values.put(RESPONSE_CODE, linedata.get("responseCode"));
            values.put(RESPONSE_MESSAGE, linedata.get("responseMessage"));
            values.put(TRANS_DATE, linedata.get("tranDate"));
            values.put(TRANS_TIME, linedata.get("transTime"));
            values.put(CARD_NO, linedata.get("cardNO"));
            values.put(TID, linedata.get("tID"));
            values.put(INV_NO, linedata.get("invNO"));
            values.put(RRN, linedata.get("rrn"));
            values.put(APPROVAL_CODE, linedata.get("approvalCode"));


            dbSql.insert(TABLE_NAME, null, values);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            //sqliteDB.endTransaction();
        }
    }

    public ArrayList<TransactionDetails> getTransactionDetails() {

        String qry = "SELECT * FROM "+TABLE_NAME;
        ArrayList<TransactionDetails> transactionDetails = null;
        try {

            Cursor cursor = sqliteDB.rawQuery(qry, null);
            transactionDetails = new ArrayList<TransactionDetails>();
            while (cursor.moveToNext()) {

                TransactionDetails transaction = new TransactionDetails();
                transaction.setBillNo(cursor.getString(cursor.getColumnIndexOrThrow(BILL_NO)));
                transaction.setResponseCode(cursor.getString(cursor.getColumnIndexOrThrow(RESPONSE_CODE)));
                transaction.setResponseMessage(cursor.getString(cursor.getColumnIndexOrThrow(RESPONSE_MESSAGE)));
                transaction.setTranDate(cursor.getString(cursor.getColumnIndexOrThrow(TRANS_DATE)));
                transaction.setTransTime(cursor.getString(cursor.getColumnIndexOrThrow(TRANS_TIME)));
                transaction.setCardNO(cursor.getString(cursor.getColumnIndexOrThrow(CARD_NO)));
                transaction.settID(cursor.getString(cursor.getColumnIndexOrThrow(TID)));
                transaction.setInvNO(cursor.getString(cursor.getColumnIndexOrThrow(INV_NO)));
                transaction.setRrn(cursor.getString(cursor.getColumnIndexOrThrow(RRN)));
                transaction.setApprovalCode(cursor.getString(cursor.getColumnIndexOrThrow(APPROVAL_CODE)));

                transactionDetails.add(transaction);


            }
        } catch (Exception e) {
            Log.e("Db", "Exception while retrieving -> " + e.toString());
        } finally {
        }
        return transactionDetails;

    }



}
